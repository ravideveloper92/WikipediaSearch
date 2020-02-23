package com.wikipedia.suggestededits

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.wikipedia.R
import com.wikipedia.activity.BaseActivity
import com.wikipedia.util.ResourceUtil
import kotlinx.android.synthetic.main.activity_suggested_edits_tags_onboarding.*

class SuggestedEditsImageTagsOnboardingActivity : com.wikipedia.activity.BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(com.wikipedia.util.ResourceUtil.getThemedColor(this, R.attr.paper_color))
        setNavigationBarColor(com.wikipedia.util.ResourceUtil.getThemedColor(this, R.attr.paper_color))
        setContentView(R.layout.activity_suggested_edits_tags_onboarding)
        onboarding_done_button.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        setSupportActionBar(onboarding_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SuggestedEditsImageTagsOnboardingActivity::class.java)
        }
    }
}
