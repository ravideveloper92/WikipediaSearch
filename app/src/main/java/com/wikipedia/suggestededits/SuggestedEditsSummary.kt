package com.wikipedia.suggestededits

import com.wikipedia.Constants
import com.wikipedia.gallery.ExtMetadata
import com.wikipedia.page.PageTitle
import com.wikipedia.util.ImageUrlUtil

data class SuggestedEditsSummary(
        var title: String,
        var lang: String,
        var pageTitle: com.wikipedia.page.PageTitle,
        var displayTitle: String?,
        var description: String?,
        var thumbnailUrl: String?,
        var extractHtml: String?,
        var timestamp: String?,
        var user: String?,
        var metadata: com.wikipedia.gallery.ExtMetadata?
) {
    fun getPreferredSizeThumbnailUrl(): String = ImageUrlUtil.getUrlForPreferredSize(thumbnailUrl!!, com.wikipedia.Constants.PREFERRED_CARD_THUMBNAIL_SIZE)
}
