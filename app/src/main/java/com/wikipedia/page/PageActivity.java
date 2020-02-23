package com.wikipedia.page;

import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.wikipedia.page.linkpreview.LinkPreviewDialog;
import com.wikipedia.page.references.ReferenceListDialog;
import com.wikipedia.page.references.References;
import com.wikipedia.page.tabs.TabActivity;
import com.wikipedia.util.UriUtil;

import org.apache.commons.lang3.StringUtils;
import com.wikipedia.Constants;
import com.wikipedia.Constants.InvokeSource;
import com.wikipedia.R;
import com.wikipedia.WikipediaApp;
import com.wikipedia.activity.BaseActivity;
import com.wikipedia.analytics.IntentFunnel;
import com.wikipedia.analytics.LinkPreviewFunnel;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.descriptions.DescriptionEditRevertHelpView;
import com.wikipedia.events.ArticleSavedOrDeletedEvent;
import com.wikipedia.events.ChangeTextSizeEvent;
import com.wikipedia.gallery.GalleryActivity;
import com.wikipedia.history.HistoryEntry;
import com.wikipedia.language.LangLinksActivity;
import com.wikipedia.main.MainActivity;
import com.wikipedia.navtab.NavTab;
import com.wikipedia.page.linkpreview.LinkPreviewDialog;
import com.wikipedia.page.references.ReferenceListDialog;
import com.wikipedia.page.references.References;
import com.wikipedia.page.tabs.TabActivity;
import com.wikipedia.readinglist.database.ReadingListPage;
import com.wikipedia.search.WikiSearchActivity;
import com.wikipedia.settings.Prefs;
import com.wikipedia.settings.SettingsActivity;
import com.wikipedia.theme.ThemeChooserDialog;
import com.wikipedia.util.AnimationUtil;
import com.wikipedia.util.ClipboardUtil;
import com.wikipedia.util.DeviceUtil;
import com.wikipedia.util.DimenUtil;
import com.wikipedia.util.FeedbackUtil;
import com.wikipedia.util.ShareUtil;
import com.wikipedia.util.ThrowableUtil;
import com.wikipedia.views.ObservableWebView;
import com.wikipedia.views.PageActionOverflowView;
import com.wikipedia.views.TabCountsView;
import com.wikipedia.views.ViewUtil;
import com.wikipedia.widgets.WidgetProviderFeaturedPage;
import com.wikipedia.wiktionary.WiktionaryDialog;

import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

import static com.wikipedia.Constants.INTENT_EXTRA_ACTION;
import static com.wikipedia.Constants.InvokeSource.LINK_PREVIEW_MENU;
import static com.wikipedia.Constants.InvokeSource.TOOLBAR;
import static com.wikipedia.descriptions.DescriptionEditActivity.Action.ADD_CAPTION;
import static com.wikipedia.settings.Prefs.isLinkPreviewEnabled;
import static com.wikipedia.util.UriUtil.visitInExternalBrowser;

public class PageActivity extends BaseActivity implements PageFragment.Callback,
        LinkPreviewDialog.Callback, ThemeChooserDialog.Callback,
        WiktionaryDialog.Callback, ReferenceListDialog.Callback {
    public static final String ACTION_LOAD_IN_NEW_TAB = "com.wikipedia.load_in_new_tab";
    public static final String ACTION_LOAD_IN_CURRENT_TAB = "com.wikipedia.load_in_current_tab";
    public static final String ACTION_LOAD_IN_CURRENT_TAB_SQUASH = "com.wikipedia.load_in_current_tab_squash";
    public static final String ACTION_LOAD_FROM_EXISTING_TAB = "com.wikipedia.load_from_existing_tab";
    public static final String ACTION_CREATE_NEW_TAB = "com.wikipedia.create_new_tab";
    public static final String ACTION_RESUME_READING = "com.wikipedia.resume_reading";
    public static final String EXTRA_PAGETITLE = "com.wikipedia.pagetitle";
    public static final String EXTRA_HISTORYENTRY  = "com.wikipedia.history.historyentry";

    private static final String LANGUAGE_CODE_BUNDLE_KEY = "language";

    public enum TabPosition {
        CURRENT_TAB,
        CURRENT_TAB_SQUASH,
        NEW_TAB_BACKGROUND,
        NEW_TAB_FOREGROUND,
        EXISTING_TAB
    }

    @BindView(R.id.page_progress_bar) ProgressBar progressBar;
    @BindView(R.id.page_toolbar_container) View toolbarContainerView;
    @BindView(R.id.page_toolbar) Toolbar toolbar;
    @BindView(R.id.page_toolbar_button_search) ImageView searchButton;
    @BindView(R.id.page_toolbar_button_tabs) TabCountsView tabsButton;
    @BindView(R.id.page_toolbar_button_show_overflow_menu) ImageView overflowButton;
    @Nullable private Unbinder unbinder;

    private PageFragment pageFragment;

    private WikipediaApp app;
    private Set<ActionMode> currentActionModes = new HashSet<>();
    private CompositeDisposable disposables = new CompositeDisposable();

    private PageToolbarHideHandler toolbarHideHandler;
    private OverflowCallback overflowCallback = new OverflowCallback();

    private ExclusiveBottomSheetPresenter bottomSheetPresenter = new ExclusiveBottomSheetPresenter();

    private DialogInterface.OnDismissListener listDialogDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            pageFragment.updateBookmarkAndMenuOptionsFromDao();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (WikipediaApp) getApplicationContext();
        AnimationUtil.setSharedElementTransitions(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        try {
            setContentView(R.layout.activity_page);
        } catch (Exception e) {
            if ((!TextUtils.isEmpty(e.getMessage()) && e.getMessage().toLowerCase().contains("webview"))
                    || (!TextUtils.isEmpty(ThrowableUtil.getInnermostThrowable(e).getMessage())
                    && ThrowableUtil.getInnermostThrowable(e).getMessage().toLowerCase().contains("webview"))) {
                // If the system failed to inflate our activity because of the WebView (which could
                // be one of several types of exceptions), it likely means that the system WebView
                // is in the process of being updated. In this case, show the user a message and
                // bail immediately.
                Toast.makeText(app, R.string.error_webview_updating, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            throw e;
        }

        unbinder = ButterKnife.bind(this);

        disposables.add(app.getBus().subscribe(new EventBusConsumer()));

        updateProgressBar(false, true, 0);

        pageFragment = (PageFragment) getSupportFragmentManager().findFragmentById(R.id.page_fragment);

        setSupportActionBar(toolbar);
        clearActionBarTitle();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FeedbackUtil.setToolbarButtonLongPressToast(searchButton, tabsButton, overflowButton);

        toolbarHideHandler = new PageToolbarHideHandler(pageFragment, toolbarContainerView, toolbar, tabsButton);

        boolean languageChanged = false;
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("isSearching")) {
                openSearchActivity(TOOLBAR, null);
            }
            String language = savedInstanceState.getString(LANGUAGE_CODE_BUNDLE_KEY);
            languageChanged = !app.getAppOrSystemLanguageCode().equals(language);
        }

        if (languageChanged) {
            app.resetWikiSite();
            loadEmptyPage(TabPosition.EXISTING_TAB);
        }

        if (savedInstanceState == null) {
            // if there's no savedInstanceState, and we're not coming back from a Theme change,
            // then we must have been launched with an Intent, so... handle it!
            handleIntent(getIntent());
        }
    }

    @OnClick(R.id.page_toolbar_button_search)
    public void onSearchButtonClicked() {
        openSearchActivity(TOOLBAR, null);
    }

    @OnClick(R.id.page_toolbar_button_tabs)
    public void onShowTabsButtonClicked() {
        TabActivity.captureFirstTabBitmap(pageFragment.getContainerView());
        startActivityForResult(TabActivity.newIntentFromPageActivity(this), Constants.ACTIVITY_REQUEST_BROWSE_TABS);
    }

    @OnClick(R.id.page_toolbar_button_show_overflow_menu)
    public void onShowOverflowMenuButtonClicked() {
        showOverflowMenu(toolbar.findViewById(R.id.page_toolbar_button_show_overflow_menu));
    }

    public void animateTabsButton() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.tab_list_zoom_enter);
        tabsButton.startAnimation(anim);
        tabsButton.updateTabCount();
    }

    private void finishActionMode() {
        Set<ActionMode> actionModesToFinish = new HashSet<>(currentActionModes);
        for (ActionMode mode : actionModesToFinish) {
            mode.finish();
        }
        currentActionModes.clear();
    }

    public void hideSoftKeyboard() {
        DeviceUtil.hideSoftKeyboard(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!isDestroyed()) {
            tabsButton.updateTabCount();
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (app.haveMainActivity()) {
                    onBackPressed();
                } else {
                    goToMainTab(NavTab.EXPLORE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSearchRequested() {
        openSearchActivity(TOOLBAR, null);
        return true;
    }

    private void goToMainTab(@NonNull NavTab tab) {
        startActivity(MainActivity.newIntent(this)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(Constants.INTENT_RETURN_TO_MAIN, true)
                .putExtra(Constants.INTENT_EXTRA_GO_TO_MAIN_TAB, tab.code()));
        finish();
    }

    /** @return True if the contextual action bar is open. */
    public boolean isCabOpen() {
        return !currentActionModes.isEmpty();
    }

    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(ACTION_RESUME_READING).setClass(context, PageActivity.class);
    }

    @NonNull
    public static Intent newIntentForNewTab(@NonNull Context context) {
        return new Intent(ACTION_CREATE_NEW_TAB)
                .setClass(context, PageActivity.class);
    }

    @NonNull
    public static Intent newIntentForNewTab(@NonNull Context context,
                                            @NonNull HistoryEntry entry,
                                            @NonNull PageTitle title) {
        return new Intent(ACTION_LOAD_IN_NEW_TAB)
                .setClass(context, PageActivity.class)
                .putExtra(EXTRA_HISTORYENTRY, entry)
                .putExtra(EXTRA_PAGETITLE, title);
    }

    public static Intent newIntentForCurrentTab(@NonNull Context context,
                                                @NonNull HistoryEntry entry,
                                                @NonNull PageTitle title) {
        return newIntentForCurrentTab(context, entry, title, true);
    }

    public static Intent newIntentForCurrentTab(@NonNull Context context,
                                                @NonNull HistoryEntry entry,
                                                @NonNull PageTitle title,
                                                boolean squashBackstack) {
        return new Intent(squashBackstack ? ACTION_LOAD_IN_CURRENT_TAB_SQUASH : ACTION_LOAD_IN_CURRENT_TAB)
                .setClass(context, PageActivity.class)
                .putExtra(EXTRA_HISTORYENTRY, entry)
                .putExtra(EXTRA_PAGETITLE, title);
    }

    public static Intent newIntentForExistingTab(@NonNull Context context,
                                                 @NonNull HistoryEntry entry,
                                                 @NonNull PageTitle title) {
        return new Intent(ACTION_LOAD_FROM_EXISTING_TAB)
                .setClass(context, PageActivity.class)
                .putExtra(EXTRA_HISTORYENTRY, entry)
                .putExtra(EXTRA_PAGETITLE, title);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(@NonNull Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            WikiSite wiki = new WikiSite(intent.getData());
            PageTitle title = wiki.titleForUri(intent.getData());
            HistoryEntry historyEntry = new HistoryEntry(title,
                    intent.hasExtra(Constants.INTENT_EXTRA_VIEW_FROM_NOTIFICATION)
                            ? HistoryEntry.SOURCE_NOTIFICATION_SYSTEM : HistoryEntry.SOURCE_EXTERNAL_LINK);
            if (intent.hasExtra(Intent.EXTRA_REFERRER)) {
                // Populate the referrer with the externally-referring URL, e.g. an external Browser URL.
                // This can be a Uri or a String, so let's extract it safely as an Object.
                historyEntry.setReferrer(intent.getExtras().get(Intent.EXTRA_REFERRER).toString());
            }
            if (title.isSpecial()) {
                UriUtil.visitInExternalBrowser(this, intent.getData());
                finish();
                return;
            }
            loadPage(title, historyEntry, TabPosition.NEW_TAB_FOREGROUND);
        } else if ((ACTION_LOAD_IN_NEW_TAB.equals(intent.getAction())
                || ACTION_LOAD_IN_CURRENT_TAB.equals(intent.getAction())
                || ACTION_LOAD_IN_CURRENT_TAB_SQUASH.equals(intent.getAction()))
                && intent.hasExtra(EXTRA_HISTORYENTRY)) {
            PageTitle title = intent.getParcelableExtra(EXTRA_PAGETITLE);
            HistoryEntry historyEntry = intent.getParcelableExtra(EXTRA_HISTORYENTRY);
            if (ACTION_LOAD_IN_NEW_TAB.equals(intent.getAction())) {
                loadPage(title, historyEntry, TabPosition.NEW_TAB_FOREGROUND);
            } else if (ACTION_LOAD_IN_CURRENT_TAB.equals(intent.getAction())) {
                loadPage(title, historyEntry, TabPosition.CURRENT_TAB);
            } else if (ACTION_LOAD_IN_CURRENT_TAB_SQUASH.equals(intent.getAction())) {
                loadPage(title, historyEntry, TabPosition.CURRENT_TAB_SQUASH);
            }
            if (intent.hasExtra(Constants.INTENT_EXTRA_REVERT_QNUMBER)) {
                showDescriptionEditRevertDialog(intent.getStringExtra(Constants.INTENT_EXTRA_REVERT_QNUMBER));
            }
        } else if (ACTION_LOAD_FROM_EXISTING_TAB.equals(intent.getAction())
                && intent.hasExtra(EXTRA_HISTORYENTRY)) {
            PageTitle title = intent.getParcelableExtra(EXTRA_PAGETITLE);
            HistoryEntry historyEntry = intent.getParcelableExtra(EXTRA_HISTORYENTRY);
            loadPage(title, historyEntry, TabPosition.EXISTING_TAB);
        } else if (ACTION_RESUME_READING.equals(intent.getAction())
                || intent.hasExtra(Constants.INTENT_APP_SHORTCUT_CONTINUE_READING)) {
            // do nothing, since this will be handled indirectly by PageFragment.
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            PageTitle title = new PageTitle(query, app.getWikiSite());
            HistoryEntry historyEntry = new HistoryEntry(title, HistoryEntry.SOURCE_SEARCH);
            loadPage(title, historyEntry, TabPosition.EXISTING_TAB);
        } else if (intent.hasExtra(Constants.INTENT_FEATURED_ARTICLE_FROM_WIDGET)) {
            new IntentFunnel(app).logFeaturedArticleWidgetTap();
            PageTitle title = intent.getParcelableExtra(EXTRA_PAGETITLE);
            HistoryEntry historyEntry = new HistoryEntry(title, HistoryEntry.SOURCE_WIDGET);
            loadPage(title, historyEntry, TabPosition.EXISTING_TAB);
        } else if (ACTION_CREATE_NEW_TAB.equals(intent.getAction())) {
            loadEmptyPage(TabPosition.NEW_TAB_FOREGROUND);
        } else {
            loadEmptyPage(TabPosition.CURRENT_TAB);
        }
    }

    /**
     * Update the state of the main progress bar that is shown inside the ActionBar of the activity.
     * @param visible Whether the progress bar is visible.
     * @param indeterminate Whether the progress bar is indeterminate.
     * @param value Value of the progress bar (may be between 0 and 10000). Ignored if the
     *              progress bar is indeterminate.
     */
    public void updateProgressBar(boolean visible, boolean indeterminate, int value) {
        progressBar.setIndeterminate(indeterminate);
        if (!indeterminate) {
            progressBar.setProgress(value);
        }
        progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Load a new page, and put it on top of the backstack, optionally allowing state loss of the
     * fragment manager. Useful for when this function is called from an AsyncTask result.
     * @param title Title of the page to load.
     * @param entry HistoryEntry associated with this page.
     * @param position Whether to open this page in the current tab, a new background tab, or new
     *                 foreground tab.
     */
    public void loadPage(@NonNull final PageTitle title,
                         @NonNull final HistoryEntry entry,
                         @NonNull final TabPosition position) {
        if (isDestroyed()) {
            return;
        }

        if (entry.getSource() != HistoryEntry.SOURCE_INTERNAL_LINK || !isLinkPreviewEnabled()) {
            new LinkPreviewFunnel(app, entry.getSource()).logNavigate();
        }

        app.putCrashReportProperty("api", title.getWikiSite().authority());
        app.putCrashReportProperty("title", title.toString());

        if (title.isSpecial()) {
            UriUtil.visitInExternalBrowser(this, Uri.parse(title.getMobileUri()));
            return;
        }

        toolbarContainerView.post(() -> {
            if (!pageFragment.isAdded()) {
                return;
            }
            // Close the link preview, if one is open.
            hideLinkPreview();

            pageFragment.closeFindInPage();
            if (position == TabPosition.CURRENT_TAB) {
                pageFragment.loadPage(title, entry, true, false);
            } else if (position == TabPosition.CURRENT_TAB_SQUASH) {
                pageFragment.loadPage(title, entry, true, true);
            } else if (position == TabPosition.NEW_TAB_BACKGROUND) {
                pageFragment.openInNewBackgroundTab(title, entry);
            } else if (position == TabPosition.NEW_TAB_FOREGROUND) {
                pageFragment.openInNewForegroundTab(title, entry);
            } else {
                pageFragment.openFromExistingTab(title, entry);
            }
            app.getSessionFunnel().pageViewed(entry);
        });
    }

    public void loadEmptyPage(TabPosition position) {
        PageTitle title = new PageTitle(Constants.EMPTY_PAGE_TITLE, app.getWikiSite());
        HistoryEntry historyEntry = new HistoryEntry(title, HistoryEntry.SOURCE_INTERNAL_LINK);
        loadPage(title, historyEntry, position);
    }

    private void hideLinkPreview() {
        bottomSheetPresenter.dismiss(getSupportFragmentManager());
    }

    public void showAddToListDialog(@NonNull PageTitle title, @NonNull InvokeSource source) {
        bottomSheetPresenter.showAddToListDialog(getSupportFragmentManager(), title, source, listDialogDismissListener);
    }

    // Note: back button first handled in {@link #onOptionsItemSelected()};
    @Override
    public void onBackPressed() {
        if (isCabOpen()) {
            finishActionMode();
            return;
        }

        app.getSessionFunnel().backPressed();
        if (pageFragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onPageShowBottomSheet(@NonNull BottomSheetDialog dialog) {
        bottomSheetPresenter.show(getSupportFragmentManager(), dialog);
    }

    @Override
    public void onPageShowBottomSheet(@NonNull BottomSheetDialogFragment dialog) {
        bottomSheetPresenter.show(getSupportFragmentManager(), dialog);
    }

    @Override
    public void onPageDismissBottomSheet() {
        bottomSheetPresenter.dismiss(getSupportFragmentManager());
    }

    @Override
    public void onPageInitWebView(@NonNull ObservableWebView webView) {
        toolbarHideHandler.setScrollView(webView);
    }

    @Override
    public void onPageLoadPage(@NonNull PageTitle title, @NonNull HistoryEntry entry) {
        loadPage(title, entry, TabPosition.CURRENT_TAB);
    }

    @Override
    public void onPageShowLinkPreview(@NonNull HistoryEntry entry) {
        bottomSheetPresenter.show(getSupportFragmentManager(),
                LinkPreviewDialog.newInstance(entry, null));
    }

    @Override
    public void onPageLoadEmptyPageInForegroundTab() {
        loadEmptyPage(TabPosition.EXISTING_TAB);
    }

    @Override
    public void onPageUpdateProgressBar(boolean visible, boolean indeterminate, int value) {
        updateProgressBar(visible, indeterminate, value);
    }

    @Override
    public void onPageShowThemeChooser() {
        bottomSheetPresenter.show(getSupportFragmentManager(), new ThemeChooserDialog());
    }

    @Override
    public void onPageStartSupportActionMode(@NonNull ActionMode.Callback callback) {
        startActionMode(callback);
    }

    @Override
    public void onPageHideSoftKeyboard() {
        hideSoftKeyboard();
    }

    @Override
    public void onPageAddToReadingList(@NonNull PageTitle title, @NonNull InvokeSource source) {
        showAddToListDialog(title, source);
    }

    @Override
    public void onPageRemoveFromReadingLists(@NonNull PageTitle title) {
        if (!pageFragment.isAdded()) {
            return;
        }
        FeedbackUtil.showMessage(this,
                getString(R.string.reading_list_item_deleted, title.getDisplayText()));
        pageFragment.updateBookmarkAndMenuOptionsFromDao();
    }

    @Override
    public void onPageLoadError(@NonNull PageTitle title) {
        getSupportActionBar().setTitle(title.getDisplayText());
    }

    @Override
    public void onPageLoadErrorBackPressed() {
        finish();
    }

    @Override
    public void onPageHideAllContent() {
        toolbarHideHandler.setFadeEnabled(false);
    }

    @Override
    public void onPageSetToolbarFadeEnabled(boolean enabled) {
        toolbarHideHandler.setFadeEnabled(enabled);
    }

    @Override
    public void onPageSetToolbarElevationEnabled(boolean enabled) {
        toolbarContainerView.setElevation(DimenUtil.dpToPx(enabled ? DimenUtil.getDimension(R.dimen.toolbar_default_elevation) : 0));
    }

    @Override
    public void onLinkPreviewLoadPage(@NonNull PageTitle title, @NonNull HistoryEntry entry, boolean inNewTab) {
        loadPage(title, entry, inNewTab ? TabPosition.NEW_TAB_BACKGROUND : TabPosition.CURRENT_TAB);
    }

    @Override
    public void onLinkPreviewCopyLink(@NonNull PageTitle title) {
        copyLink(title.getCanonicalUri());
        showCopySuccessMessage();
    }

    @Override
    public void onLinkPreviewAddToList(@NonNull PageTitle title) {
        showAddToListDialog(title, LINK_PREVIEW_MENU);
    }

    @Override
    public void onLinkPreviewShareLink(@NonNull PageTitle title) {
        ShareUtil.shareText(this, title);
    }

    @Override
    public void wiktionaryShowDialogForTerm(@NonNull String term) {
        pageFragment.getShareHandler().showWiktionaryDefinition(term);
    }

    @Override
    public void onToggleDimImages() {
        recreate();
    }

    @Override
    public void onCancel() { }

    @Override @NonNull public LinkHandler getLinkHandler() {
        return pageFragment.getLinkHandler();
    }

    @Override @NonNull public Observable<References> getReferences() {
        return pageFragment.getReferences();
    }

    private void copyLink(@NonNull String url) {
        ClipboardUtil.setPlainText(this, null, url);
    }

    private void showCopySuccessMessage() {
        FeedbackUtil.showMessage(this, R.string.address_copied);
    }

    private void showOverflowMenu(@NonNull View anchor) {
        PageActionOverflowView overflowView = new PageActionOverflowView(this);
        overflowView.show(anchor, overflowCallback, pageFragment.getCurrentTab());
    }

    private class OverflowCallback implements PageActionOverflowView.Callback {
        @Override
        public void forwardClick() {
            pageFragment.goForward();
        }

        @Override
        public void feedClick() {
            goToMainTab(NavTab.EXPLORE);
        }

        @Override
        public void readingListsClick() {
            if (Prefs.getOverflowReadingListsOptionClickCount() < 2) {
                Prefs.setOverflowReadingListsOptionClickCount(Prefs.getOverflowReadingListsOptionClickCount() + 1);
            }
            goToMainTab(NavTab.READING_LISTS);
        }

        @Override
        public void historyClick() {
            goToMainTab(NavTab.HISTORY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.resetWikiSite();
    }

    @Override
    public void onPause() {
        if (isCabOpen()) {
            // Explicitly close any current ActionMode (see T147191)
            finishActionMode();
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LANGUAGE_CODE_BUNDLE_KEY, app.getAppOrSystemLanguageCode());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (newArticleLanguageSelected(requestCode, resultCode) || galleryPageSelected(requestCode, resultCode)) {
            toolbarContainerView.post(() -> handleIntent(data));
        } else if (galleryImageCaptionAdded(requestCode, resultCode)) {
            pageFragment.refreshPage();
        } else if (requestCode == Constants.ACTIVITY_REQUEST_BROWSE_TABS) {
            if (app.getTabCount() == 0 && resultCode != TabActivity.RESULT_NEW_TAB) {
                // They browsed the tabs and cleared all of them, without wanting to open a new tab.
                finish();
                return;
            }
            if (resultCode == TabActivity.RESULT_NEW_TAB) {
                loadEmptyPage(TabPosition.NEW_TAB_FOREGROUND);
                animateTabsButton();
            } else if (resultCode == TabActivity.RESULT_LOAD_FROM_BACKSTACK) {
                pageFragment.reloadFromBackstack();
            }
        } else if (requestCode == Constants.ACTIVITY_REQUEST_IMAGE_CAPTION_EDIT
                && resultCode == RESULT_OK) {
            pageFragment.refreshPage();
            String editLanguage = StringUtils.defaultString(pageFragment.getLeadImageEditLang(), app.language().getAppLanguageCode());
            FeedbackUtil.makeSnackbar(this, data.getSerializableExtra(INTENT_EXTRA_ACTION) == ADD_CAPTION
                    ? getString(R.string.description_edit_success_saved_image_caption_snackbar)
                    : getString(R.string.description_edit_success_saved_image_caption_in_lang_snackbar, app.language().getAppLanguageLocalizedName(editLanguage)), FeedbackUtil.LENGTH_DEFAULT)
                    .setAction(R.string.suggested_edits_article_cta_snackbar_action, v -> pageFragment.openImageInGallery(editLanguage)).show();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        disposables.clear();
        Prefs.setHasVisitedArticlePage(true);
        super.onDestroy();
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        if (!isCabOpen() && mode.getTag() == null) {
            Menu menu = mode.getMenu();
            menu.clear();
            mode.getMenuInflater().inflate(R.menu.menu_text_select, menu);
            ViewUtil.setCloseButtonInActionMode(pageFragment.requireContext(), mode);
            pageFragment.onActionModeShown(mode);
        }
        currentActionModes.add(mode);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        currentActionModes.remove(mode);
        toolbarHideHandler.onScrolled(pageFragment.getWebView().getScrollY(),
                pageFragment.getWebView().getScrollY());
    }

    protected void clearActionBarTitle() {
        getSupportActionBar().setTitle("");
    }

    private boolean newArticleLanguageSelected(int requestCode, int resultCode) {
        return requestCode == Constants.ACTIVITY_REQUEST_LANGLINKS && resultCode == LangLinksActivity.ACTIVITY_RESULT_LANGLINK_SELECT;
    }

    private boolean galleryPageSelected(int requestCode, int resultCode) {
        return requestCode == Constants.ACTIVITY_REQUEST_GALLERY && resultCode == GalleryActivity.ACTIVITY_RESULT_PAGE_SELECTED;
    }

    private boolean galleryImageCaptionAdded(int requestCode, int resultCode) {
        return requestCode == Constants.ACTIVITY_REQUEST_GALLERY && resultCode == GalleryActivity.ACTIVITY_RESULT_IMAGE_CAPTION_ADDED;
    }

    private boolean languageChanged(int resultCode) {
        return resultCode == SettingsActivity.ACTIVITY_RESULT_LANGUAGE_CHANGED;
    }

    /**
     * Update any instances of our Featured Page widget, since it will change with the currently selected language.
     */
    private void updateFeaturedPageWidget() {
        Intent widgetIntent = new Intent(this, WidgetProviderFeaturedPage.class);
        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(
                new ComponentName(this, WidgetProviderFeaturedPage.class));
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(widgetIntent);
    }

    private void showDescriptionEditRevertDialog(@NonNull String qNumber) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.notification_reverted_title)
                .setView(new DescriptionEditRevertHelpView(this, qNumber))
                .setPositiveButton(R.string.reverted_edit_dialog_ok_button_text, null)
                .create()
                .show();
    }

    private void openSearchActivity(@NonNull InvokeSource source, @Nullable String query) {
        Intent intent = WikiSearchActivity.newIntent(this, source, query);
        startActivity(intent);
    }

    private class EventBusConsumer implements Consumer<Object> {
        @Override
        public void accept(Object event) {
            if (event instanceof ChangeTextSizeEvent) {
                if (pageFragment != null && pageFragment.getWebView() != null) {
                    pageFragment.updateFontSize();
                }
            } else if (event instanceof ArticleSavedOrDeletedEvent) {
                if (((ArticleSavedOrDeletedEvent) event).isAdded()) {
                    Prefs.shouldShowBookmarkToolTip(false);
                }
                if (pageFragment == null || !pageFragment.isAdded() || pageFragment.getTitleOriginal() == null) {
                    return;
                }
                for (ReadingListPage page : ((ArticleSavedOrDeletedEvent) event).getPages()) {
                    if (page.title().equals(pageFragment.getTitleOriginal().getDisplayText())) {
                        pageFragment.updateBookmarkAndMenuOptionsFromDao();
                    }
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_F)
                || (!event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_F3)) {
            pageFragment.showFindInPage();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}