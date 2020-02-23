package com.wikipedia.feed.news;

import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spanned;
import android.text.style.StyleSpan;

import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.model.CardType;
import com.wikipedia.util.StringUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.dataclient.page.PageSummary;
import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.model.CardType;
import com.wikipedia.richtext.RichTextUtil;
import com.wikipedia.util.StringUtil;
import com.wikipedia.util.log.L;

import java.util.List;

public class NewsItemCard extends Card {
    @NonNull private NewsItem newsItem;
    @NonNull private WikiSite wiki;

    NewsItemCard(@NonNull NewsItem item, @NonNull WikiSite wiki) {
        this.newsItem = item;
        this.wiki = wiki;
    }

    @NonNull public NewsItem item() {
        return newsItem;
    }

    @NonNull public WikiSite wikiSite() {
        return wiki;
    }

    @Nullable @Override public Uri image() {
        return newsItem.thumb();
    }

    @NonNull @Override public CardType type() {
        return CardType.NEWS_ITEM;
    }

    @NonNull public CharSequence text() {
        return removeImageCaption(StringUtil.fromHtml(newsItem.story()));
    }

    @NonNull public List<PageSummary> links() {
        return newsItem.links();
    }

    /* Remove the in-Wikitext thumbnail caption, which will almost certainly not apply here */
    @NonNull private CharSequence removeImageCaption(@NonNull Spanned text) {
        Object[] spans = RichTextUtil.getSpans(text, 0, text.length());
        for (Object span : spans) {
            if (span instanceof StyleSpan && ((StyleSpan) span).getStyle() == Typeface.ITALIC) {
                int start = text.getSpanStart(span);
                int end = text.getSpanEnd(span);
                if (text.charAt(start) == '(' && text.charAt(end - 1) == ')') {
                    L.v("Removing spanned text: " + text.subSequence(start, end));
                    return RichTextUtil.remove(text, start, end);
                }
            }
        }
        return text;
    }
}
