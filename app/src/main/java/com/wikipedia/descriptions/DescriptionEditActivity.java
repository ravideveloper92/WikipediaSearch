package com.wikipedia.descriptions;

import android.content.Context;
import android.content.Intent;

import com.wikipedia.Constants;
import com.wikipedia.activity.SingleFragmentActivity;
import com.wikipedia.json.GsonMarshaller;
import com.wikipedia.json.GsonUnmarshaller;
import com.wikipedia.page.ExclusiveBottomSheetPresenter;
import com.wikipedia.page.PageActivity;
import com.wikipedia.page.PageTitle;
import com.wikipedia.page.linkpreview.LinkPreviewDialog;
import com.wikipedia.suggestededits.SuggestedEditsSummary;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.R;
import com.wikipedia.activity.SingleFragmentActivity;
import com.wikipedia.analytics.SuggestedEditsFunnel;
import com.wikipedia.history.HistoryEntry;
import com.wikipedia.json.GsonMarshaller;
import com.wikipedia.json.GsonUnmarshaller;
import com.wikipedia.page.ExclusiveBottomSheetPresenter;
import com.wikipedia.page.PageActivity;
import com.wikipedia.page.PageTitle;
import com.wikipedia.page.linkpreview.LinkPreviewDialog;
import com.wikipedia.readinglist.AddToReadingListDialog;
import com.wikipedia.suggestededits.SuggestedEditsSummary;
import com.wikipedia.util.ClipboardUtil;
import com.wikipedia.util.FeedbackUtil;
import com.wikipedia.util.ShareUtil;
import com.wikipedia.views.ImagePreviewDialog;

import static com.wikipedia.Constants.INTENT_EXTRA_ACTION;
import static com.wikipedia.Constants.INTENT_EXTRA_INVOKE_SOURCE;
import static com.wikipedia.Constants.InvokeSource;
import static com.wikipedia.Constants.InvokeSource.LINK_PREVIEW_MENU;
import static com.wikipedia.Constants.InvokeSource.PAGE_ACTIVITY;
import static com.wikipedia.descriptions.DescriptionEditActivity.Action.ADD_CAPTION;
import static com.wikipedia.descriptions.DescriptionEditActivity.Action.TRANSLATE_CAPTION;
import static com.wikipedia.descriptions.DescriptionEditActivity.Action.TRANSLATE_DESCRIPTION;
import static com.wikipedia.util.DeviceUtil.hideSoftKeyboard;

public class DescriptionEditActivity extends SingleFragmentActivity<DescriptionEditFragment>
        implements DescriptionEditFragment.Callback, LinkPreviewDialog.Callback {

    public enum Action {
        ADD_DESCRIPTION,
        TRANSLATE_DESCRIPTION,
        ADD_CAPTION,
        TRANSLATE_CAPTION,
        ADD_IMAGE_TAGS
    }

    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_HIGHLIGHT_TEXT = "highlightText";
    private static final String EXTRA_SOURCE_SUMMARY = "sourceSummary";
    private static final String EXTRA_TARGET_SUMMARY = "targetSummary";
    private Action action;
    private ExclusiveBottomSheetPresenter bottomSheetPresenter = new ExclusiveBottomSheetPresenter();

    public static Intent newIntent(@NonNull Context context,
                                   @NonNull PageTitle title,
                                   @Nullable String highlightText,
                                   @Nullable SuggestedEditsSummary sourceSummary,
                                   @Nullable SuggestedEditsSummary targetSummary,
                                   @NonNull Action action,
                                   @NonNull Constants.InvokeSource invokeSource) {
        return new Intent(context, DescriptionEditActivity.class)
                .putExtra(EXTRA_TITLE, GsonMarshaller.marshal(title))
                .putExtra(EXTRA_HIGHLIGHT_TEXT, highlightText)
                .putExtra(EXTRA_SOURCE_SUMMARY, sourceSummary == null ? null : GsonMarshaller.marshal(sourceSummary))
                .putExtra(EXTRA_TARGET_SUMMARY, targetSummary == null ? null : GsonMarshaller.marshal(targetSummary))
                .putExtra(Constants.INTENT_EXTRA_ACTION, action)
                .putExtra(Constants.INTENT_EXTRA_INVOKE_SOURCE, invokeSource);
    }

    @Override
    public void onDescriptionEditSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBottomBarContainerClicked(@NonNull Action action) {
        SuggestedEditsSummary summary;

        if (action == TRANSLATE_DESCRIPTION) {
            summary = GsonUnmarshaller.unmarshal(SuggestedEditsSummary.class, getIntent().getStringExtra(EXTRA_TARGET_SUMMARY));
        } else {
            summary = GsonUnmarshaller.unmarshal(SuggestedEditsSummary.class, getIntent().getStringExtra(EXTRA_SOURCE_SUMMARY));
        }

        if (action == ADD_CAPTION || action == TRANSLATE_CAPTION) {
            bottomSheetPresenter.show(getSupportFragmentManager(),
                    ImagePreviewDialog.Companion.newInstance(summary, action));
        } else {
            bottomSheetPresenter.show(getSupportFragmentManager(),
                    LinkPreviewDialog.newInstance(new HistoryEntry(summary.getPageTitle(),
                                    getIntent().hasExtra(Constants.INTENT_EXTRA_INVOKE_SOURCE) && getIntent().getSerializableExtra(Constants.INTENT_EXTRA_INVOKE_SOURCE) == Constants.InvokeSource.PAGE_ACTIVITY
                                            ? HistoryEntry.SOURCE_EDIT_DESCRIPTION : HistoryEntry.SOURCE_SUGGESTED_EDITS),
                            null, true));
        }
    }

    public void onLinkPreviewLoadPage(@NonNull PageTitle title, @NonNull HistoryEntry entry, boolean inNewTab) {
        startActivity(PageActivity.newIntentForCurrentTab(this, entry, entry.getTitle()));
    }

    @Override
    public void onLinkPreviewCopyLink(@NonNull PageTitle title) {
        copyLink(title.getCanonicalUri());
    }

    @Override
    public void onLinkPreviewAddToList(@NonNull PageTitle title) {
        bottomSheetPresenter.show(getSupportFragmentManager(),
                AddToReadingListDialog.newInstance(title, Constants.InvokeSource.LINK_PREVIEW_MENU));
    }

    @Override
    public void onLinkPreviewShareLink(@NonNull PageTitle title) {
        ShareUtil.shareText(this, title);
    }

    public void updateStatusBarColor(@ColorInt int color) {
        setStatusBarColor(color);
    }

    public void updateNavigationBarColor(@ColorInt int color) {
        setNavigationBarColor(color);
    }

    private void copyLink(@NonNull String url) {
        ClipboardUtil.setPlainText(this, null, url);
        FeedbackUtil.showMessage(this, R.string.address_copied);
    }

    @Override
    public DescriptionEditFragment createFragment() {
        Constants.InvokeSource invokeSource = (Constants.InvokeSource) getIntent().getSerializableExtra(Constants.INTENT_EXTRA_INVOKE_SOURCE);
        action = (Action) getIntent().getSerializableExtra(Constants.INTENT_EXTRA_ACTION);
        PageTitle title = GsonUnmarshaller.unmarshal(PageTitle.class, getIntent().getStringExtra(EXTRA_TITLE));
        SuggestedEditsFunnel.get().click(title.getDisplayText(), action);

        return DescriptionEditFragment.newInstance(title,
                getIntent().getStringExtra(EXTRA_HIGHLIGHT_TEXT),
                getIntent().getStringExtra(EXTRA_SOURCE_SUMMARY),
                getIntent().getStringExtra(EXTRA_TARGET_SUMMARY),
                action,
                invokeSource);
    }

    @Override
    public void onBackPressed() {
        if (getFragment().editView.showingReviewContent()) {
            getFragment().editView.loadReviewContent(false);
        } else {
            hideSoftKeyboard(this);
            SuggestedEditsFunnel.get().cancel(action);
            super.onBackPressed();
        }
    }
}
