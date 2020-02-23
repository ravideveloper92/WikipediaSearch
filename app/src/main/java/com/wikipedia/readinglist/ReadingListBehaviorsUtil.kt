package com.wikipedia.readinglist

import android.app.Activity
import android.content.DialogInterface
import android.text.Spanned
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.*
import org.apache.commons.lang3.StringUtils
import com.wikipedia.R
import com.wikipedia.readinglist.database.ReadingList
import com.wikipedia.readinglist.database.ReadingListDbHelper
import com.wikipedia.readinglist.database.ReadingListPage
import com.wikipedia.settings.Prefs
import com.wikipedia.util.DeviceUtil
import com.wikipedia.util.FeedbackUtil
import com.wikipedia.util.StringUtil
import com.wikipedia.util.log.L
import com.wikipedia.views.CircularProgressBar.MIN_PROGRESS
import java.util.*


object ReadingListBehaviorsUtil {

    interface SearchCallback {
        fun onCompleted(lists: MutableList<Any>)
    }

    interface SnackbarCallback {
        fun onUndoDeleteClicked()
    }

    interface Callback {
        fun onCompleted()
    }

    private var allReadingLists = listOf<com.wikipedia.readinglist.database.ReadingList>()

    // Kotlin coroutine
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    private val scope = CoroutineScope(Dispatchers.Main)
    private val exceptionHandler = CoroutineExceptionHandler { _, exception -> com.wikipedia.util.log.L.w(exception) }

    fun getListsContainPage(readingListPage: com.wikipedia.readinglist.database.ReadingListPage): List<com.wikipedia.readinglist.database.ReadingList> {
        val lists = mutableListOf<com.wikipedia.readinglist.database.ReadingList>()
        allReadingLists.forEach { list ->
            list.pages().forEach addToList@{ page ->
                if (page.title() == readingListPage.title()) {
                    lists.add(list)
                    return@addToList
                }
            }
        }
        return lists
    }

    fun savePagesForOffline(activity: Activity, selectedPages: List<com.wikipedia.readinglist.database.ReadingListPage>, callback: Callback) {
        if (com.wikipedia.settings.Prefs.isDownloadOnlyOverWiFiEnabled() && !com.wikipedia.util.DeviceUtil.isOnWiFi()) {
            showMobileDataWarningDialog(activity, DialogInterface.OnClickListener { _, _ ->
                savePagesForOffline(activity, selectedPages, true)
                callback.onCompleted()
            })
        } else {
            savePagesForOffline(activity, selectedPages, !com.wikipedia.settings.Prefs.isDownloadingReadingListArticlesEnabled())
            callback.onCompleted()
        }
    }

    private fun savePagesForOffline(activity: Activity, selectedPages: List<com.wikipedia.readinglist.database.ReadingListPage>, forcedSave: Boolean) {
        if (selectedPages.isNotEmpty()) {
            for (page in selectedPages) {
                resetPageProgress(page)
            }
            com.wikipedia.readinglist.database.ReadingListDbHelper.instance().markPagesForOffline(selectedPages, true, forcedSave)
            showMultiSelectOfflineStateChangeSnackbar(activity, selectedPages, true)
        }
    }

    fun removePagesFromOffline(activity: Activity, selectedPages: List<com.wikipedia.readinglist.database.ReadingListPage>, callback: Callback) {
        if (selectedPages.isNotEmpty()) {
            com.wikipedia.readinglist.database.ReadingListDbHelper.instance().markPagesForOffline(selectedPages, false, false)
            showMultiSelectOfflineStateChangeSnackbar(activity, selectedPages, false)
            callback.onCompleted()
        }
    }

    fun deleteReadingList(activity: Activity, readingList: com.wikipedia.readinglist.database.ReadingList?, showDialog: Boolean, callback: Callback) {
        if (readingList == null) {
            return
        }
        if (showDialog) {
            AlertDialog.Builder(activity)
                    .setMessage(activity.getString(R.string.reading_list_delete_confirm, readingList.title()))
                    .setPositiveButton(R.string.reading_list_delete_dialog_ok_button_text) { _, _ ->
                        com.wikipedia.readinglist.database.ReadingListDbHelper.instance().deleteList(readingList)
                        com.wikipedia.readinglist.database.ReadingListDbHelper.instance().markPagesForDeletion(readingList, readingList.pages(), false)
                        callback.onCompleted() }
                    .setNegativeButton(R.string.reading_list_delete_dialog_cancel_button_text, null)
                    .create()
                    .show()
        } else {
            com.wikipedia.readinglist.database.ReadingListDbHelper.instance().deleteList(readingList)
            com.wikipedia.readinglist.database.ReadingListDbHelper.instance().markPagesForDeletion(readingList, readingList.pages(), false)
            callback.onCompleted()
        }
    }

    fun deletePages(activity: Activity, listsContainPage: List<com.wikipedia.readinglist.database.ReadingList>, readingListPage: com.wikipedia.readinglist.database.ReadingListPage, snackbarCallback: SnackbarCallback, callback: Callback) {
        if (listsContainPage.size > 1) {
            scope.launch(exceptionHandler) {
                val pages = withContext(dispatcher) { com.wikipedia.readinglist.database.ReadingListDbHelper.instance().getAllPageOccurrences(com.wikipedia.readinglist.database.ReadingListPage.toPageTitle(readingListPage)) }
                val lists = withContext(dispatcher) { com.wikipedia.readinglist.database.ReadingListDbHelper.instance().getListsFromPageOccurrences(pages) }
                com.wikipedia.readinglist.RemoveFromReadingListsDialog(lists).deleteOrShowDialog(activity) { list, page ->
                    showDeletePageFromListsUndoSnackbar(activity, list, page, snackbarCallback)
                    callback.onCompleted()
                }
            }
        } else {
            com.wikipedia.readinglist.database.ReadingListDbHelper.instance().markPagesForDeletion(listsContainPage[0], listOf(readingListPage))
            listsContainPage[0].pages().remove(readingListPage)
            showDeletePagesUndoSnackbar(activity, listsContainPage[0], listOf(readingListPage), snackbarCallback)
            callback.onCompleted()
        }
    }

    fun renameReadingList(activity: Activity, readingList: com.wikipedia.readinglist.database.ReadingList?, callback: Callback) {
        if (readingList == null) {
            return
        } else if (readingList.isDefault) {
            com.wikipedia.util.log.L.w("Attempted to rename default list.")
            return
        }

        val tempLists = com.wikipedia.readinglist.database.ReadingListDbHelper.instance().allListsWithoutContents
        val existingTitles = ArrayList<String>()
        for (list in tempLists) {
            existingTitles.add(list.title())
        }
        existingTitles.remove(readingList.title())

        com.wikipedia.readinglist.ReadingListTitleDialog.readingListTitleDialog(activity, readingList.title(), readingList.description(), existingTitles) { text, description ->
            readingList.title(text)
            readingList.description(description)
            readingList.dirty(true)
            com.wikipedia.readinglist.database.ReadingListDbHelper.instance().updateList(readingList, true)
            callback.onCompleted()
        }.show()
    }

    private fun showDeletePageFromListsUndoSnackbar(activity: Activity, lists: List<com.wikipedia.readinglist.database.ReadingList>?, page: com.wikipedia.readinglist.database.ReadingListPage, callback: SnackbarCallback) {
        if (lists == null) {
            return
        }
        com.wikipedia.util.FeedbackUtil
                .makeSnackbar(activity,
                        String.format(activity.getString(
                                if (lists.size == 1) R.string.reading_list_item_deleted else R.string.reading_lists_item_deleted), page.title()),
                        com.wikipedia.util.FeedbackUtil.LENGTH_DEFAULT)
                .setAction(R.string.reading_list_item_delete_undo) {
                    com.wikipedia.readinglist.database.ReadingListDbHelper.instance().addPageToLists(lists, page, true)
                    callback.onUndoDeleteClicked() }
                .show()
    }

    fun showDeletePagesUndoSnackbar(activity: Activity, readingList: com.wikipedia.readinglist.database.ReadingList?, pages: List<com.wikipedia.readinglist.database.ReadingListPage>, callback: SnackbarCallback) {
        if (readingList == null) {
            return
        }
        com.wikipedia.util.FeedbackUtil
                .makeSnackbar(activity,
                        String.format(activity.getString(
                                if (pages.size == 1) R.string.reading_list_item_deleted else R.string.reading_list_items_deleted),
                                if (pages.size == 1) pages[0].title() else pages.size),
                        com.wikipedia.util.FeedbackUtil.LENGTH_DEFAULT)
                .setAction(R.string.reading_list_item_delete_undo) {
                    val newPages = ArrayList<com.wikipedia.readinglist.database.ReadingListPage>()
                    for (page in pages) {
                        newPages.add(com.wikipedia.readinglist.database.ReadingListPage(ReadingListPage.toPageTitle(page)))
                    }
                    com.wikipedia.readinglist.database.ReadingListDbHelper.instance().addPagesToList(readingList, newPages, true)
                    readingList.pages().addAll(newPages)
                    callback.onUndoDeleteClicked() }
                .show()
    }

    fun showDeleteListUndoSnackbar(activity: Activity, readingList: com.wikipedia.readinglist.database.ReadingList?, callback: SnackbarCallback) {
        if (readingList == null) {
            return
        }
        com.wikipedia.util.FeedbackUtil
                .makeSnackbar(activity, String.format(activity.getString(R.string.reading_list_deleted), readingList.title()), com.wikipedia.util.FeedbackUtil.LENGTH_DEFAULT)
                .setAction(R.string.reading_list_item_delete_undo) {
                    val newList = com.wikipedia.readinglist.database.ReadingListDbHelper.instance().createList(readingList.title(), readingList.description())
                    val newPages = ArrayList<com.wikipedia.readinglist.database.ReadingListPage>()
                    for (page in readingList.pages()) {
                        newPages.add(com.wikipedia.readinglist.database.ReadingListPage(ReadingListPage.toPageTitle(page)))
                    }
                    com.wikipedia.readinglist.database.ReadingListDbHelper.instance().addPagesToList(newList, newPages, true)
                    callback.onUndoDeleteClicked()
                }
                .show()
    }

    fun togglePageOffline(activity: Activity, page: com.wikipedia.readinglist.database.ReadingListPage?, callback: Callback) {
        if (page == null) {
            return
        }
        if (page.offline()) {
            scope.launch(exceptionHandler) {
                val pages = withContext(dispatcher) { com.wikipedia.readinglist.database.ReadingListDbHelper.instance().getAllPageOccurrences(com.wikipedia.readinglist.database.ReadingListPage.toPageTitle(page))}
                val lists = withContext(dispatcher) { com.wikipedia.readinglist.database.ReadingListDbHelper.instance().getListsFromPageOccurrences(pages) }
                if (lists.size > 1) {
                    val dialog = AlertDialog.Builder(activity)
                            .setTitle(R.string.reading_list_confirm_remove_article_from_offline_title)
                            .setMessage(getConfirmToggleOfflineMessage(activity, page, lists))
                            .setPositiveButton(R.string.reading_list_confirm_remove_article_from_offline) { _, _ -> toggleOffline(activity, page, callback) }
                            .setNegativeButton(R.string.reading_list_remove_from_offline_cancel_button_text, null)
                            .create()
                    dialog.show()
                } else {
                    toggleOffline(activity, page, callback)
                }
            }
        } else {
            toggleOffline(activity, page, callback)
        }
    }

    fun toggleOffline(activity: Activity, page: com.wikipedia.readinglist.database.ReadingListPage, callback: Callback) {
        resetPageProgress(page)
        if (com.wikipedia.settings.Prefs.isDownloadOnlyOverWiFiEnabled() && !com.wikipedia.util.DeviceUtil.isOnWiFi()) {
            showMobileDataWarningDialog(activity, DialogInterface.OnClickListener { _, _ ->
                toggleOffline(activity, page, true)
                callback.onCompleted()
            })
        } else {
            toggleOffline(activity, page, !com.wikipedia.settings.Prefs.isDownloadingReadingListArticlesEnabled())
            callback.onCompleted()
        }
    }

    private fun toggleOffline(activity: Activity, page: com.wikipedia.readinglist.database.ReadingListPage, forcedSave: Boolean) {
        com.wikipedia.readinglist.database.ReadingListDbHelper.instance().markPageForOffline(page, !page.offline(), forcedSave)
        com.wikipedia.util.FeedbackUtil.showMessage(activity,
                activity.resources.getQuantityString(
                        if (page.offline()) R.plurals.reading_list_article_offline_message else R.plurals.reading_list_article_not_offline_message, 1))
    }

    private fun showMobileDataWarningDialog(activity: Activity, listener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_title_download_only_over_wifi)
                .setMessage(R.string.dialog_text_download_only_over_wifi)
                .setPositiveButton(R.string.dialog_title_download_only_over_wifi_allow, listener)
                .setNegativeButton(R.string.reading_list_download_using_mobile_data_cancel_button_text, null)
                .show()
    }

    private fun showMultiSelectOfflineStateChangeSnackbar(activity: Activity, pages: List<com.wikipedia.readinglist.database.ReadingListPage>, offline: Boolean) {
        com.wikipedia.util.FeedbackUtil.showMessage(activity,
                activity.resources.getQuantityString(
                        if (offline) R.plurals.reading_list_article_offline_message else R.plurals.reading_list_article_not_offline_message, pages.size
                ))
    }

    private fun resetPageProgress(page: com.wikipedia.readinglist.database.ReadingListPage) {
        if (!page.offline()) {
            page.downloadProgress(MIN_PROGRESS)
        }
    }

    private fun getConfirmToggleOfflineMessage(activity: Activity, page: com.wikipedia.readinglist.database.ReadingListPage, lists: List<com.wikipedia.readinglist.database.ReadingList>): Spanned {
        var result = activity.getString(R.string.reading_list_confirm_remove_article_from_offline_message,
                "<b>${page.title()}</b>")
        lists.forEach {
            result += "<br>&nbsp;&nbsp;<b>&#8226; ${it.title()}</b>"
        }
        return com.wikipedia.util.StringUtil.fromHtml(result)
    }

    fun searchListsAndPages(searchQuery: String?, callback: SearchCallback) {
        scope.launch(exceptionHandler) {
            allReadingLists = withContext(dispatcher) { com.wikipedia.readinglist.database.ReadingListDbHelper.instance().allLists }
            val list = withContext(dispatcher) { applySearchQuery(searchQuery, allReadingLists) }
            if (searchQuery.isNullOrEmpty()) {
                com.wikipedia.readinglist.database.ReadingList.sortGenericList(list, com.wikipedia.settings.Prefs.getReadingListSortMode(com.wikipedia.readinglist.database.ReadingList.SORT_BY_NAME_ASC))
            }
            callback.onCompleted(list)
        }
    }

    private fun applySearchQuery(searchQuery: String?, lists: List<com.wikipedia.readinglist.database.ReadingList>): MutableList<Any> {
        val result = mutableListOf<Any>()

        if (searchQuery.isNullOrEmpty()) {
            result.addAll(lists)
            return result
        }

        val normalizedQuery = StringUtils.stripAccents(searchQuery).toLowerCase(Locale.getDefault())
        var lastListItemIndex = 0
        lists.forEach { list ->
            if (StringUtils.stripAccents(list.title()).toLowerCase(Locale.getDefault()).contains(normalizedQuery)) {
                result.add(lastListItemIndex++, list)
            }
            list.pages().forEach { page ->
                if (page.title().toLowerCase(Locale.getDefault()).contains(normalizedQuery)) {
                    var noMatch = true
                    result.forEach checkMatch@{
                        if (it is com.wikipedia.readinglist.database.ReadingListPage && it.title() == page.title()) {
                            noMatch = false
                            return@checkMatch
                        }
                    }
                    if (noMatch) {
                        result.add(page)
                    }
                }
            }
        }
        return result
    }
}
