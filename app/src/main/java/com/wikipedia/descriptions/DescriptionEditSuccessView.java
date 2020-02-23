package com.wikipedia.descriptions;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.wikipedia.R;
import com.wikipedia.views.AppTextViewWithImages;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DescriptionEditSuccessView extends FrameLayout {
    @BindView(R.id.view_description_edit_success_hint_text) AppTextViewWithImages hintTextView;

    @Nullable private Callback callback;

    public interface Callback {
        void onDismissClick();
    }

    public DescriptionEditSuccessView(Context context) {
        super(context);
        init();
    }

    public DescriptionEditSuccessView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DescriptionEditSuccessView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCallback(@Nullable Callback callback) {
        this.callback = callback;
    }

    private void init() {
        inflate(getContext(), R.layout.view_description_edit_success, this);
        ButterKnife.bind(this);
        setHintText();
    }

    private void setHintText() {
        String editHint = getResources().getString(R.string.description_edit_success_article_edit_hint);
        hintTextView.setTextWithDrawables(editHint, R.drawable.ic_mode_edit_white_24dp);
    }

    @OnClick(R.id.view_description_edit_success_done_button) void onDismissClick() {
        if (callback != null) {
            callback.onDismissClick();
        }
    }
}
