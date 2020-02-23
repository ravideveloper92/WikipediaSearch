package com.wikipedia.feed.searchbar;

import androidx.annotation.NonNull;

import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.model.CardType;

public class SearchCard extends Card {
    @NonNull @Override public CardType type() {
        return CardType.SEARCH_BAR;
    }
}
