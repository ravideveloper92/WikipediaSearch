package com.wikipedia.main;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.wikipedia.page.ExclusiveBottomSheetPresenter;
import com.wikipedia.page.PageActivity;
import com.wikipedia.page.PageTitle;
import com.wikipedia.page.linkpreview.LinkPreviewDialog;
import com.wikipedia.page.tabs.TabActivity;
import com.wikipedia.search.SearchFragment;
import com.wikipedia.search.WikiSearchActivity;

import com.wikipedia.BackPressedHandler;
import com.wikipedia.Constants;
import com.wikipedia.R;
import com.wikipedia.WikipediaApp;
import com.wikipedia.activity.FragmentUtil;
import com.wikipedia.analytics.GalleryFunnel;
import com.wikipedia.analytics.LoginFunnel;
import com.wikipedia.auth.AccountUtil;
import com.wikipedia.events.LoggedOutInBackgroundEvent;
import com.wikipedia.feed.FeedFragment;
import com.wikipedia.feed.image.FeaturedImage;
import com.wikipedia.feed.image.FeaturedImageCard;
import com.wikipedia.feed.news.NewsActivity;
import com.wikipedia.feed.news.NewsItemCard;
import com.wikipedia.feed.view.HorizontalScrollingListCardItemView;
import com.wikipedia.gallery.GalleryActivity;
import com.wikipedia.gallery.ImagePipelineBitmapGetter;
import com.wikipedia.gallery.MediaDownloadReceiver;
import com.wikipedia.history.HistoryEntry;
import com.wikipedia.history.HistoryFragment;
import com.wikipedia.login.LoginActivity;
import com.wikipedia.navtab.NavTab;
import com.wikipedia.navtab.NavTabFragmentPagerAdapter;
import com.wikipedia.navtab.NavTabLayout;
import com.wikipedia.navtab.NavTabOverlayLayout;
import com.wikipedia.page.ExclusiveBottomSheetPresenter;
import com.wikipedia.page.PageActivity;
import com.wikipedia.page.PageTitle;
import com.wikipedia.page.linkpreview.LinkPreviewDialog;
import com.wikipedia.page.tabs.TabActivity;
import com.wikipedia.random.RandomActivity;
import com.wikipedia.readinglist.AddToReadingListDialog;
import com.wikipedia.search.WikiSearchActivity;
import com.wikipedia.search.SearchFragment;
import com.wikipedia.settings.Prefs;
import com.wikipedia.settings.SettingsActivity;
import com.wikipedia.suggestededits.SuggestedEditsTasksFragment;
import com.wikipedia.util.ClipboardUtil;
import com.wikipedia.util.FeedbackUtil;
import com.wikipedia.util.PermissionUtil;
import com.wikipedia.util.ShareUtil;
import com.wikipedia.util.log.L;

import java.io.File;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.wikipedia.Constants.ACTIVITY_REQUEST_OPEN_SEARCH_ACTIVITY;
import static com.wikipedia.Constants.InvokeSource.APP_SHORTCUTS;
import static com.wikipedia.Constants.InvokeSource.FEED;
import static com.wikipedia.Constants.InvokeSource.FEED_BAR;
import static com.wikipedia.Constants.InvokeSource.LINK_PREVIEW_MENU;
import static com.wikipedia.Constants.InvokeSource.VOICE;

public class MainFragment extends Fragment implements BackPressedHandler, FeedFragment.Callback,
        HistoryFragment.Callback, LinkPreviewDialog.Callback {
    @BindView(R.id.fragment_main_view_pager) ViewPager2 viewPager;
    @BindView(R.id.fragment_main_nav_tab_container) FrameLayout navTabContainer;
    @BindView(R.id.fragment_main_nav_tab_layout) NavTabLayout tabLayout;
    @BindView(R.id.fragment_main_nav_tab_overlay_layout) NavTabOverlayLayout tabOverlayLayout;
    private Unbinder unbinder;
    private ExclusiveBottomSheetPresenter bottomSheetPresenter = new ExclusiveBottomSheetPresenter();
    private MediaDownloadReceiver downloadReceiver = new MediaDownloadReceiver();
    private MediaDownloadReceiverCallback downloadReceiverCallback = new MediaDownloadReceiverCallback();
    private Snackbar suggestedEditsNavTabSnackbar;
    private PageChangeCallback pageChangeCallback = new PageChangeCallback();
    private CompositeDisposable disposables = new CompositeDisposable();
    private boolean navTabAutoSelect;

    // The permissions request API doesn't take a callback, so in the event we have to
    // ask for permission to download a featured image from the feed, we'll have to hold
    // the image we're waiting for permission to download as a bit of state here. :(
    @Nullable private FeaturedImage pendingDownloadImage;

    public interface Callback {
        void onTabChanged(@NonNull NavTab tab);
        void updateToolbarElevation(boolean elevate);
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                 @Nullable ViewGroup container,
                                                 @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, view);
        disposables.add(WikipediaApp.getInstance().getBus().subscribe(new EventBusConsumer()));

        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(new NavTabFragmentPagerAdapter(this));
        viewPager.registerOnPageChangeCallback(pageChangeCallback);

        tabLayout.setOnNavigationItemSelectedListener(item -> {
            if (!navTabAutoSelect && getCurrentFragment() instanceof FeedFragment && item.getOrder() == 0) {
                ((FeedFragment) getCurrentFragment()).scrollToTop();
            }
            viewPager.setCurrentItem(item.getOrder());
            return true;
        });

        if (savedInstanceState == null) {
            handleIntent(requireActivity().getIntent());
        }
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        downloadReceiver.setCallback(null);
        requireContext().unregisterReceiver(downloadReceiver);
    }

    @Override public void onResume() {
        super.onResume();
        requireContext().registerReceiver(downloadReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        downloadReceiver.setCallback(downloadReceiverCallback);
        // reset the last-page-viewed timer
        Prefs.pageLastShown(0);
        navTabAutoSelect = true;
        resetNavTabLayouts();
        navTabAutoSelect = false;
    }

    @Override public void onDestroyView() {
        viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        unbinder.unbind();
        unbinder = null;
        disposables.dispose();
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == Constants.ACTIVITY_REQUEST_VOICE_SEARCH
                && resultCode == Activity.RESULT_OK && data != null
                && data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) != null) {
            String searchQuery = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            openSearchActivity(VOICE, searchQuery);
        } else if (requestCode == Constants.ACTIVITY_REQUEST_GALLERY
                && resultCode == GalleryActivity.ACTIVITY_RESULT_PAGE_SELECTED) {
            startActivity(data);
        } else if (requestCode == Constants.ACTIVITY_REQUEST_LOGIN
                && resultCode == LoginActivity.RESULT_LOGIN_SUCCESS) {
            refreshExploreFeed();
            ((MainActivity) requireActivity()).setUpHomeMenuIcon();
            if (!Prefs.shouldShowSuggestedEditsTooltip()) {
                FeedbackUtil.showMessage(this, R.string.login_success_toast);
            }
        } else if (requestCode == Constants.ACTIVITY_REQUEST_BROWSE_TABS) {
            if (WikipediaApp.getInstance().getTabCount() == 0) {
                // They browsed the tabs and cleared all of them, without wanting to open a new tab.
                return;
            }
            if (resultCode == TabActivity.RESULT_NEW_TAB) {
                startActivity(PageActivity.newIntentForNewTab(requireContext()));
            } else if (resultCode == TabActivity.RESULT_LOAD_FROM_BACKSTACK) {
                startActivity(PageActivity.newIntent(requireContext()));
            }
        } else if ((requestCode == Constants.ACTIVITY_REQUEST_OPEN_SEARCH_ACTIVITY && resultCode == SearchFragment.RESULT_LANG_CHANGED)
                || (requestCode == Constants.ACTIVITY_REQUEST_SETTINGS && resultCode == SettingsActivity.ACTIVITY_RESULT_LANGUAGE_CHANGED)) {
            refreshExploreFeed();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.ACTIVITY_REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION:
                if (PermissionUtil.isPermitted(grantResults)) {
                    if (pendingDownloadImage != null) {
                        download(pendingDownloadImage);
                    }
                } else {
                    setPendingDownload(null);
                    L.i("Write permission was denied by user");
                    FeedbackUtil.showMessage(this,
                            R.string.gallery_save_image_write_permission_rationale);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void handleIntent(Intent intent) {
        if (intent.hasExtra(Constants.INTENT_APP_SHORTCUT_RANDOMIZER)) {
            startActivity(RandomActivity.newIntent(requireActivity(), APP_SHORTCUTS));
        } else if (intent.hasExtra(Constants.INTENT_APP_SHORTCUT_SEARCH)) {
            openSearchActivity(APP_SHORTCUTS, null);
        } else if (intent.hasExtra(Constants.INTENT_APP_SHORTCUT_CONTINUE_READING)) {
            startActivity(PageActivity.newIntent(requireActivity()));
        } else if (intent.hasExtra(Constants.INTENT_EXTRA_DELETE_READING_LIST)) {
            goToTab(NavTab.READING_LISTS);
        } else if (intent.hasExtra(Constants.INTENT_EXTRA_GO_TO_MAIN_TAB)
                && !((tabLayout.getSelectedItemId() == NavTab.EXPLORE.code())
                && intent.getIntExtra(Constants.INTENT_EXTRA_GO_TO_MAIN_TAB, NavTab.EXPLORE.code()) == NavTab.EXPLORE.code())) {
            goToTab(NavTab.of(intent.getIntExtra(Constants.INTENT_EXTRA_GO_TO_MAIN_TAB, NavTab.EXPLORE.code())));
        } else if (lastPageViewedWithin(1) && !intent.hasExtra(Constants.INTENT_RETURN_TO_MAIN) && WikipediaApp.getInstance().getTabCount() > 0) {
            startActivity(PageActivity.newIntent(requireContext()));
        }
    }

    @Override public void onFeedSearchRequested() {
        openSearchActivity(FEED_BAR, null);
    }

    @Override public void onFeedVoiceSearchRequested() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        try {
            startActivityForResult(intent, Constants.ACTIVITY_REQUEST_VOICE_SEARCH);
        } catch (ActivityNotFoundException a) {
            FeedbackUtil.showMessage(this, R.string.error_voice_search_not_available);
        }
    }

    @Override public void onFeedSelectPage(HistoryEntry entry) {
        startActivity(PageActivity.newIntentForCurrentTab(requireContext(), entry, entry.getTitle()), getTransitionAnimationBundle(entry.getTitle()));
    }

    @Override public void onFeedSelectPageFromExistingTab(HistoryEntry entry) {
        startActivity(PageActivity.newIntentForExistingTab(requireContext(), entry, entry.getTitle()), getTransitionAnimationBundle(entry.getTitle()));
    }

    @Override public void onFeedAddPageToList(HistoryEntry entry) {
        bottomSheetPresenter.show(getChildFragmentManager(),
                AddToReadingListDialog.newInstance(entry.getTitle(), FEED));
    }

    @Override
    public void onFeedRemovePageFromList(@NonNull HistoryEntry entry) {
        FeedbackUtil.showMessage(requireActivity(),
                getString(R.string.reading_list_item_deleted, entry.getTitle().getDisplayText()));
    }

    @Override public void onFeedSharePage(HistoryEntry entry) {
        ShareUtil.shareText(requireContext(), entry.getTitle());
    }

    @Override public void onFeedNewsItemSelected(@NonNull NewsItemCard card, @NonNull HorizontalScrollingListCardItemView view) {
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(requireActivity(), view.getImageView(), getString(R.string.transition_news_item));
        startActivity(NewsActivity.newIntent(requireActivity(), card.item(), card.wikiSite()), card.image() != null ? options.toBundle() : null);
    }

    @Override public void onFeedShareImage(final FeaturedImageCard card) {
        final String thumbUrl = card.baseImage().getThumbnailUrl();
        final String fullSizeUrl = card.baseImage().getOriginal().getSource();
        new ImagePipelineBitmapGetter(thumbUrl) {
            @Override
            public void onSuccess(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    ShareUtil.shareImage(requireContext(),
                            bitmap,
                            new File(thumbUrl).getName(),
                            ShareUtil.getFeaturedImageShareSubject(requireContext(), card.age()),
                            fullSizeUrl);
                } else {
                    FeedbackUtil.showMessage(MainFragment.this, getString(R.string.gallery_share_error, card.baseImage().title()));
                }
            }
        }.get();
    }

    @Override public void onFeedDownloadImage(FeaturedImage image) {
        if (!(PermissionUtil.hasWriteExternalStoragePermission(requireContext()))) {
            setPendingDownload(image);
            requestWriteExternalStoragePermission();
        } else {
            download(image);
        }
    }

    @Override public void onFeaturedImageSelected(FeaturedImageCard card) {
        startActivityForResult(GalleryActivity.newIntent(requireActivity(), card.age(),
                card.filename(), card.baseImage(), card.wikiSite(),
                GalleryFunnel.SOURCE_FEED_FEATURED_IMAGE), Constants.ACTIVITY_REQUEST_GALLERY);
    }

    @Override
    public void onLoginRequested() {
        startActivityForResult(LoginActivity.newIntent(requireContext(), LoginFunnel.SOURCE_NAV),
                Constants.ACTIVITY_REQUEST_LOGIN);
    }

    @Nullable
    public Bundle getTransitionAnimationBundle(@NonNull PageTitle pageTitle) {
        // TODO: add future transition animations.
        return null;
    }

    @Override
    public void updateToolbarElevation(boolean elevate) {
        if (callback() != null) {
            callback().updateToolbarElevation(elevate);
        }
    }

    public void requestUpdateToolbarElevation() {
        Fragment fragment = getCurrentFragment();
        updateToolbarElevation(!(fragment instanceof FeedFragment || fragment instanceof SuggestedEditsTasksFragment) || (fragment instanceof FeedFragment && ((FeedFragment) fragment).shouldElevateToolbar()));
    }

    @Override
    public void onLoadPage(@NonNull HistoryEntry entry) {
        startActivity(PageActivity.newIntentForCurrentTab(requireContext(), entry, entry.getTitle()), getTransitionAnimationBundle(entry.getTitle()));
    }

    @Override
    public void onClearHistory() {
        disposables.add(Completable.fromAction(() -> WikipediaApp.getInstance().getDatabaseClient(HistoryEntry.class).deleteAll())
                .subscribeOn(Schedulers.io()).subscribe());
    }

    public void onLinkPreviewLoadPage(@NonNull PageTitle title, @NonNull HistoryEntry entry, boolean inNewTab) {
        if (inNewTab) {
            startActivity(PageActivity.newIntentForNewTab(requireContext(), entry, entry.getTitle()), getTransitionAnimationBundle(entry.getTitle()));
        } else {
            startActivity(PageActivity.newIntentForCurrentTab(requireContext(), entry, entry.getTitle()), getTransitionAnimationBundle(entry.getTitle()));
        }
    }

    @Override
    public void onLinkPreviewCopyLink(@NonNull PageTitle title) {
        copyLink(title.getCanonicalUri());
    }

    @Override
    public void onLinkPreviewAddToList(@NonNull PageTitle title) {
        bottomSheetPresenter.show(getChildFragmentManager(),
                AddToReadingListDialog.newInstance(title, LINK_PREVIEW_MENU));
    }

    @Override
    public void onLinkPreviewShareLink(@NonNull PageTitle title) {
        ShareUtil.shareText(requireContext(), title);
    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment = getCurrentFragment();
        return fragment instanceof BackPressedHandler && ((BackPressedHandler) fragment).onBackPressed();
    }

    public void setBottomNavVisible(boolean visible) {
        navTabContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void onGoOffline() {
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof FeedFragment) {
            ((FeedFragment) fragment).onGoOffline();
        } else if (fragment instanceof HistoryFragment) {
            ((HistoryFragment) fragment).refresh();
        }
    }

    public void onGoOnline() {
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof FeedFragment) {
            ((FeedFragment) fragment).onGoOnline();
        } else if (fragment instanceof HistoryFragment) {
            ((HistoryFragment) fragment).refresh();
        }
    }

    private void copyLink(@NonNull String url) {
        ClipboardUtil.setPlainText(requireContext(), null, url);
        FeedbackUtil.showMessage(this, R.string.address_copied);
    }

    private boolean lastPageViewedWithin(int days) {
        return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - Prefs.pageLastShown()) < days;
    }

    private void download(@NonNull FeaturedImage image) {
        setPendingDownload(null);
        downloadReceiver.download(requireContext(), image);
        FeedbackUtil.showMessage(this, R.string.gallery_save_progress);
    }

    private void setPendingDownload(@Nullable FeaturedImage image) {
        pendingDownloadImage = image;
    }

    private void requestWriteExternalStoragePermission() {
        PermissionUtil.requestWriteStorageRuntimePermissions(this,
                Constants.ACTIVITY_REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
    }

    private void openSearchActivity(@NonNull Constants.InvokeSource source, @Nullable String query) {
        Intent intent = WikiSearchActivity.newIntent(requireActivity(), source, query);
        startActivityForResult(intent, ACTIVITY_REQUEST_OPEN_SEARCH_ACTIVITY);
    }

    private void goToTab(@NonNull NavTab tab) {
        tabLayout.setSelectedItemId(tab.code());
    }

    private void refreshExploreFeed() {
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof FeedFragment) {
            ((FeedFragment) fragment).refresh();
        }
    }

    void resetNavTabLayouts() {
        tabLayout.setTabViews();
        tabLayout.setSelectedItemId(viewPager.getCurrentItem());
        if (AccountUtil.isLoggedIn()) {
            if (Prefs.shouldShowSuggestedEditsTooltip()) {
                Prefs.setShouldShowSuggestedEditsTooltip(false);
                Prefs.setShouldShowImageTagsTooltip(false);
                tabOverlayLayout.pick(NavTab.SUGGESTED_EDITS);
                suggestedEditsNavTabSnackbar = FeedbackUtil.makeSnackbar(requireActivity(), getString(R.string.main_tooltip_text, AccountUtil.getUserName()), FeedbackUtil.LENGTH_LONG);
                suggestedEditsNavTabSnackbar.setAction(R.string.main_tooltip_action_button, view -> goToTab(NavTab.SUGGESTED_EDITS));
                suggestedEditsNavTabSnackbar.show();
            } else if (Prefs.shouldShowImageTagsTooltip()) {
                Prefs.setShouldShowImageTagsTooltip(false);
                tabOverlayLayout.pick(NavTab.SUGGESTED_EDITS);
                suggestedEditsNavTabSnackbar = FeedbackUtil.makeSnackbar(requireActivity(), getString(R.string.suggested_edits_image_tags_snackbar), FeedbackUtil.LENGTH_LONG);
                suggestedEditsNavTabSnackbar.setAction(R.string.main_tooltip_action_button, view -> goToTab(NavTab.SUGGESTED_EDITS));
                suggestedEditsNavTabSnackbar.show();
            }
        } else {
            hideNavTabOverlayLayout();
        }
    }

    void hideNavTabOverlayLayout() {
        tabOverlayLayout.hide();
        if (suggestedEditsNavTabSnackbar != null) {
            suggestedEditsNavTabSnackbar.dismiss();
        }
    }

    @Nullable
    public Fragment getCurrentFragment() {
        return ((NavTabFragmentPagerAdapter) viewPager.getAdapter()).getFragmentAt(viewPager.getCurrentItem());
    }

    private class PageChangeCallback extends ViewPager2.OnPageChangeCallback {
        @Override
        public void onPageSelected(int position) {
            Callback callback = callback();
            if (callback != null) {
                NavTab tab = NavTab.of(position);
                callback.onTabChanged(tab);
            }
        }
    }

    private class MediaDownloadReceiverCallback implements MediaDownloadReceiver.Callback {
        @Override
        public void onSuccess() {
            FeedbackUtil.showMessage(requireActivity(), R.string.gallery_save_success);
        }
    }

    private class EventBusConsumer implements Consumer<Object> {
        @Override
        public void accept(Object event) {
            if (event instanceof LoggedOutInBackgroundEvent) {
                resetNavTabLayouts();
            }
        }
    }

    @Nullable private Callback callback() {
        return FragmentUtil.getCallback(this, Callback.class);
    }
}
