package com.wikipedia.suggestededits

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.ImageViewCompat
import kotlinx.android.synthetic.main.view_suggested_edits_task_item.view.*
import com.wikipedia.Constants.MIN_LANGUAGES_TO_UNLOCK_TRANSLATION
import com.wikipedia.R
import com.wikipedia.WikipediaApp
import com.wikipedia.util.ResourceUtil

internal class SuggestedEditsTaskView constructor(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.view_suggested_edits_task_item, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        isClickable = true
        isFocusable = true
        setPadding(resources.getDimension(R.dimen.activity_horizontal_margin).toInt(), 0, resources.getDimension(R.dimen.activity_horizontal_margin).toInt(), 0)
        setBackgroundResource(com.wikipedia.util.ResourceUtil.getThemedAttributeId(context, R.attr.selectableItemBackground))
    }

    private fun updateTranslateActionUI() {
        val color = com.wikipedia.util.ResourceUtil.getThemedColor(context, if (com.wikipedia.WikipediaApp.getInstance().language().appLanguageCodes.size >= MIN_LANGUAGES_TO_UNLOCK_TRANSLATION)
            R.attr.colorAccent else R.attr.material_theme_de_emphasised_color)
        ImageViewCompat.setImageTintList(suggestedEditsTranslateImage, ColorStateList.valueOf(color))
        suggestedEditsTranslateActionText.setTextColor(color)
    }

    fun setUpViews(task: SuggestedEditsTask, callback: Callback?) {
        updateTranslateActionUI()
        taskTitle.text = task.title
        taskDescription.text = task.description
        taskIcon.setImageResource(task.imageDrawable)
        taskTitleNewLabel.visibility = if (task.new) View.VISIBLE else GONE

        this.setOnClickListener {
            if (!task.disabled) {
                callback?.onViewClick(task, false)
            }
        }
        addContainer.setOnClickListener {
            if (!task.disabled) {
                callback?.onViewClick(task, false)
            }
        }
        translateContainer.setOnClickListener {
            if (!task.disabled) {
                callback?.onViewClick(task, true)
            }
        }
        translateContainer.visibility = if (task.translatable) View.VISIBLE else GONE
    }

    interface Callback {
        fun onViewClick(task: SuggestedEditsTask, isTranslate: Boolean)
    }
}
