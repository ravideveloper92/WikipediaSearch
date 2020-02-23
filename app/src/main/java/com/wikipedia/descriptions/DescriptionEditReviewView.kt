package com.wikipedia.descriptions

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.view_description_edit_review.view.*
import com.wikipedia.R
import com.wikipedia.descriptions.DescriptionEditLicenseView.Companion.ARG_NOTICE_ARTICLE_DESCRIPTION
import com.wikipedia.descriptions.DescriptionEditLicenseView.Companion.ARG_NOTICE_DEFAULT
import com.wikipedia.descriptions.DescriptionEditLicenseView.Companion.ARG_NOTICE_IMAGE_CAPTION
import com.wikipedia.suggestededits.SuggestedEditsSummary
import com.wikipedia.util.L10nUtil
import com.wikipedia.util.StringUtil

class DescriptionEditReviewView constructor(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_description_edit_review, this)
    }

    val isShowing: Boolean
        get() = visibility == VISIBLE


    fun show() {
        visibility = VISIBLE
    }

    fun hide() {
        visibility = GONE
    }

    fun setSummary(summary: SuggestedEditsSummary, description: String, captionReview: Boolean) {
        com.wikipedia.util.L10nUtil.setConditionalLayoutDirection(this, summary.lang)
        if (captionReview) {
            setGalleryReviewView(summary, description)
            licenseView.buildLicenseNotice(ARG_NOTICE_IMAGE_CAPTION)
        } else {
            setDescriptionReviewView(summary, description)
            licenseView.buildLicenseNotice(if (summary.description.isNullOrEmpty()) ARG_NOTICE_ARTICLE_DESCRIPTION else ARG_NOTICE_DEFAULT)
        }
    }

    private fun setDescriptionReviewView(summary: SuggestedEditsSummary, description: String) {
        galleryContainer.visibility = GONE
        articleTitle!!.text = com.wikipedia.util.StringUtil.fromHtml(summary.displayTitle)
        articleSubtitle!!.text = description
        articleExtract!!.text = com.wikipedia.util.StringUtil.fromHtml(summary.extractHtml)

        if (summary.thumbnailUrl.isNullOrBlank()) {
            articleImage.visibility = GONE
            articleExtract.maxLines = ARTICLE_EXTRACT_MAX_LINE_WITHOUT_IMAGE
        } else {
            articleImage.visibility = VISIBLE
            articleImage.loadImage(Uri.parse(summary.getPreferredSizeThumbnailUrl()))
            articleExtract.maxLines = ARTICLE_EXTRACT_MAX_LINE_WITH_IMAGE
        }
    }

    private fun setGalleryReviewView(summary: SuggestedEditsSummary, description: String) {
        articleContainer.visibility = GONE
        indicatorDivider.visibility = GONE
        galleryDescriptionText.text = com.wikipedia.util.StringUtil.fromHtml(description)
        if (summary.thumbnailUrl.isNullOrBlank()) {
            galleryImage.visibility = GONE
        } else {
            galleryImage.visibility = VISIBLE
            galleryImage.loadImage(Uri.parse(summary.getPreferredSizeThumbnailUrl()))
        }
        licenseView.darkLicenseView()
    }

    companion object {
        const val ARTICLE_EXTRACT_MAX_LINE_WITH_IMAGE = 9
        const val ARTICLE_EXTRACT_MAX_LINE_WITHOUT_IMAGE = 15
    }

}
