package com.wikipedia.suggestededits

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import com.wikipedia.Constants.INTENT_EXTRA_ACTION
import com.wikipedia.R
import com.wikipedia.activity.SingleFragmentActivity
import com.wikipedia.descriptions.DescriptionEditActivity.Action
import com.wikipedia.descriptions.DescriptionEditActivity.Action.*
import com.wikipedia.suggestededits.SuggestedEditsCardsFragment.Companion.newInstance
import com.wikipedia.util.ResourceUtil
import com.wikipedia.views.ImageZoomHelper
import java.lang.Exception

class SuggestedEditsCardsActivity : com.wikipedia.activity.SingleFragmentActivity<SuggestedEditsCardsFragment>() {

    private lateinit var imageZoomHelper: com.wikipedia.views.ImageZoomHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = getString(getActionBarTitleRes(intent.getSerializableExtra(INTENT_EXTRA_ACTION) as Action))
        imageZoomHelper = com.wikipedia.views.ImageZoomHelper(this)
        setStatusBarColor(com.wikipedia.util.ResourceUtil.getThemedColor(this, R.attr.paper_color))
        setNavigationBarColor(com.wikipedia.util.ResourceUtil.getThemedColor(this, R.attr.paper_color))
    }

    override fun createFragment(): SuggestedEditsCardsFragment {
        return newInstance(intent.getSerializableExtra(INTENT_EXTRA_ACTION) as Action)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        try {
            return imageZoomHelper.onDispatchTouchEvent(event) || super.dispatchTouchEvent(event)
        } catch (e: Exception) { }
        return false
    }

    private fun getActionBarTitleRes(action: Action): Int {
        return when(action) {
            TRANSLATE_DESCRIPTION -> {
                R.string.suggested_edits_translate_descriptions
            }
            ADD_CAPTION -> {
                R.string.suggested_edits_add_image_captions
            }
            TRANSLATE_CAPTION -> {
                R.string.suggested_edits_translate_image_captions
            }
            ADD_IMAGE_TAGS -> {
                R.string.suggested_edits_tag_images
            }
            else -> R.string.suggested_edits_add_descriptions
        }
    }

    companion object {
        const val EXTRA_SOURCE_ADDED_CONTRIBUTION = "addedContribution"

        fun newIntent(context: Context, action: Action): Intent {
            return Intent(context, SuggestedEditsCardsActivity::class.java).putExtra(INTENT_EXTRA_ACTION, action)
        }
    }
}
