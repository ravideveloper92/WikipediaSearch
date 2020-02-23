package com.wikipedia.feed.accessibility;

import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.feed.dataclient.DummyClient;
import com.wikipedia.feed.model.Card;

public class AccessibilityCardClient extends DummyClient {
    @Override public Card getNewCard(WikiSite wiki) {
        return new AccessibilityCard();
    }
}
