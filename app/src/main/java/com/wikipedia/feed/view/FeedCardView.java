package com.wikipedia.feed.view;

import com.wikipedia.feed.model.Card;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.feed.model.Card;

public interface FeedCardView<T extends Card> {
    void setCard(@NonNull T card);
    @Nullable T getCard();
    void setCallback(@Nullable FeedAdapter.Callback callback);
}
