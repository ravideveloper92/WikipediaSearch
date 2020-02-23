package com.wikipedia.settings;

import android.content.Context;
import android.content.Intent;

import com.wikipedia.activity.SingleFragmentActivity;
import com.wikipedia.activity.SingleFragmentActivity;

public class DeveloperSettingsActivity extends SingleFragmentActivity<DeveloperSettingsFragment> {
    public static Intent newIntent(Context context) {
        return new Intent(context, DeveloperSettingsActivity.class);
    }

    @Override
    public DeveloperSettingsFragment createFragment() {
        return DeveloperSettingsFragment.newInstance();
    }
}
