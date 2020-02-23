package com.wikipedia.onboarding;

import android.content.Context;
import android.content.Intent;

import com.wikipedia.activity.SingleFragmentActivity;

import androidx.annotation.NonNull;

import com.wikipedia.activity.SingleFragmentActivity;
import com.wikipedia.settings.Prefs;

public class InitialOnboardingActivity
        extends SingleFragmentActivity<InitialOnboardingFragment>
        implements InitialOnboardingFragment.Callback {

    @NonNull public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, InitialOnboardingActivity.class);
    }

    @Override public void onComplete() {
        setResult(RESULT_OK);
        Prefs.setInitialOnboardingEnabled(false);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (getFragment().onBackPressed()) {
            return;
        }
        setResult(RESULT_OK);
        finish();
    }

    @Override protected InitialOnboardingFragment createFragment() {
        return InitialOnboardingFragment.newInstance();
    }
}
