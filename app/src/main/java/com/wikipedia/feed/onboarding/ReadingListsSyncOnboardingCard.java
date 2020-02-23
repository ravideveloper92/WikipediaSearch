package com.wikipedia.feed.onboarding;


import com.wikipedia.feed.announcement.Announcement;

import androidx.annotation.NonNull;

import com.wikipedia.R;
import com.wikipedia.auth.AccountUtil;
import com.wikipedia.feed.announcement.Announcement;
import com.wikipedia.feed.model.CardType;

public class ReadingListsSyncOnboardingCard extends OnboardingCard {
    public ReadingListsSyncOnboardingCard(@NonNull Announcement announcement) {
        super(announcement);
    }

    @NonNull @Override public CardType type() {
        return CardType.ONBOARDING_READING_LIST_SYNC;
    }

    public boolean shouldShow() {
        return super.shouldShow() && !AccountUtil.isLoggedIn();
    }

    @Override
    public int prefKey() {
        return R.string.preference_key_feed_readinglists_sync_onboarding_card_enabled;
    }
}
