package com.wikipedia.feed.onthisday;

import android.content.Context;
import android.content.Intent;

import com.wikipedia.activity.SingleFragmentActivity;

import androidx.annotation.NonNull;

import com.wikipedia.Constants.InvokeSource;
import com.wikipedia.activity.SingleFragmentActivity;
import com.wikipedia.dataclient.WikiSite;

import static com.wikipedia.Constants.INTENT_EXTRA_INVOKE_SOURCE;

public class OnThisDayActivity extends SingleFragmentActivity<OnThisDayFragment> {
    public static final String AGE = "age";
    public static final String WIKISITE = "wikisite";

    public static Intent newIntent(@NonNull Context context, int age, WikiSite wikiSite, InvokeSource invokeSource) {
        return new Intent(context, OnThisDayActivity.class)
                .putExtra(AGE, age)
                .putExtra(WIKISITE, wikiSite)
                .putExtra(INTENT_EXTRA_INVOKE_SOURCE, invokeSource);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getFragment().onBackPressed();
    }

    @Override
    protected OnThisDayFragment createFragment() {
        return OnThisDayFragment.newInstance(getIntent().getIntExtra(AGE, 0), getIntent().getParcelableExtra(WIKISITE));
    }
}
