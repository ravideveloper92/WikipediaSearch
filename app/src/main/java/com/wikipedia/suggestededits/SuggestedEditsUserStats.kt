package com.wikipedia.suggestededits

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import com.wikipedia.dataclient.Service
import com.wikipedia.dataclient.ServiceFactory
import com.wikipedia.dataclient.WikiSite
import com.wikipedia.dataclient.mwapi.MwQueryResponse
import com.wikipedia.settings.Prefs
import java.util.*
import kotlin.math.ceil

object SuggestedEditsUserStats {
    private const val REVERT_SEVERITY_PAUSE_THRESHOLD = 5
    private const val REVERT_SEVERITY_DISABLE_THRESHOLD = 7
    private const val PAUSE_DURATION_DAYS = 7

    var totalEdits: Int = 0
    var totalReverts: Int = 0

    fun getEditCountsObservable(): Observable<com.wikipedia.dataclient.mwapi.MwQueryResponse> {
        return Observable.zip(com.wikipedia.dataclient.ServiceFactory.get(com.wikipedia.dataclient.WikiSite(com.wikipedia.dataclient.Service.WIKIDATA_URL)).editorTaskCounts, com.wikipedia.dataclient.ServiceFactory.get(com.wikipedia.dataclient.WikiSite(com.wikipedia.dataclient.Service.COMMONS_URL)).editorTaskCounts,
                BiFunction<com.wikipedia.dataclient.mwapi.MwQueryResponse, com.wikipedia.dataclient.mwapi.MwQueryResponse, com.wikipedia.dataclient.mwapi.MwQueryResponse> { wikidataResponse, commonsResponse ->
                    // If the user is blocked on Commons, then boil up the Commons response, otherwise
                    // pass back the Wikidata response, which will be checked for blocking anyway.
                    if (commonsResponse.query()!!.userInfo()!!.isBlocked) commonsResponse else wikidataResponse
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    val editorTaskCounts = it.query()!!.editorTaskCounts()!!
                    totalEdits = editorTaskCounts.totalEdits
                    totalReverts = editorTaskCounts.totalReverts
                    maybePauseAndGetEndDate()
                }
    }

    fun updateStatsInBackground() {
        getEditCountsObservable().subscribe()
    }

    fun getRevertSeverity(): Int {
        return if (totalEdits <= 100) totalReverts else ceil(totalReverts.toFloat() / totalEdits.toFloat() * 100f).toInt()
    }

    fun isDisabled(): Boolean {
        return getRevertSeverity() > REVERT_SEVERITY_DISABLE_THRESHOLD
    }

    fun maybePauseAndGetEndDate(): Date? {
        val pauseDate = com.wikipedia.settings.Prefs.getSuggestedEditsPauseDate()
        var pauseEndDate: Date? = null

        // Are we currently in a pause period?
        if (pauseDate.time != 0L) {
            val cal = Calendar.getInstance()
            cal.time = pauseDate
            cal.add(Calendar.DAY_OF_YEAR, PAUSE_DURATION_DAYS)
            pauseEndDate = cal.time

            if (Date().after((pauseEndDate))) {
                // We've exceeded the pause period, so remove it.
                com.wikipedia.settings.Prefs.setSuggestedEditsPauseDate(Date(0))
                pauseEndDate = null
            }
        }

        if (getRevertSeverity() > REVERT_SEVERITY_PAUSE_THRESHOLD) {
            // Do we need to impose a new pause?
            if (totalReverts > com.wikipedia.settings.Prefs.getSuggestedEditsPauseReverts()) {
                val cal = Calendar.getInstance()
                cal.time = Date()
                com.wikipedia.settings.Prefs.setSuggestedEditsPauseDate(cal.time)
                com.wikipedia.settings.Prefs.setSuggestedEditsPauseReverts(totalReverts)

                cal.add(Calendar.DAY_OF_YEAR, PAUSE_DURATION_DAYS)
                pauseEndDate = cal.time
            }
        }
        return pauseEndDate
    }
}

