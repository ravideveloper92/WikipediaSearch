package com.wikipedia.feed.searchbar;

import com.wikipedia.feed.dataclient.DummyClient;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.feed.dataclient.DummyClient;
import com.wikipedia.feed.model.Card;

public class SearchClient extends DummyClient {
    @Override public Card getNewCard(WikiSite wiki) {
        return new SearchCard();
    }
}
