package com.wikipedia.feed.suggestededits

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import com.wikipedia.WikipediaApp
import com.wikipedia.dataclient.ServiceFactory
import com.wikipedia.dataclient.WikiSite
import com.wikipedia.descriptions.DescriptionEditActivity
import com.wikipedia.descriptions.DescriptionEditActivity.Action.*
import com.wikipedia.feed.FeedCoordinator
import com.wikipedia.feed.dataclient.FeedClient
import com.wikipedia.feed.model.Card
import com.wikipedia.page.Namespace
import com.wikipedia.page.PageTitle
import com.wikipedia.suggestededits.SuggestedEditsSummary
import com.wikipedia.suggestededits.SuggestedEditsUserStats
import com.wikipedia.suggestededits.provider.MissingDescriptionProvider
import com.wikipedia.util.StringUtil
import java.util.*

class SuggestedEditsFeedClient(private var action: com.wikipedia.descriptions.DescriptionEditActivity.Action) : com.wikipedia.feed.dataclient.FeedClient {
    interface Callback {
        fun updateCardContent(card: SuggestedEditsCard)
    }

    private var age: Int = 0
    private val disposables = CompositeDisposable()
    private val app = com.wikipedia.WikipediaApp.getInstance()
    private var sourceSummary: SuggestedEditsSummary? = null
    private var targetSummary: SuggestedEditsSummary? = null
    private val langFromCode: String = app.language().appLanguageCode
    private val langToCode: String = if (app.language().appLanguageCodes.size == 1) "" else app.language().appLanguageCodes[1]

    override fun request(context: Context, wiki: com.wikipedia.dataclient.WikiSite, age: Int, cb: com.wikipedia.feed.dataclient.FeedClient.Callback) {
        this.age = age
        cancel()

        if (age == 0) {
            // In the background, fetch the user's latest contribution stats, so that we can update whether the
            // Suggested Edits feature is paused or disabled, the next time the feed is refreshed.
            SuggestedEditsUserStats.updateStatsInBackground()
        }

        if (SuggestedEditsUserStats.isDisabled() || SuggestedEditsUserStats.maybePauseAndGetEndDate() != null) {
            com.wikipedia.feed.FeedCoordinator.postCardsToCallback(cb, Collections.emptyList())
            return
        }

        fetchSuggestedEditForType(cb, null)
    }

    override fun cancel() {
        disposables.clear()
    }

    private fun toSuggestedEditsCard(wiki: com.wikipedia.dataclient.WikiSite): SuggestedEditsCard {
        return SuggestedEditsCard(wiki, action, sourceSummary, targetSummary, age)
    }

    fun fetchSuggestedEditForType(cb: com.wikipedia.feed.dataclient.FeedClient.Callback?, callback: Callback?) {
        when (action) {
            TRANSLATE_DESCRIPTION -> getArticleToTranslateDescription(cb, callback)
            ADD_CAPTION -> getImageToAddCaption(cb, callback)
            TRANSLATE_CAPTION -> getImageToTranslateCaption(cb, callback)
            else -> getArticleToAddDescription(cb, callback)
        }
    }

    private fun getArticleToAddDescription(cb: com.wikipedia.feed.dataclient.FeedClient.Callback?, callback: Callback?) {
        disposables.add(MissingDescriptionProvider
                .getNextArticleWithMissingDescription(com.wikipedia.dataclient.WikiSite.forLanguageCode(langFromCode))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ pageSummary ->
                    sourceSummary = SuggestedEditsSummary(
                            pageSummary.apiTitle,
                            langFromCode,
                            pageSummary.getPageTitle(com.wikipedia.dataclient.WikiSite.forLanguageCode(langFromCode)),
                            pageSummary.displayTitle,
                            pageSummary.description,
                            pageSummary.thumbnailUrl,
                            pageSummary.extractHtml,
                            null, null, null
                    )

                    val card: SuggestedEditsCard = toSuggestedEditsCard(com.wikipedia.dataclient.WikiSite.forLanguageCode(langFromCode))

                    if (callback == null) {
                        com.wikipedia.feed.FeedCoordinator.postCardsToCallback(cb!!, if (sourceSummary == null) emptyList<com.wikipedia.feed.model.Card>() else listOf(card))
                    } else {
                        callback.updateCardContent(card)
                    }

                }, { if (callback == null) cb!!.success(emptyList()) }))
    }

    private fun getArticleToTranslateDescription(cb: com.wikipedia.feed.dataclient.FeedClient.Callback?, callback: Callback?) {
        disposables.add(MissingDescriptionProvider
                .getNextArticleWithMissingDescription(com.wikipedia.dataclient.WikiSite.forLanguageCode(langFromCode), langToCode, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ pair ->
                    val source = pair.second
                    val target = pair.first

                    sourceSummary = SuggestedEditsSummary(
                            source.apiTitle,
                            langFromCode,
                            source.getPageTitle(com.wikipedia.dataclient.WikiSite.forLanguageCode(langFromCode)),
                            source.displayTitle,
                            source.description,
                            source.thumbnailUrl,
                            source.extractHtml,
                            null, null, null
                    )

                    targetSummary = SuggestedEditsSummary(
                            target.apiTitle,
                            langToCode,
                            target.getPageTitle(com.wikipedia.dataclient.WikiSite.forLanguageCode(langToCode)),
                            target.displayTitle,
                            target.description,
                            target.thumbnailUrl,
                            target.extractHtml,
                            null, null, null
                    )

                    val card: SuggestedEditsCard = toSuggestedEditsCard(com.wikipedia.dataclient.WikiSite.forLanguageCode(langFromCode))

                    if (callback == null) {
                        com.wikipedia.feed.FeedCoordinator.postCardsToCallback(cb!!, if (pair == null) emptyList<com.wikipedia.feed.model.Card>() else listOf(card))
                    } else {
                        callback.updateCardContent(card)
                    }

                }, { if (callback != null) cb!!.success(emptyList()) }))
    }

    private fun getImageToAddCaption(cb: com.wikipedia.feed.dataclient.FeedClient.Callback?, callback: Callback?) {
        disposables.add(MissingDescriptionProvider.getNextImageWithMissingCaption(langFromCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { title ->
                    com.wikipedia.dataclient.ServiceFactory.get(com.wikipedia.dataclient.WikiSite.forLanguageCode(langFromCode)).getImageExtMetadata(title)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                }
                .subscribe({ response ->
                    val page = response.query()!!.pages()!![0]
                    if (page.imageInfo() != null) {
                        val title = page.title()
                        val imageInfo = page.imageInfo()!!

                        sourceSummary = SuggestedEditsSummary(
                                title,
                                langFromCode,
                                com.wikipedia.page.PageTitle(
                                        com.wikipedia.page.Namespace.FILE.name,
                                        com.wikipedia.util.StringUtil.removeNamespace(title),
                                        null,
                                        imageInfo.thumbUrl,
                                        com.wikipedia.dataclient.WikiSite.forLanguageCode(langFromCode)
                                ),
                                com.wikipedia.util.StringUtil.removeHTMLTags(title),
                                imageInfo.metadata!!.imageDescription(),
                                imageInfo.thumbUrl,
                                null,
                                imageInfo.timestamp,
                                imageInfo.user,
                                imageInfo.metadata
                        )
                        val card: SuggestedEditsCard = toSuggestedEditsCard(com.wikipedia.dataclient.WikiSite.forLanguageCode(langFromCode))
                        if (callback == null) {
                            com.wikipedia.feed.FeedCoordinator.postCardsToCallback(cb!!, if (sourceSummary == null) emptyList<com.wikipedia.feed.model.Card>() else listOf(card))
                        } else {
                            callback.updateCardContent(card)
                        }
                    }
                }, { if (callback != null) cb!!.success(emptyList()) }))
    }

    private fun getImageToTranslateCaption(cb: com.wikipedia.feed.dataclient.FeedClient.Callback?, callback: Callback?) {
        var fileCaption: String? = null

        disposables.add(MissingDescriptionProvider.getNextImageWithMissingCaption(langFromCode, langToCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { pair ->
                    fileCaption = pair.first
                    com.wikipedia.dataclient.ServiceFactory.get(com.wikipedia.dataclient.WikiSite.forLanguageCode(langFromCode)).getImageExtMetadata(pair.second)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                }
                .subscribe({ response ->
                    val page = response.query()!!.pages()!![0]
                    if (page.imageInfo() != null) {
                        val title = page.title()
                        val imageInfo = page.imageInfo()!!

                        sourceSummary = SuggestedEditsSummary(
                                title,
                                langFromCode,
                                com.wikipedia.page.PageTitle(
                                        com.wikipedia.page.Namespace.FILE.name,
                                        com.wikipedia.util.StringUtil.removeNamespace(title),
                                        null,
                                        imageInfo.thumbUrl,
                                        com.wikipedia.dataclient.WikiSite.forLanguageCode(langFromCode)
                                ),
                                com.wikipedia.util.StringUtil.removeHTMLTags(title),
                                fileCaption,
                                imageInfo.thumbUrl,
                                null,
                                imageInfo.timestamp,
                                imageInfo.user,
                                imageInfo.metadata
                        )

                        targetSummary = sourceSummary!!.copy(
                                description = null,
                                lang = langToCode,
                                pageTitle = com.wikipedia.page.PageTitle(
                                        com.wikipedia.page.Namespace.FILE.name,
                                        com.wikipedia.util.StringUtil.removeNamespace(title),
                                        null,
                                        imageInfo.thumbUrl,
                                        com.wikipedia.dataclient.WikiSite.forLanguageCode(langToCode)
                                )
                        )

                        val card: SuggestedEditsCard = toSuggestedEditsCard(com.wikipedia.dataclient.WikiSite.forLanguageCode(langToCode))
                        if (callback == null) {
                            com.wikipedia.feed.FeedCoordinator.postCardsToCallback(cb!!, if (targetSummary == null) emptyList<com.wikipedia.feed.model.Card>() else listOf(card))
                        } else {
                            callback.updateCardContent(card)
                        }
                    }
                }, { if (callback == null) cb!!.success(emptyList()) }))
    }

}
