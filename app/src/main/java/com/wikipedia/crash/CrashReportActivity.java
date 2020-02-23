package com.wikipedia.crash;

import android.content.Intent;

import com.wikipedia.activity.SingleFragmentActivity;

import androidx.annotation.Nullable;

import com.wikipedia.activity.SingleFragmentActivity;

public class CrashReportActivity extends SingleFragmentActivity<CrashReportFragment>
        implements CrashReportFragment.Callback {
    @Override
    protected CrashReportFragment createFragment() {
        return CrashReportFragment.newInstance();
    }

    @Override
    public void onStartOver() {
        int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK;
        Intent intent = getLaunchIntent().addFlags(flags);
        startActivity(intent);
        finish();
    }

    @Override
    public void onQuit() {
        finish();
    }

    @Nullable private Intent getLaunchIntent() {
        return getPackageManager().getLaunchIntentForPackage(getPackageName());
    }
}
