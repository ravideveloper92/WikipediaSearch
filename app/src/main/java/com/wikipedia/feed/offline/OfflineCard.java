package com.wikipedia.feed.offline;

import androidx.annotation.NonNull;

import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.model.CardType;

public class OfflineCard extends Card {
    @NonNull @Override public CardType type() {
        return CardType.OFFLINE;
    }
}
