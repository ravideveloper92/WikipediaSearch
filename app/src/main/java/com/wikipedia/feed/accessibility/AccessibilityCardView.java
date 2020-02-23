package com.wikipedia.feed.accessibility;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.R;
import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.view.FeedAdapter;
import com.wikipedia.feed.view.FeedCardView;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AccessibilityCardView extends LinearLayout implements FeedCardView<Card> {
    @Nullable private FeedAdapter.Callback callback;

    public AccessibilityCardView(Context context) {
        super(context);
        inflate(getContext(), R.layout.view_card_accessibility, this);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.view_card_accessibility_button_load_more) void onLoadMoreClick() {
        if (callback != null) {
            callback.onRequestMore();
        }
    }

    @Override public void setCallback(@Nullable FeedAdapter.Callback callback) {
        this.callback = callback;
    }

    @Override public void setCard(@NonNull Card card) { }
    @Override @Nullable public Card getCard() {
        return null;
    }
}
