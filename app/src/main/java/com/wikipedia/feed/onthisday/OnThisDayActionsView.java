package com.wikipedia.feed.onthisday;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OnThisDayActionsView extends LinearLayout{
    public interface Callback {
        void onAddPageToList();
        void onSharePage();
    }

    @BindView(R.id.on_this_day_item_title) TextView titleView;

    @Nullable
    private Callback callback;

    public OnThisDayActionsView(Context context) {
        super(context);
        init();
    }

    public OnThisDayActionsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OnThisDayActionsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setState(@NonNull String pageTitle) {
        titleView.setText(pageTitle);
    }

    public void setCallback(@Nullable Callback callback) {
        this.callback = callback;
    }

    @OnClick(R.id.on_this_day_item_share) void onShareClick(View view) {
        if (callback != null) {
            callback.onSharePage();
        }
    }

    @OnClick(R.id.on_this_day_item_add_to_list) void onAddPageToListClick(View view) {
        if (callback != null) {
            callback.onAddPageToList();
        }
    }

    private void init() {
        inflate(getContext(), R.layout.view_on_this_day_page_actions, this);
        ButterKnife.bind(this);
        setOrientation(VERTICAL);
    }

}
