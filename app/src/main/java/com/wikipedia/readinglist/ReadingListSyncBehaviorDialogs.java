package com.wikipedia.readinglist;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.wikipedia.WikipediaApp;
import com.wikipedia.page.LinkMovementMethodExt;
import com.wikipedia.readinglist.database.ReadingListDbHelper;
import com.wikipedia.readinglist.sync.ReadingListSyncAdapter;
import com.wikipedia.util.StringUtil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.wikipedia.R;
import com.wikipedia.WikipediaApp;
import com.wikipedia.analytics.LoginFunnel;
import com.wikipedia.events.ReadingListsEnableSyncStatusEvent;
import com.wikipedia.login.LoginActivity;
import com.wikipedia.page.LinkMovementMethodExt;
import com.wikipedia.readinglist.database.ReadingListDbHelper;
import com.wikipedia.readinglist.sync.ReadingListSyncAdapter;
import com.wikipedia.savedpages.SavedPageSyncService;
import com.wikipedia.settings.Prefs;
import com.wikipedia.settings.SettingsActivity;
import com.wikipedia.util.FeedbackUtil;
import com.wikipedia.util.StringUtil;

public final class ReadingListSyncBehaviorDialogs {

    private static boolean PROMPT_LOGIN_TO_SYNC_DIALOG_SHOWING = false;

    public static void detectedRemoteTornDownDialog(@NonNull Activity activity) {
        new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(R.string.reading_list_turned_sync_off_dialog_title)
                .setMessage(R.string.reading_list_turned_sync_off_dialog_text)
                .setPositiveButton(R.string.reading_list_turned_sync_off_dialog_ok, null)
                .setNegativeButton(R.string.reading_list_turned_sync_off_dialog_settings,
                        (dialogInterface, i) -> {
                            activity.startActivity(SettingsActivity.newIntent(activity));
                        })
                .show();
    }

    public static void promptEnableSyncDialog(@NonNull Activity activity) {
        if (!Prefs.shouldShowReadingListSyncEnablePrompt()) {
            return;
        }
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_with_checkbox, null);
        TextView message = view.findViewById(R.id.dialog_message);
        CheckBox checkbox = view.findViewById(R.id.dialog_checkbox);
        message.setText(StringUtil.fromHtml(activity.getString(R.string.reading_list_prompt_turned_sync_on_dialog_text)));
        message.setMovementMethod(new LinkMovementMethodExt(
                (@NonNull String url) -> FeedbackUtil.showAndroidAppFAQ(activity)));
        new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(R.string.reading_list_prompt_turned_sync_on_dialog_title)
                .setView(view)
                .setPositiveButton(R.string.reading_list_prompt_turned_sync_on_dialog_enable_syncing,
                        (dialogInterface, i) -> {
                            Prefs.shouldShowReadingListSyncMergePrompt(true);
                            ReadingListSyncAdapter.setSyncEnabledWithSetup();
                        })
                .setNegativeButton(R.string.reading_list_prompt_turned_sync_on_dialog_no_thanks, null)
                .setOnDismissListener((dialog) -> {
                    Prefs.shouldShowReadingListSyncEnablePrompt(!checkbox.isChecked());
                    WikipediaApp.getInstance().getBus().post(new ReadingListsEnableSyncStatusEvent());
                })
                .show();
    }

    static void promptLogInToSyncDialog(@NonNull Activity activity) {
        if (!Prefs.shouldShowReadingListSyncEnablePrompt() || PROMPT_LOGIN_TO_SYNC_DIALOG_SHOWING) {
            return;
        }
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_with_checkbox, null);
        TextView message = view.findViewById(R.id.dialog_message);
        CheckBox checkbox = view.findViewById(R.id.dialog_checkbox);
        message.setText(StringUtil.fromHtml(activity.getString(R.string.reading_lists_login_reminder_text_with_link)));
        message.setMovementMethod(new LinkMovementMethodExt(
                (@NonNull String url) -> FeedbackUtil.showAndroidAppFAQ(activity)));
        new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(R.string.reading_list_login_reminder_title)
                .setView(view)
                .setPositiveButton(R.string.reading_list_preference_login_or_signup_to_enable_sync_dialog_login,
                        (dialogInterface, i) -> {
                            Intent loginIntent = LoginActivity.newIntent(activity,
                                    LoginFunnel.SOURCE_READING_MANUAL_SYNC);

                            activity.startActivity(loginIntent);
                        })
                .setNegativeButton(R.string.reading_list_prompt_turned_sync_on_dialog_no_thanks, null)
                .setOnDismissListener((dialog) -> {
                    PROMPT_LOGIN_TO_SYNC_DIALOG_SHOWING = false;
                    Prefs.shouldShowReadingListSyncEnablePrompt(!checkbox.isChecked());
                })
                .show();
        PROMPT_LOGIN_TO_SYNC_DIALOG_SHOWING = true;
    }

    public static void removeExistingListsOnLogoutDialog(@NonNull Activity activity) {
        new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(R.string.reading_list_logout_option_reminder_dialog_title)
                .setMessage(R.string.reading_list_logout_option_reminder_dialog_text)
                .setPositiveButton(R.string.reading_list_logout_option_reminder_dialog_yes, null)
                .setNegativeButton(R.string.reading_list_logout_option_reminder_dialog_no,
                        (dialogInterface, i) -> {
                            ReadingListDbHelper.instance().resetToDefaults();
                            SavedPageSyncService.sendSyncEvent();
                        })
                .show();
    }

    public static void mergeExistingListsOnLoginDialog(@NonNull Activity activity) {
        new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(R.string.reading_list_login_option_reminder_dialog_title)
                .setMessage(R.string.reading_list_login_option_reminder_dialog_text)
                .setPositiveButton(R.string.reading_list_login_option_reminder_dialog_yes, null)
                .setNegativeButton(R.string.reading_list_login_option_reminder_dialog_no,
                        (dialogInterface, i) -> {
                            ReadingListDbHelper.instance().resetToDefaults();
                            SavedPageSyncService.sendSyncEvent();
                            Prefs.setReadingListsLastSyncTime(null);
                        })
                .setOnDismissListener(dialog -> ReadingListSyncAdapter.manualSyncWithForce())
                .show();
    }

    private ReadingListSyncBehaviorDialogs() {
    }
}
