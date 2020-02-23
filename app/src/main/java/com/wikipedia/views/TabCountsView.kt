package com.wikipedia.views

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import kotlinx.android.synthetic.main.view_tabs_count.view.*
import com.wikipedia.R
import com.wikipedia.WikipediaApp
import com.wikipedia.util.DimenUtil
import com.wikipedia.util.ResourceUtil

class TabCountsView constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    init {
        View.inflate(context, R.layout.view_tabs_count, this)
        layoutParams = ViewGroup.LayoutParams(com.wikipedia.util.DimenUtil.roundedDpToPx(48.0f), ViewGroup.LayoutParams.MATCH_PARENT)
        setBackgroundResource(com.wikipedia.util.ResourceUtil.getThemedAttributeId(context, R.attr.selectableItemBackgroundBorderless))
    }

    fun updateTabCount() {
        val count = com.wikipedia.WikipediaApp.getInstance().tabCount
        tabsCountText.text = count.toString()

        var tabTextSize = TAB_COUNT_TEXT_SIZE_MEDIUM

        if (count > TAB_COUNT_LARGE_NUMBER) {
            tabTextSize = TAB_COUNT_TEXT_SIZE_SMALL
        } else if (count <= TAB_COUNT_SMALL_NUMBER) {
            tabTextSize = TAB_COUNT_TEXT_SIZE_LARGE
        }

        tabsCountText.setTextSize(TypedValue.COMPLEX_UNIT_SP, tabTextSize)
    }

    fun setColor(@ColorInt color: Int) {
        tabsCountText.setTextColor(color)
        tabsCountText.background.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    companion object {
        private const val TAB_COUNT_LARGE_NUMBER = 99f
        private const val TAB_COUNT_SMALL_NUMBER = 9f
        private const val TAB_COUNT_TEXT_SIZE_LARGE = 12f
        private const val TAB_COUNT_TEXT_SIZE_MEDIUM = 10f
        private const val TAB_COUNT_TEXT_SIZE_SMALL = 8f
    }
}
