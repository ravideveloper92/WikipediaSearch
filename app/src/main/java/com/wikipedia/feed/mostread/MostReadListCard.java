package com.wikipedia.feed.mostread;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;

import com.wikipedia.R;
import com.wikipedia.WikipediaApp;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.dataclient.page.PageSummary;
import com.wikipedia.feed.model.CardType;
import com.wikipedia.feed.model.ListCard;
import com.wikipedia.util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MostReadListCard extends ListCard<MostReadItemCard> {
    @NonNull private final MostReadArticles articles;

    public MostReadListCard(@NonNull MostReadArticles articles, @NonNull WikiSite wiki) {
        super(toItems(articles.articles(), wiki), wiki);
        this.articles = articles;
    }

    @NonNull @Override public String title() {
        return getString(R.string.most_read_list_card_title);
    }

    @Nullable @Override public String subtitle() {
        return DateUtil.getFeedCardDateString(articles.date());
    }

    @NonNull @Override public CardType type() {
        return CardType.MOST_READ_LIST;
    }

    @NonNull @VisibleForTesting
    public static List<MostReadItemCard> toItems(@NonNull List<PageSummary> articles,
                                          @NonNull WikiSite wiki) {
        List<MostReadItemCard> cards = new ArrayList<>();
        for (PageSummary article : articles) {
            cards.add(new MostReadItemCard(article, wiki));
        }
        return cards;
    }

    @NonNull private String getString(@StringRes int id, @Nullable Object... formatArgs) {
        return context().getString(id, formatArgs);
    }

    @NonNull private Context context() {
        return WikipediaApp.getInstance();
    }

    @Override
    protected int dismissHashCode() {
        return (int) TimeUnit.MILLISECONDS.toDays(articles.date().getTime()) + wikiSite().hashCode();
    }
}
