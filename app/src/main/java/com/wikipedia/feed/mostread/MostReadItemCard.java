package com.wikipedia.feed.mostread;

import android.net.Uri;
import android.text.TextUtils;

import com.wikipedia.page.PageTitle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.dataclient.page.PageSummary;
import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.model.CardType;
import com.wikipedia.page.PageTitle;

public class MostReadItemCard extends Card {
    @NonNull private final PageSummary page;
    @NonNull private final WikiSite wiki;

    MostReadItemCard(@NonNull PageSummary page, @NonNull WikiSite wiki) {
        this.page = page;
        this.wiki = wiki;
    }

    @NonNull @Override public String title() {
        return page.getDisplayTitle();
    }

    @Nullable @Override public String subtitle() {
        return page.getDescription();
    }

    @Nullable @Override public Uri image() {
        String thumbUrl = page.getThumbnailUrl();
        return thumbUrl != null ? Uri.parse(thumbUrl) : null;
    }

    @NonNull @Override public CardType type() {
        return CardType.MOST_READ_ITEM;
    }

    @NonNull public PageTitle pageTitle() {
        PageTitle title = new PageTitle(page.getApiTitle(), wiki);
        if (page.getThumbnailUrl() != null) {
            title.setThumbUrl(page.getThumbnailUrl());
        }
        if (!TextUtils.isEmpty(page.getDescription())) {
            title.setDescription(page.getDescription());
        }
        return title;
    }
}
