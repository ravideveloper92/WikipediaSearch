package com.wikipedia.page

import android.annotation.SuppressLint
import kotlinx.coroutines.*
import com.wikipedia.WikipediaApp
import com.wikipedia.readinglist.database.ReadingListDbHelper
import com.wikipedia.readinglist.database.ReadingListPage
import com.wikipedia.util.log.L

object PageAvailableOfflineHandler {
    interface Callback {
        fun onFinish(available: Boolean)
    }

    fun check(page: com.wikipedia.readinglist.database.ReadingListPage, callback: Callback) {
        callback.onFinish(com.wikipedia.WikipediaApp.getInstance().isOnline || (page.offline() && !page.saving()))
    }

    @SuppressLint("CheckResult")
    fun check(pageTitle: com.wikipedia.page.PageTitle, callback: Callback) {
        if (com.wikipedia.WikipediaApp.getInstance().isOnline) {
            callback.onFinish(true)
            return
        }
        CoroutineScope(Dispatchers.Main).launch(CoroutineExceptionHandler { _, exception ->
            run {
                callback.onFinish(false)
                com.wikipedia.util.log.L.w(exception)
            }
        }) {
            val readingListPage = withContext(Dispatchers.IO) { com.wikipedia.readinglist.database.ReadingListDbHelper.instance().findPageInAnyList(pageTitle) }
            callback.onFinish(readingListPage != null && readingListPage.offline() && !readingListPage.saving())
        }
    }
}