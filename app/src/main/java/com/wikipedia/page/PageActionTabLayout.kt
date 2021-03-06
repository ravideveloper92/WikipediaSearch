package com.wikipedia.page

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.wikipedia.R
import com.wikipedia.page.action.PageActionTab
import com.wikipedia.util.FeedbackUtil
import com.wikipedia.views.ConfigurableTabLayout

class PageActionTabLayout constructor(context: Context, attrs: AttributeSet? = null) : ConfigurableTabLayout(context, attrs) {
    init {
        View.inflate(getContext(), R.layout.view_article_tab_layout, this)
    }

    fun setPageActionTabsCallback(pageActionTabsCallback: com.wikipedia.page.action.PageActionTab.Callback) {
        for (i in 0 until childCount) {
            val tab = getChildAt(i)
            if (tab.tag != null) {
                val tabPosition = Integer.valueOf((tab.tag as String))
                tab.setOnClickListener { v: View? ->
                    if (isEnabled(v!!)) {
                        com.wikipedia.page.action.PageActionTab.of(tabPosition).select(pageActionTabsCallback)
                    }
                }
            }
            com.wikipedia.util.FeedbackUtil.setToolbarButtonLongPressToast(tab)
        }
    }
}