package com.wikipedia.dataclient.okhttp

import okhttp3.Interceptor
import okhttp3.Response
import com.wikipedia.WikipediaApp
import com.wikipedia.settings.Prefs.isEventLoggingEnabled
import java.io.IOException

internal class CommonHeaderRequestInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val app = com.wikipedia.WikipediaApp.getInstance()
        val request = chain.request().newBuilder()
                .header("User-Agent", app.userAgent)
                .header(if (isEventLoggingEnabled()) "X-WMF-UUID" else "DNT",
                        if (isEventLoggingEnabled()) app.appInstallID else "1")
                .build()
        return chain.proceed(request)
    }
}
