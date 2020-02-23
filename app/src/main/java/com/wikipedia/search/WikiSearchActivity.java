package com.wikipedia.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.Constants.InvokeSource;
import com.wikipedia.WikipediaApp;
import com.wikipedia.activity.SingleFragmentActivity;
import com.wikipedia.analytics.IntentFunnel;
import com.wikipedia.util.ResourceUtil;

import static com.wikipedia.Constants.INTENT_EXTRA_INVOKE_SOURCE;
import static com.wikipedia.Constants.InvokeSource.WIDGET;

public class WikiSearchActivity extends SingleFragmentActivity<SearchFragment> {
    static final String QUERY_EXTRA = "query";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNavigationBarColor(ResourceUtil.getThemedColor(this, android.R.attr.windowBackground));
    }

    public static Intent newIntent(@NonNull Context context, InvokeSource source, @Nullable String query) {

        if (source == WIDGET) {
            new IntentFunnel(WikipediaApp.getInstance()).logSearchWidgetTap();
        }

        return new Intent(context, WikiSearchActivity.class)
                .putExtra(INTENT_EXTRA_INVOKE_SOURCE, source)
                .putExtra(QUERY_EXTRA, query);
    }

    @Override
    public SearchFragment createFragment() {
        return SearchFragment.newInstance((InvokeSource) getIntent().getSerializableExtra(INTENT_EXTRA_INVOKE_SOURCE),
                getIntent().getStringExtra(QUERY_EXTRA));
    }
}
