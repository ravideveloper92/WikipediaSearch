package com.wikipedia.feed.featured;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.R;
import com.wikipedia.WikipediaApp;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.dataclient.page.PageSummary;
import com.wikipedia.feed.model.CardType;
import com.wikipedia.feed.model.WikiSiteCard;
import com.wikipedia.history.HistoryEntry;
import com.wikipedia.util.DateUtil;

public class FeaturedArticleCard extends WikiSiteCard {
    @NonNull private PageSummary page;
    private int age;

    public FeaturedArticleCard(@NonNull PageSummary page, int age, @NonNull WikiSite wiki) {
        super(wiki);
        this.page = page;
        this.age = age;
    }

    @Override
    @NonNull
    public String title() {
        return WikipediaApp.getInstance().getString(R.string.view_featured_article_card_title);
    }

    @Override
    @NonNull
    public String subtitle() {
        return DateUtil.getFeedCardDateString(age);
    }

    @NonNull
    String articleTitle() {
        return page.getDisplayTitle();
    }

    @Nullable
    String articleSubtitle() {
        return page.getDescription();
    }

    @Override
    @Nullable
    public Uri image() {
        String thumbUrl = page.getThumbnailUrl();
        return thumbUrl != null ? Uri.parse(thumbUrl) : null;
    }

    @Nullable
    @Override
    public String extract() {
        return page.getExtractHtml();
    }

    @NonNull @Override public CardType type() {
        return CardType.FEATURED_ARTICLE;
    }

    @NonNull
    public HistoryEntry historyEntry(int source) {
        return new HistoryEntry(page.getPageTitle(wikiSite()), source);
    }

    @Override
    protected int dismissHashCode() {
        return page.getApiTitle().hashCode();
    }
}
