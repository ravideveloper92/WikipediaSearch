package com.wikipedia.suggestededits.provider

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import com.wikipedia.dataclient.Service
import com.wikipedia.dataclient.ServiceFactory
import com.wikipedia.dataclient.WikiSite
import com.wikipedia.dataclient.mwapi.MwQueryPage
import com.wikipedia.dataclient.page.PageSummary
import com.wikipedia.page.PageTitle
import java.util.*
import java.util.concurrent.Semaphore

object MissingDescriptionProvider {
    private val mutex : Semaphore = Semaphore(1)

    private val articlesWithMissingDescriptionCache : Stack<String> = Stack()
    private var articlesWithMissingDescriptionCacheLang : String = ""
    private val articlesWithTranslatableDescriptionCache : Stack<Pair<com.wikipedia.page.PageTitle, com.wikipedia.page.PageTitle>> = Stack()
    private var articlesWithTranslatableDescriptionCacheFromLang : String = ""
    private var articlesWithTranslatableDescriptionCacheToLang : String = ""

    private val imagesWithMissingCaptionsCache : Stack<String> = Stack()
    private var imagesWithMissingCaptionsCacheLang : String = ""
    private val imagesWithTranslatableCaptionCache : Stack<Pair<String, String>> = Stack()
    private var imagesWithTranslatableCaptionCacheFromLang : String = ""
    private var imagesWithTranslatableCaptionCacheToLang : String = ""

    private val imagesWithMissingTagsCache : Stack<com.wikipedia.dataclient.mwapi.MwQueryPage> = Stack()

    // TODO: add a maximum-retry limit -- it's currently infinite, or until disposed.

    fun getNextArticleWithMissingDescription(wiki: com.wikipedia.dataclient.WikiSite): Observable<com.wikipedia.dataclient.page.PageSummary> {
        return Observable.fromCallable { mutex.acquire() }.flatMap {
            var cachedTitle = ""
            if (articlesWithMissingDescriptionCacheLang != wiki.languageCode()) {
                // evict the cache if the language has changed.
                articlesWithMissingDescriptionCache.clear()
            }
            if (!articlesWithMissingDescriptionCache.empty()) {
                cachedTitle = articlesWithMissingDescriptionCache.pop()
            }

            if (cachedTitle.isNotEmpty()) {
                Observable.just(cachedTitle)
            } else {
                com.wikipedia.dataclient.ServiceFactory.getRest(com.wikipedia.dataclient.WikiSite(com.wikipedia.dataclient.Service.WIKIDATA_URL)).getArticlesWithoutDescriptions(com.wikipedia.dataclient.WikiSite.normalizeLanguageCode(wiki.languageCode()))
                        .map { pages ->
                            var title: String? = null
                            articlesWithMissingDescriptionCacheLang = wiki.languageCode()
                            for (page in pages) {
                                articlesWithMissingDescriptionCache.push(page.title())
                            }
                            if (!articlesWithMissingDescriptionCache.empty()) {
                                title = articlesWithMissingDescriptionCache.pop()
                            }
                            if (title == null) {
                                throw ListEmptyException()
                            }
                            title
                        }
                        .retry { t: Throwable -> t is ListEmptyException }
            }
        }.flatMap { title -> com.wikipedia.dataclient.ServiceFactory.getRest(wiki).getSummary(null, title) }
                .doFinally { mutex.release() }
    }

    fun getNextArticleWithMissingDescription(sourceWiki: com.wikipedia.dataclient.WikiSite, targetLang: String, sourceLangMustExist: Boolean): Observable<Pair<com.wikipedia.dataclient.page.PageSummary, com.wikipedia.dataclient.page.PageSummary>> {
        return Observable.fromCallable { mutex.acquire() }.flatMap {
            val targetWiki = com.wikipedia.dataclient.WikiSite.forLanguageCode(targetLang)
            var cachedPair: Pair<com.wikipedia.page.PageTitle, com.wikipedia.page.PageTitle>? = null
            if (articlesWithTranslatableDescriptionCacheFromLang != sourceWiki.languageCode()
                    || articlesWithTranslatableDescriptionCacheToLang != targetLang) {
                // evict the cache if the language has changed.
                articlesWithTranslatableDescriptionCache.clear()
            }
            if (!articlesWithTranslatableDescriptionCache.empty()) {
                cachedPair = articlesWithTranslatableDescriptionCache.pop()
            }

            if (cachedPair != null) {
                Observable.just(cachedPair)
            } else {
                com.wikipedia.dataclient.ServiceFactory.getRest(com.wikipedia.dataclient.WikiSite(com.wikipedia.dataclient.Service.WIKIDATA_URL)).getArticlesWithTranslatableDescriptions(com.wikipedia.dataclient.WikiSite.normalizeLanguageCode(sourceWiki.languageCode()), com.wikipedia.dataclient.WikiSite.normalizeLanguageCode(targetLang))
                        .map { pages ->
                            var sourceAndTargetPageTitles: Pair<com.wikipedia.page.PageTitle, com.wikipedia.page.PageTitle>? = null
                            articlesWithTranslatableDescriptionCacheFromLang = sourceWiki.languageCode()
                            articlesWithTranslatableDescriptionCacheToLang = targetLang
                            for (page in pages) {
                                val entity = page.entity
                                if (entity == null
                                        || entity.descriptions().containsKey(targetLang)
                                        || sourceLangMustExist && !entity.descriptions().containsKey(sourceWiki.languageCode())
                                        || !entity.sitelinks().containsKey(sourceWiki.dbName())
                                        || !entity.sitelinks().containsKey(targetWiki.dbName())) {
                                    continue
                                }
                                articlesWithTranslatableDescriptionCache.push(Pair(com.wikipedia.page.PageTitle(entity.sitelinks()[targetWiki.dbName()]!!.title, targetWiki),
                                        com.wikipedia.page.PageTitle(entity.sitelinks()[sourceWiki.dbName()]!!.title, sourceWiki)))
                            }
                            if (!articlesWithTranslatableDescriptionCache.empty()) {
                                sourceAndTargetPageTitles = articlesWithTranslatableDescriptionCache.pop()
                            }
                            if (sourceAndTargetPageTitles == null) {
                                throw ListEmptyException()
                            }
                            sourceAndTargetPageTitles
                        }
                        .retry { t: Throwable -> t is ListEmptyException }
            }
        }.flatMap { sourceAndTargetPageTitles: Pair<com.wikipedia.page.PageTitle, com.wikipedia.page.PageTitle> -> getSummary(sourceAndTargetPageTitles) }
                .doFinally { mutex.release() }
    }

    private fun getSummary(titles: Pair<com.wikipedia.page.PageTitle, com.wikipedia.page.PageTitle>): Observable<Pair<com.wikipedia.dataclient.page.PageSummary, com.wikipedia.dataclient.page.PageSummary>> {
        return Observable.zip(com.wikipedia.dataclient.ServiceFactory.getRest(titles.first.wikiSite).getSummary(null, titles.first.prefixedText),
                com.wikipedia.dataclient.ServiceFactory.getRest(titles.second.wikiSite).getSummary(null, titles.second.prefixedText),
                BiFunction<com.wikipedia.dataclient.page.PageSummary, com.wikipedia.dataclient.page.PageSummary, Pair<com.wikipedia.dataclient.page.PageSummary, com.wikipedia.dataclient.page.PageSummary>> { source, target -> Pair(source, target) })
    }

    fun getNextImageWithMissingCaption(lang: String): Observable<String> {
        return Observable.fromCallable { mutex.acquire() }.flatMap {
            var cachedTitle: String? = null
            if (imagesWithMissingCaptionsCacheLang != lang) {
                // evict the cache if the language has changed.
                imagesWithMissingCaptionsCache.clear()
            }
            if (!imagesWithMissingCaptionsCache.empty()) {
                cachedTitle = imagesWithMissingCaptionsCache.pop()
            }

            if (cachedTitle != null) {
                Observable.just(cachedTitle)
            } else {
                com.wikipedia.dataclient.ServiceFactory.getRest(com.wikipedia.dataclient.WikiSite(com.wikipedia.dataclient.Service.COMMONS_URL)).getImagesWithoutCaptions(com.wikipedia.dataclient.WikiSite.normalizeLanguageCode(lang))
                        .map { pages ->
                            imagesWithMissingCaptionsCacheLang = lang
                            for (page in pages) {
                                imagesWithMissingCaptionsCache.push(page.title())
                            }
                            var item: String? = null
                            if (!imagesWithMissingCaptionsCache.empty()) {
                                item = imagesWithMissingCaptionsCache.pop()
                            }
                            if (item == null) {
                                throw ListEmptyException()
                            }
                            item
                        }
                        .retry { t: Throwable -> t is ListEmptyException }
            }
        }.doFinally { mutex.release() }
    }

    fun getNextImageWithMissingCaption(sourceLang: String, targetLang: String): Observable<Pair<String, String>> {
        return Observable.fromCallable { mutex.acquire() }.flatMap {
            var cachedPair: Pair<String, String>? = null
            if (imagesWithTranslatableCaptionCacheFromLang != sourceLang
                    || imagesWithTranslatableCaptionCacheToLang != targetLang) {
                // evict the cache if the language has changed.
                imagesWithTranslatableCaptionCache.clear()
            }
            if (!imagesWithTranslatableCaptionCache.empty()) {
                cachedPair = imagesWithTranslatableCaptionCache.pop()
            }

            if (cachedPair != null) {
                Observable.just(cachedPair)
            } else {
                com.wikipedia.dataclient.ServiceFactory.getRest(com.wikipedia.dataclient.WikiSite(com.wikipedia.dataclient.Service.COMMONS_URL)).getImagesWithTranslatableCaptions(com.wikipedia.dataclient.WikiSite.normalizeLanguageCode(sourceLang), com.wikipedia.dataclient.WikiSite.normalizeLanguageCode(targetLang))
                        .map { pages ->
                            imagesWithTranslatableCaptionCacheFromLang = sourceLang
                            imagesWithTranslatableCaptionCacheToLang = targetLang

                            var item: Pair<String, String>? = null
                            for (page in pages) {
                                if (!page.captions.containsKey(sourceLang) || page.captions.containsKey(targetLang)) {
                                    continue
                                }
                                imagesWithTranslatableCaptionCache.push(Pair(page.captions[sourceLang]!!, page.title()))
                            }
                            if (!imagesWithTranslatableCaptionCache.empty()) {
                                item = imagesWithTranslatableCaptionCache.pop()
                            }
                            if (item == null) {
                                throw ListEmptyException()
                            }
                            item
                        }
                        .retry { t: Throwable -> t is ListEmptyException }
            }
        }.doFinally { mutex.release() }
    }

    fun getNextImageWithMissingTags(lang: String): Observable<com.wikipedia.dataclient.mwapi.MwQueryPage> {
        return Observable.fromCallable { mutex.acquire() }.flatMap {
            var cachedItem: com.wikipedia.dataclient.mwapi.MwQueryPage? = null
            if (!imagesWithMissingTagsCache.empty()) {
                cachedItem = imagesWithMissingTagsCache.pop()
            }

            if (cachedItem != null) {
                Observable.just(cachedItem)
            } else {
                com.wikipedia.dataclient.ServiceFactory.get(com.wikipedia.dataclient.WikiSite(com.wikipedia.dataclient.Service.COMMONS_URL)).getImagesWithUnreviewedLabels(com.wikipedia.dataclient.WikiSite.normalizeLanguageCode(lang))
                        .map { response ->
                            for (page in response.query()!!.pages()!!) {
                                // make sure there's at least one unreviewed tag
                                var hasUnreviewed = false
                                for (label in page.imageLabels) {
                                    if (label.state == "unreviewed") {
                                        hasUnreviewed = true
                                        break
                                    }
                                }
                                if (hasUnreviewed) {
                                    imagesWithMissingTagsCache.push(page)
                                }
                            }
                            var item: com.wikipedia.dataclient.mwapi.MwQueryPage? = null
                            if (!imagesWithMissingTagsCache.empty()) {
                                item = imagesWithMissingTagsCache.pop()
                            }
                            if (item == null) {
                                throw ListEmptyException()
                            }
                            item
                        }
                        .retry { t: Throwable -> t is ListEmptyException }
            }
        }.doFinally { mutex.release() }
    }

    private class ListEmptyException : RuntimeException()
}
