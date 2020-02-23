package com.wikipedia.feed.progress;

import android.content.Context;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.R;
import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.view.FeedAdapter;
import com.wikipedia.feed.view.FeedCardView;

public class ProgressCardView extends FrameLayout implements FeedCardView<Card> {
    public ProgressCardView(Context context) {
        super(context);
        inflate(getContext(), R.layout.view_card_progress, this);
    }

    @Override public void setCard(@NonNull Card card) { }
    @Override public Card getCard() {
        return null;
    }
    @Override public void setCallback(@Nullable FeedAdapter.Callback callback) { }
}
