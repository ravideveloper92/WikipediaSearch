package com.wikipedia.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.wikipedia.R
import com.wikipedia.activity.SingleFragmentActivity
import com.wikipedia.onboarding.SuggestedEditsOnboardingFragment.Companion.newInstance
import com.wikipedia.util.ResourceUtil

class SuggestedEditsOnboardingActivity : com.wikipedia.activity.SingleFragmentActivity<SuggestedEditsOnboardingFragment>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNavigationBarColor(com.wikipedia.util.ResourceUtil.getThemedColor(this, R.attr.main_toolbar_color))
    }

    override fun createFragment(): SuggestedEditsOnboardingFragment {
        return newInstance()
    }

    companion object {
        @JvmStatic
        fun newIntent(context: Context): Intent {
            return Intent(context, SuggestedEditsOnboardingActivity::class.java)
        }
    }
}