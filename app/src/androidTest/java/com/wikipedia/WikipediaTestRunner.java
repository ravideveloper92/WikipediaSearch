package com.wikipedia;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.wikipedia.espresso.Constants;
import com.wikipedia.espresso.MockInstrumentationInterceptor;
import com.wikipedia.espresso.util.ConfigurationTools;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnitRunner;

import com.wikipedia.R;
import com.wikipedia.dataclient.okhttp.TestStubInterceptor;
import com.wikipedia.espresso.MockInstrumentationInterceptor;
import com.wikipedia.espresso.util.ConfigurationTools;
import com.wikipedia.settings.Prefs;
import com.wikipedia.settings.PrefsIoUtil;
import com.wikipedia.util.log.L;

import java.io.File;

import static com.wikipedia.espresso.Constants.TEST_COMPARISON_OUTPUT_FOLDER;

public class WikipediaTestRunner extends AndroidJUnitRunner {
    @Override
    public void onStart() {
        deviceRequirementsCheck();
        TestStubInterceptor.Companion.setCALLBACK(new MockInstrumentationInterceptor(InstrumentationRegistry.getContext()));
        clearAppInfo();
        disableOnboarding();
        cleanUpComparisonResults();
        super.onStart();
    }

    private void disableOnboarding() {
        // main onboarding screen
        Prefs.setInitialOnboardingEnabled(false);

        // onboarding feed cards
        PrefsIoUtil.setBoolean(R.string.preference_key_feed_readinglists_sync_onboarding_card_enabled, false);
        PrefsIoUtil.setBoolean(R.string.preference_key_toc_tutorial_enabled, false);
        PrefsIoUtil.setBoolean(R.string.preference_key_feed_customize_onboarding_card_enabled, false);
    }

    private void clearAppInfo() {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(WikipediaApp.getInstance());
        prefs.edit().clear().commit();
        WikipediaApp.getInstance().deleteDatabase("wikipedia.db");
        WikipediaApp.getInstance().logOut();
    }

    private void cleanUpComparisonResults() {
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.TEST_COMPARISON_OUTPUT_FOLDER);
        if (folder.exists()) {
            try {
                File[] files = folder.listFiles();
                for (File file : files) {
                    if (file.isFile()) {
                        if (!file.delete()) {
                            throw new RuntimeException("Cannot delete file: " + file.getName() + " while cleaning up");
                        }
                    }
                }
            } catch (Exception e) {
                L.d("Failed to clean up comparison result files: " + e);
            }
        }
    }

    private void deviceRequirementsCheck() {
        new ConfigurationTools(InstrumentationRegistry.getContext())
                .checkDeviceConfigurations();
    }
}

