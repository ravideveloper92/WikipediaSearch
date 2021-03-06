package com.wikipedia.feed.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.wikipedia.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActionFooterView extends ConstraintLayout {
    @BindView(R.id.view_card_action_footer_button) View actionButton;
    @BindView(R.id.view_card_action_footer_button_icon) ImageView actionIcon;
    @BindView(R.id.view_card_action_footer_button_text) TextView actionText;
    @BindView(R.id.view_card_action_footer_share_button) View shareButton;

    public ActionFooterView(Context context) {
        super(context);
        init();
    }

    public ActionFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ActionFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_card_action_footer, this);
        ButterKnife.bind(this);
    }

    public ActionFooterView actionIcon(@DrawableRes int resId) {
        actionIcon.setImageResource(resId);
        return this;
    }

    public void actionIconColor(@ColorRes int color) {
        actionIcon.setColorFilter(ContextCompat.getColor(getContext(), color));
    }

    public ActionFooterView actionText(@StringRes int resId) {
        actionText.setText(getResources().getString(resId));
        return this;
    }

    public void actionTextColor(@ColorRes int color) {
        actionText.setTextColor(ContextCompat.getColor(getContext(), color));
    }

    public ActionFooterView onActionListener(@Nullable OnClickListener listener) {
        actionButton.setOnClickListener(listener);
        return this;
    }

    public ActionFooterView onShareListener(@Nullable OnClickListener listener) {
        shareButton.setOnClickListener(listener);
        return this;
    }
}
