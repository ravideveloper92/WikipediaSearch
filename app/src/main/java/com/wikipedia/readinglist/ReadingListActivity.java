package com.wikipedia.readinglist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;

import com.wikipedia.activity.SingleFragmentActivity;
import com.wikipedia.readinglist.database.ReadingList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.activity.SingleFragmentActivity;
import com.wikipedia.readinglist.database.ReadingList;
import com.wikipedia.util.ResourceUtil;

public class ReadingListActivity extends SingleFragmentActivity<ReadingListFragment> {
    protected static final String EXTRA_READING_LIST_ID = "readingListId";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    public static Intent newIntent(@NonNull Context context, @NonNull ReadingList list) {
        return new Intent(context, ReadingListActivity.class)
                .putExtra(EXTRA_READING_LIST_ID, list.id());
    }

    @Override
    public ReadingListFragment createFragment() {
        return ReadingListFragment.newInstance(getIntent().getLongExtra(EXTRA_READING_LIST_ID, 0));
    }

    public void updateNavigationBarColor() {
        setNavigationBarColor(ResourceUtil.getThemedColor(this, android.R.attr.windowBackground));
    }
}
