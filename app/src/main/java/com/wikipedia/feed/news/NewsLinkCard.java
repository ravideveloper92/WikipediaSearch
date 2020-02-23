package com.wikipedia.feed.news;

import android.net.Uri;
import android.text.TextUtils;

import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.model.CardType;
import com.wikipedia.page.PageTitle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.dataclient.page.PageSummary;
import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.model.CardType;
import com.wikipedia.page.PageTitle;

import static com.wikipedia.dataclient.Service.PREFERRED_THUMB_SIZE;
import static com.wikipedia.util.ImageUrlUtil.getUrlForSize;

class NewsLinkCard extends Card {
    @NonNull private PageSummary page;
    @NonNull private WikiSite wiki;

    NewsLinkCard(@NonNull PageSummary page, @NonNull WikiSite wiki) {
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
        return thumbUrl != null ? getUrlForSize(Uri.parse(thumbUrl), PREFERRED_THUMB_SIZE) : null;
    }

    @NonNull @Override public CardType type() {
        return CardType.NEWS_ITEM_LINK;
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
