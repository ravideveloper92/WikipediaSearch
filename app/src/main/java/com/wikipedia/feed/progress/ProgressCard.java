package com.wikipedia.feed.progress;

import androidx.annotation.NonNull;

import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.model.CardType;

public class ProgressCard extends Card {
    @NonNull @Override public CardType type() {
        return CardType.PROGRESS;
    }
}
