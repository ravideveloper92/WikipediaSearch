package com.wikipedia.feed.announcement;

import androidx.annotation.NonNull;

import com.wikipedia.feed.model.CardType;

public class SurveyCard extends AnnouncementCard {

    public SurveyCard(@NonNull Announcement announcement) {
        super(announcement);
    }

    @NonNull @Override public CardType type() {
        return CardType.SURVEY;
    }
}
