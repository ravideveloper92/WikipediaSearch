package com.wikipedia.util

import android.net.Uri

object ImageUrlUtil {

    private val WIDTH_IN_IMAGE_URL_REGEX = """/(\d+)px-""".toPattern()

    @JvmStatic
    fun getUrlForSize(uri: Uri, size: Int): Uri {
        return Uri.parse(getUrlForSize(uri.toString(), size))
    }

    @JvmStatic
    fun getUrlForSize(original: String, size: Int): String {
        val matcher = WIDTH_IN_IMAGE_URL_REGEX.matcher(original)
        return if (matcher.find() && matcher.group(1).toInt() > size) {
            matcher.replaceAll("/${size}px-")
        } else {
            original
        }
    }

    @JvmStatic
    fun getUrlForPreferredSize(original: String, size: Int): String {
        val matcher = WIDTH_IN_IMAGE_URL_REGEX.matcher(original)
        return if (matcher.find() && matcher.group(1).toInt() != size) {
            matcher.replaceAll("/${size}px-")
        } else {
            original
        }
    }
}
