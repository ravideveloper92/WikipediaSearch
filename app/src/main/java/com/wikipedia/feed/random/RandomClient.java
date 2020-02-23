package com.wikipedia.feed.random;

import com.wikipedia.feed.dataclient.DummyClient;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.feed.dataclient.DummyClient;
import com.wikipedia.feed.model.Card;

public class RandomClient extends DummyClient {
    @Override public Card getNewCard(WikiSite wiki) {
        return new RandomCard(wiki);
    }
}
