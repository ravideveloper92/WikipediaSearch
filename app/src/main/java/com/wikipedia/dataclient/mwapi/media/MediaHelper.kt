package com.wikipedia.dataclient.mwapi.media

import com.wikipedia.dataclient.Service
import com.wikipedia.dataclient.ServiceFactory
import com.wikipedia.dataclient.WikiSite

import java.util.HashMap

import io.reactivex.Observable

object MediaHelper {
    private const val COMMONS_DB_NAME = "commonswiki"

    /**
     * Returns a map of "language":"caption" combinations for a particular file on Commons.
     */
    fun getImageCaptions(fileName: String): Observable<Map<String, String>> {
        return com.wikipedia.dataclient.ServiceFactory.get(com.wikipedia.dataclient.WikiSite(com.wikipedia.dataclient.Service.COMMONS_URL)).getEntitiesByTitle(fileName, COMMONS_DB_NAME)
                .map { entities ->
                    val captions = HashMap<String, String>()
                    for (label in entities.first!!.labels().values) {
                        captions[label.language()] = label.value()
                    }
                    captions
                }
    }

}
