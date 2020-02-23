package com.wikipedia.feed.mostread;

import com.wikipedia.json.annotations.Required;

import androidx.annotation.NonNull;

import com.wikipedia.dataclient.page.PageSummary;
import com.wikipedia.json.annotations.Required;

import java.util.Date;
import java.util.List;

public final class MostReadArticles {
    @SuppressWarnings("unused,NullableProblems") @Required
    @NonNull private Date date;
    @SuppressWarnings("unused,NullableProblems") @Required @NonNull private List<PageSummary> articles;

    @NonNull public Date date() {
        return date;
    }

    @NonNull public List<PageSummary> articles() {
        return articles;
    }
}
