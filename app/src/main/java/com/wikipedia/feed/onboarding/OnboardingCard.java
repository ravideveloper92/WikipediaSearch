package com.wikipedia.feed.onboarding;

import com.wikipedia.feed.announcement.Announcement;
import com.wikipedia.feed.announcement.AnnouncementCard;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.wikipedia.feed.announcement.Announcement;
import com.wikipedia.feed.announcement.AnnouncementCard;
import com.wikipedia.settings.PrefsIoUtil;

public abstract class OnboardingCard extends AnnouncementCard {
    public OnboardingCard(@NonNull Announcement announcement) {
        super(announcement);
    }

    @StringRes public abstract int prefKey();

    public boolean shouldShow() {
        return PrefsIoUtil.getBoolean(prefKey(), true);
    }

    @Override public void onDismiss() {
        PrefsIoUtil.setBoolean(prefKey(), false);
    }

    @Override public void onRestore() {
        PrefsIoUtil.setBoolean(prefKey(), true);
    }
}
