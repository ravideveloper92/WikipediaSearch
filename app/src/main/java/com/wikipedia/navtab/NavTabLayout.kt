package com.wikipedia.navtab

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Menu
import android.view.ViewGroup
import android.widget.TextView

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.wikipedia.R

import com.wikipedia.auth.AccountUtil

class NavTabLayout constructor(context: Context, attrs: AttributeSet) : BottomNavigationView(context, attrs) {
    init {
        setTabViews()
    }

    fun setTabViews() {
        menu.clear()
        for (i in 0 until NavTab.size()) {
            val navTab = NavTab.of(i)
            if (!com.wikipedia.auth.AccountUtil.isLoggedIn() && NavTab.SUGGESTED_EDITS === navTab) {
                continue
            }
            menu.add(Menu.NONE, i, i, navTab.text()).setIcon(navTab.icon())
        }
        fixTextStyle()
    }

    private fun fixTextStyle() {
        if (childCount > 0) {
            val menuView = getChildAt(0)
            if ((menuView as ViewGroup).childCount > 0) {
                for (i in 0..menuView.childCount) {
                    val menuItemView = menuView.getChildAt(i)
                    if (menuItemView != null) {
                        val largeLabel = menuItemView.findViewById<TextView>(R.id.largeLabel)
                        largeLabel.ellipsize = TextUtils.TruncateAt.END
                        largeLabel.setPadding(0, paddingTop, 0, paddingBottom)
                        val smallLabel = menuItemView.findViewById<TextView>(R.id.smallLabel)
                        smallLabel.ellipsize = TextUtils.TruncateAt.END
                        smallLabel.setPadding(0, paddingTop, 0, paddingBottom)
                    }
                }
            }
        }
    }
}
