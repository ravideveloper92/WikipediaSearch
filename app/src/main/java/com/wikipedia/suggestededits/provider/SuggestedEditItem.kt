package com.wikipedia.suggestededits.provider

import com.google.gson.annotations.SerializedName
import com.wikipedia.dataclient.wikidata.Entities
import com.wikipedia.gallery.GalleryItem

class SuggestedEditItem {
    private val pageid: Int = 0
    private val ns: Int = 0
    private val title: String? = null
    @SerializedName("structured") private val structuredData: com.wikipedia.gallery.GalleryItem.StructuredData? = null
    @SerializedName("wikibase_item") val entity: com.wikipedia.dataclient.wikidata.Entities.Entity? = null

    fun title(): String {
        return title.orEmpty()
    }

    val captions: Map<String, String>
        get() = if (structuredData != null && structuredData.captions != null) structuredData.captions as Map<String, String> else emptyMap()
}
