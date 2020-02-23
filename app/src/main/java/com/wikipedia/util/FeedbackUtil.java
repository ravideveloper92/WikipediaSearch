package com.wikipedia.util;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.snackbar.Snackbar;
import com.wikipedia.analytics.SuggestedEditsFunnel;
import com.wikipedia.main.MainActivity;
import com.wikipedia.page.PageActivity;
import com.wikipedia.random.RandomActivity;
import com.wikipedia.readinglist.ReadingListActivity;
import com.wikipedia.suggestededits.SuggestedEditsCardsActivity;

import com.wikipedia.R;
import com.wikipedia.analytics.SuggestedEditsFunnel;
import com.wikipedia.main.MainActivity;
import com.wikipedia.page.PageActivity;
import com.wikipedia.random.RandomActivity;
import com.wikipedia.readinglist.ReadingListActivity;
import com.wikipedia.suggestededits.SuggestedEditsCardsActivity;

import java.util.concurrent.TimeUnit;

import static com.wikipedia.util.UriUtil.visitInExternalBrowser;

public final class FeedbackUtil {
    public static final int LENGTH_DEFAULT = (int) TimeUnit.SECONDS.toMillis(5);
    public static final int LENGTH_LONG = (int) TimeUnit.SECONDS.toMillis(8);
    private static final int SNACKBAR_MAX_LINES = 10;
    private static View.OnLongClickListener TOOLBAR_LONG_CLICK_LISTENER = (v) -> {
        showToastOverView(v, v.getContentDescription(), LENGTH_DEFAULT);
        return true;
    };

    public static void showError(Activity activity, Throwable e) {
        ThrowableUtil.AppError error = ThrowableUtil.getAppError(activity, e);
        makeSnackbar(activity, error.getError(), LENGTH_DEFAULT).show();
    }

    public static void showMessageAsPlainText(Activity activity, CharSequence possibleHtml) {
        CharSequence richText = StringUtil.fromHtml(possibleHtml.toString());
        showMessage(activity, richText.toString());
    }

    public static void showMessage(Fragment fragment, @StringRes int text) {
        makeSnackbar(fragment.requireActivity(), fragment.getString(text), Snackbar.LENGTH_LONG).show();
    }

    public static void showMessage(Fragment fragment, @NonNull String text) {
        makeSnackbar(fragment.requireActivity(), text, Snackbar.LENGTH_LONG).show();
    }

    public static void showMessage(Activity activity, @StringRes int resId) {
        showMessage(activity, activity.getString(resId), Snackbar.LENGTH_LONG);
    }

    public static void showMessage(Activity activity, CharSequence text) {
        showMessage(activity, text, Snackbar.LENGTH_LONG);
    }

    public static void showMessage(Activity activity, @StringRes int resId, int duration) {
        showMessage(activity, activity.getString(resId), duration);
    }

    public static void showMessage(Activity activity, CharSequence text, int duration) {
        makeSnackbar(activity, text, duration).show();
    }

    public static void showPrivacyPolicy(Context context) {
        UriUtil.visitInExternalBrowser(context, Uri.parse(context.getString(R.string.privacy_policy_url)));
    }

    public static void showOfflineReadingAndData(Context context) {
        UriUtil.visitInExternalBrowser(context, Uri.parse(context.getString(R.string.offline_reading_and_data_url)));
    }

    public static void showAboutWikipedia(Context context) {
        UriUtil.visitInExternalBrowser(context, Uri.parse(context.getString(R.string.about_wikipedia_url)));
    }

    public static void showAndroidAppFAQ(Context context) {
        UriUtil.visitInExternalBrowser(context, Uri.parse(context.getString(R.string.android_app_faq_url)));
    }

    public static void showAndroidAppRequestAnAccount(Context context) {
        UriUtil.visitInExternalBrowser(context, Uri.parse(context.getString(R.string.android_app_request_an_account_url)));
    }

    public static void showAndroidAppEditingFAQ(Context context) {
        showAndroidAppEditingFAQ(context, R.string.android_app_edit_help_url);
    }

    public static void showAndroidAppEditingFAQ(Context context, @StringRes int urlStr) {
        SuggestedEditsFunnel.get().helpOpened();
        UriUtil.visitInExternalBrowser(context, Uri.parse(context.getString(urlStr)));
    }

    public static void setToolbarButtonLongPressToast(View... views) {
        for (View v : views) {
            v.setOnLongClickListener(TOOLBAR_LONG_CLICK_LISTENER);
        }
    }

    public static void showTapTargetView(@NonNull Activity activity, @NonNull View target,
                                         @StringRes int titleId, @StringRes int descriptionId,
                                         @Nullable TapTargetView.Listener listener) {
        final float tooltipAlpha = 0.9f;
        TapTargetView.showFor(activity,
                TapTarget.forView(target, activity.getString(titleId),
                        activity.getString(descriptionId))
                        .targetCircleColor(ResourceUtil.getThemedAttributeId(activity, R.attr.colorAccent))
                        .outerCircleColor(ResourceUtil.getThemedAttributeId(activity, R.attr.colorAccent))
                        .outerCircleAlpha(tooltipAlpha)
                        .cancelable(true)
                        .transparentTarget(true),
                listener);
    }

    public static Snackbar makeSnackbar(Activity activity, CharSequence text, int duration) {
        final float snackbarLineSpacing = 5.0f;
        View view = findBestView(activity);
        Snackbar snackbar = Snackbar.make(view, StringUtil.fromHtml(text.toString()), duration);
        TextView textView = snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, snackbarLineSpacing, activity.getResources().getDisplayMetrics()), 1.0f);
        textView.setMaxLines(SNACKBAR_MAX_LINES);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        TextView actionView = snackbar.getView().findViewById(R.id.snackbar_action);
        actionView.setTextColor(ResourceUtil.getThemedColor(view.getContext(), R.attr.color_group_52));
        return snackbar;
    }

    public static Toast showToastOverView(View view, CharSequence text, int duration) {
        Toast toast = Toast.makeText(view.getContext(), text, duration);
        View v = LayoutInflater.from(view.getContext()).inflate(R.layout.abc_tooltip, null);
        TextView message = v.findViewById(R.id.message);
        message.setText(text);
        message.setMaxLines(Integer.MAX_VALUE);
        toast.setView(v);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        toast.setGravity(Gravity.TOP | Gravity.START, location[0], location[1]);
        toast.show();
        return toast;
    }

    private static View findBestView(Activity activity) {
        if (activity instanceof MainActivity) {
            return activity.findViewById(R.id.fragment_main_coordinator);
        } else if (activity instanceof PageActivity) {
            return activity.findViewById(R.id.fragment_page_coordinator);
        } else if (activity instanceof RandomActivity) {
            return activity.findViewById(R.id.random_coordinator_layout);
        } else if (activity instanceof ReadingListActivity) {
            return activity.findViewById(R.id.fragment_reading_list_coordinator);
        } else if (activity instanceof SuggestedEditsCardsActivity) {
            return activity.findViewById(R.id.suggestedEditsCardsCoordinator);
        } else {
            return activity.findViewById(android.R.id.content);
        }
    }

    private FeedbackUtil() {
    }
}