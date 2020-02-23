package com.wikipedia.feed.announcement;

import androidx.annotation.NonNull;

import com.wikipedia.feed.model.CardType;

public class FundraisingCard extends AnnouncementCard {

    public FundraisingCard(@NonNull Announcement announcement) {
        super(announcement);
    }

    @NonNull @Override public CardType type() {
        return CardType.FUNDRAISING;
    }
}
