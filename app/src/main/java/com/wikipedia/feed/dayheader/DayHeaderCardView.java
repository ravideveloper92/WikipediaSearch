package com.wikipedia.feed.dayheader;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.R;
import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.view.FeedAdapter;
import com.wikipedia.feed.view.FeedCardView;

public class DayHeaderCardView extends FrameLayout implements FeedCardView<Card> {
    private Card card;
    private TextView dayTextView;

    public DayHeaderCardView(Context context) {
        super(context);
        inflate(getContext(), R.layout.view_card_day_header, this);
        dayTextView = findViewById(R.id.day_header_text);
    }

    @Override public void setCard(@NonNull Card card) {
        this.card = card;
        dayTextView.setText(card.title());
    }

    @Override public Card getCard() {
        return card;
    }

    @Override public void setCallback(@Nullable FeedAdapter.Callback callback) { }
}
