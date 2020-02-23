package com.wikipedia.descriptions

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.view_description_edit_read_article_bar.view.*
import com.wikipedia.R
import com.wikipedia.suggestededits.SuggestedEditsSummary
import com.wikipedia.util.L10nUtil.setConditionalLayoutDirection
import com.wikipedia.util.StringUtil
import com.wikipedia.views.ViewUtil

class DescriptionEditBottomBarView constructor(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_description_edit_read_article_bar, this)
        hide()
    }

    fun show() {
        visibility = VISIBLE
    }

    fun hide() {
        visibility = GONE
    }

    fun setSummary(summary: SuggestedEditsSummary) {
        setConditionalLayoutDirection(this, summary.lang)
        viewArticleTitle!!.text = com.wikipedia.util.StringUtil.fromHtml(com.wikipedia.util.StringUtil.removeNamespace(summary.displayTitle!!))
        if (summary.thumbnailUrl.isNullOrEmpty()) {
            viewImageThumbnail.visibility = GONE
        } else {
            viewImageThumbnail.visibility = VISIBLE
            com.wikipedia.views.ViewUtil.loadImageUrlInto(viewImageThumbnail, summary.thumbnailUrl)
        }
        show()
    }
}
