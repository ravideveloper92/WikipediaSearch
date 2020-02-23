package com.wikipedia.crash

import android.content.Intent

import com.microsoft.appcenter.crashes.AbstractCrashesListener
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog
import com.microsoft.appcenter.crashes.model.ErrorReport

import com.wikipedia.WikipediaApp
import com.wikipedia.json.GsonUtil
import com.wikipedia.settings.Prefs
import com.wikipedia.util.log.L

import java.util.HashMap

import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.microsoft.appcenter.crashes.Crashes

class AppCenterCrashesListener : AbstractCrashesListener(), Thread.UncaughtExceptionHandler {
    private val props = HashMap<String, String>()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    fun putReportProperty(key: String, value: String): AppCenterCrashesListener {
        props[key] = value
        return this
    }

    fun logCrashManually(throwable: Throwable) {
        Crashes.trackError(throwable, null, getPropsAttachment())
    }

    override fun shouldProcess(report: ErrorReport?): Boolean {
        return com.wikipedia.settings.Prefs.isCrashReportAutoUploadEnabled()
    }

    override fun getErrorAttachments(report: ErrorReport?): Iterable<ErrorAttachmentLog> {
        return getPropsAttachment()
    }

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        com.wikipedia.util.log.L.e(exception)
        if (!com.wikipedia.settings.Prefs.crashedBeforeActivityCreated()) {
            com.wikipedia.settings.Prefs.crashedBeforeActivityCreated(true)
            launchCrashReportActivity()
        } else {
            com.wikipedia.util.log.L.i("Crashed before showing UI. Skipping reboot.")
        }
        Runtime.getRuntime().exit(0)
    }

    private fun getPropsAttachment(): Iterable<ErrorAttachmentLog> {
        val textLog = ErrorAttachmentLog.attachmentWithText(com.wikipedia.json.GsonUtil.getDefaultGson().toJson(props), "details.txt")
        return listOf(textLog)
    }

    private fun launchCrashReportActivity() {
        val intent = Intent(com.wikipedia.WikipediaApp.getInstance(), com.wikipedia.crash.CrashReportActivity::class.java)
                .addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
        com.wikipedia.WikipediaApp.getInstance().startActivity(intent)
    }
}
