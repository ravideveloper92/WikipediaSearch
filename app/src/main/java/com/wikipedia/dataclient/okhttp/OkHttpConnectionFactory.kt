package com.wikipedia.dataclient.okhttp

import okhttp3.Cache
import okhttp3.CacheDelegate
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.wikipedia.WikipediaApp
import com.wikipedia.dataclient.SharedPreferenceCookieManager
import com.wikipedia.settings.Prefs
import java.io.File

object OkHttpConnectionFactory {
    private const val CACHE_DIR_NAME = "okhttp-cache"
    private const val NET_CACHE_SIZE = (64 * 1024 * 1024).toLong()
    private const val SAVED_PAGE_CACHE_SIZE = NET_CACHE_SIZE * 1024
    private val NET_CACHE = Cache(File(com.wikipedia.WikipediaApp.getInstance().cacheDir, CACHE_DIR_NAME), NET_CACHE_SIZE)

    @JvmField val SAVE_CACHE = CacheDelegate(Cache(File(com.wikipedia.WikipediaApp.getInstance().filesDir, CACHE_DIR_NAME), SAVED_PAGE_CACHE_SIZE))
    @JvmStatic val client = createClient()

    private fun createClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .cookieJar(com.wikipedia.dataclient.SharedPreferenceCookieManager.getInstance())
                .cache(NET_CACHE)
                .addInterceptor(HttpLoggingInterceptor().setLevel(com.wikipedia.settings.Prefs.getRetrofitLogLevel()))
                .addInterceptor(UnsuccessfulResponseInterceptor())
                .addNetworkInterceptor(CacheControlInterceptor())
                .addInterceptor(CommonHeaderRequestInterceptor())
                .addInterceptor(DefaultMaxStaleRequestInterceptor())
                .addInterceptor(com.wikipedia.dataclient.okhttp.OfflineCacheInterceptor(SAVE_CACHE))
                .addInterceptor(TestStubInterceptor())
                .build()
    }
}
