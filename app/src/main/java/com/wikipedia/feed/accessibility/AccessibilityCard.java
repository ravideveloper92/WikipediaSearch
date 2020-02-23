package com.wikipedia.feed.accessibility;

import androidx.annotation.NonNull;

import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.model.CardType;

public class AccessibilityCard extends Card {
    @NonNull @Override public CardType type() {
        return CardType.ACCESSIBILITY;
    }
}
