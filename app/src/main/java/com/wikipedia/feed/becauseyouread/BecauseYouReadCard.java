package com.wikipedia.feed.becauseyouread;

import android.net.Uri;
import android.text.TextUtils;

import com.wikipedia.page.PageTitle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.R;
import com.wikipedia.WikipediaApp;
import com.wikipedia.feed.model.CardType;
import com.wikipedia.feed.model.ListCard;
import com.wikipedia.history.HistoryEntry;
import com.wikipedia.page.PageTitle;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BecauseYouReadCard extends ListCard<BecauseYouReadItemCard> {
    @NonNull private HistoryEntry entry;

    public BecauseYouReadCard(@NonNull final HistoryEntry entry,
                              @NonNull final List<BecauseYouReadItemCard> itemCards) {
        super(itemCards, entry.getTitle().getWikiSite());
        this.entry = entry;
    }

    @Override
    @NonNull
    public String title() {
        return WikipediaApp.getInstance().getString(R.string.view_because_you_read_card_title);
    }

    @Override
    @Nullable
    public Uri image() {
        return TextUtils.isEmpty(entry.getTitle().getThumbUrl()) ? null : Uri.parse(entry.getTitle().getThumbUrl());
    }

    @NonNull @Override public CardType type() {
        return CardType.BECAUSE_YOU_READ_LIST;
    }

    public String pageTitle() {
        return entry.getTitle().getDisplayText();
    }

    @NonNull public PageTitle getPageTitle() {
        return entry.getTitle();
    }

    /** @return The last visit age in days. */
    public long daysOld() {
        long now = System.currentTimeMillis();
        long lastVisited = entry.getTimestamp().getTime();
        return TimeUnit.MILLISECONDS.toDays(now - lastVisited);
    }

    @Override
    protected int dismissHashCode() {
        return entry.getTitle().hashCode();
    }
}
