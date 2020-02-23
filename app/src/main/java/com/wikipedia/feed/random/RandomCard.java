package com.wikipedia.feed.random;

import androidx.annotation.NonNull;

import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.model.CardType;

public class RandomCard extends Card {
    @NonNull private WikiSite wiki;

    public RandomCard(@NonNull WikiSite wiki) {
        this.wiki = wiki;
    }

    @NonNull @Override public CardType type() {
        return CardType.RANDOM;
    }

    public WikiSite wikiSite() {
        return wiki;
    }
}
