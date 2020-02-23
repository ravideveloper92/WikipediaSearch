package com.wikipedia.feed.onboarding;

import com.wikipedia.feed.announcement.Announcement;

import androidx.annotation.NonNull;

import com.wikipedia.R;
import com.wikipedia.WikipediaApp;
import com.wikipedia.feed.announcement.Announcement;
import com.wikipedia.feed.model.CardType;

public class CustomizeOnboardingCard extends OnboardingCard {
    public CustomizeOnboardingCard(@NonNull Announcement announcement) {
        super(announcement);
    }

    @NonNull @Override public CardType type() {
        return CardType.ONBOARDING_CUSTOMIZE_FEED;
    }

    public boolean shouldShow() {
        return super.shouldShow() && WikipediaApp.getInstance().isOnline();
    }

    @Override public int prefKey() {
        return R.string.preference_key_feed_customize_onboarding_card_enabled;
    }
}
