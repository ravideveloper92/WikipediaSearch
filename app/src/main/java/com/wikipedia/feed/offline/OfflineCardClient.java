package com.wikipedia.feed.offline;

import com.wikipedia.feed.dataclient.DummyClient;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.feed.dataclient.DummyClient;
import com.wikipedia.feed.model.Card;

public class OfflineCardClient extends DummyClient {
    @Override public Card getNewCard(WikiSite wiki) {
        return new OfflineCard();
    }
}
