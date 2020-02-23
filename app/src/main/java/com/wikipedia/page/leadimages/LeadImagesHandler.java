package com.wikipedia.page.leadimages;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;

import com.wikipedia.util.StringUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import org.apache.commons.lang3.StringUtils;
import com.wikipedia.Constants;
import com.wikipedia.R;
import com.wikipedia.WikipediaApp;
import com.wikipedia.analytics.GalleryFunnel;
import com.wikipedia.auth.AccountUtil;
import com.wikipedia.bridge.CommunicationBridge;
import com.wikipedia.dataclient.Service;
import com.wikipedia.dataclient.ServiceFactory;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.dataclient.mwapi.media.MediaHelper;
import com.wikipedia.descriptions.DescriptionEditActivity;
import com.wikipedia.gallery.GalleryActivity;
import com.wikipedia.gallery.ImageInfo;
import com.wikipedia.page.Page;
import com.wikipedia.page.PageFragment;
import com.wikipedia.page.PageTitle;
import com.wikipedia.suggestededits.SuggestedEditsSummary;
import com.wikipedia.util.DimenUtil;
import com.wikipedia.util.StringUtil;
import com.wikipedia.util.log.L;
import com.wikipedia.views.ObservableWebView;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.wikipedia.Constants.ACTIVITY_REQUEST_IMAGE_CAPTION_EDIT;
import static com.wikipedia.Constants.InvokeSource.LEAD_IMAGE;
import static com.wikipedia.Constants.MIN_LANGUAGES_TO_UNLOCK_TRANSLATION;
import static com.wikipedia.descriptions.DescriptionEditActivity.Action.ADD_CAPTION;
import static com.wikipedia.descriptions.DescriptionEditActivity.Action.TRANSLATE_CAPTION;
import static com.wikipedia.settings.Prefs.isImageDownloadEnabled;
import static com.wikipedia.util.DimenUtil.leadImageHeightForDevice;

public class LeadImagesHandler {
    /**
     * Minimum screen height for enabling lead images. If the screen is smaller than
     * this height, lead images will not be displayed, and will be substituted with just
     * the page title.
     */
    private static final int MIN_SCREEN_HEIGHT_DP = 480;

    @NonNull private final PageFragment parentFragment;
    @NonNull private final CommunicationBridge bridge;

    @NonNull private final PageHeaderView pageHeaderView;

    private int displayHeightDp;

    @Nullable private SuggestedEditsSummary callToActionSourceSummary;
    @Nullable private SuggestedEditsSummary callToActionTargetSummary;
    private boolean callToActionIsTranslation;
    private CompositeDisposable disposables = new CompositeDisposable();

    public LeadImagesHandler(@NonNull final PageFragment parentFragment,
                             @NonNull CommunicationBridge bridge,
                             @NonNull ObservableWebView webView,
                             @NonNull PageHeaderView pageHeaderView) {
        this.parentFragment = parentFragment;
        this.pageHeaderView = pageHeaderView;
        this.pageHeaderView.setWebView(webView);

        this.bridge = bridge;
        webView.addOnScrollChangeListener(pageHeaderView);

        initDisplayDimensions();
        initArticleHeaderView();
    }

    /**
     * Completely hide the lead image view. Useful in case of network errors, etc.
     * The only way to "show" the lead image view is by calling the beginLayout function.
     */
    public void hide() {
        pageHeaderView.hide();
    }

    @Nullable public Bitmap getLeadImageBitmap() {
        return isLeadImageEnabled() ? pageHeaderView.copyBitmap() : null;
    }

    public boolean isLeadImageEnabled() {
        return isImageDownloadEnabled()
                && !(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                && displayHeightDp >= MIN_SCREEN_HEIGHT_DP
                && !isMainPage()
                && !TextUtils.isEmpty(getLeadImageUrl());
    }


    public int getTopMargin() {
        return isLeadImageEnabled() ? Math.round(leadImageHeightForDevice() / DimenUtil.getDensityScalar())
                : Math.round(parentFragment.requireActivity().getResources().getDimensionPixelSize(R.dimen.lead_no_image_top_offset_dp) / DimenUtil.getDensityScalar());
    }

    /**
     * Determines and sets displayHeightDp for the lead images layout.
     */
    private void initDisplayDimensions() {
        displayHeightDp = (int) (DimenUtil.getDisplayHeightPx() / DimenUtil.getDensityScalar());
    }

    public void loadLeadImage() {
        String url = getLeadImageUrl();
        if (getPage() == null) {
            return;
        }
        initDisplayDimensions();
        if (!isMainPage() && !TextUtils.isEmpty(url) && isLeadImageEnabled()) {
            pageHeaderView.show();
            pageHeaderView.loadImage(url);
            updateCallToAction();
        } else {
            pageHeaderView.loadImage(null);
        }
    }

    private void updateCallToAction() {
        dispose();
        pageHeaderView.setUpCallToAction(null);
        if (!AccountUtil.isLoggedIn() || getLeadImageUrl() == null || !getLeadImageUrl().contains(Service.URL_FRAGMENT_FROM_COMMONS) || getPage() == null) {
            return;
        }
        String imageTitle = "File:" + getPage().getPageProperties().getLeadImageName();
        disposables.add(Observable.zip(MediaHelper.INSTANCE.getImageCaptions(imageTitle),
                ServiceFactory.get(getTitle().getWikiSite()).getImageExtMetadata(imageTitle), Pair::new)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                            WikipediaApp app = WikipediaApp.getInstance();
                            PageTitle captionSourcePageTitle = new PageTitle(imageTitle, new WikiSite(Service.COMMONS_URL, getTitle().getWikiSite().languageCode()));
                            ImageInfo imageInfo = pair.second.query().firstPage().imageInfo();

                            if (!pair.first.containsKey(getTitle().getWikiSite().languageCode())) {
                                pageHeaderView.setUpCallToAction(app.getResources().getString(R.string.suggested_edits_article_cta_image_caption));
                                callToActionSourceSummary = new SuggestedEditsSummary(captionSourcePageTitle.getPrefixedText(), getTitle().getWikiSite().languageCode(), captionSourcePageTitle,
                                        captionSourcePageTitle.getDisplayText(), StringUtils.defaultIfBlank(StringUtil.fromHtml(imageInfo.getMetadata().imageDescription()).toString(), null),
                                        imageInfo.getThumbUrl(), null, null, null, null);

                                return;
                            }
                            if (app.language().getAppLanguageCodes().size() >= MIN_LANGUAGES_TO_UNLOCK_TRANSLATION) {
                                for (String lang : app.language().getAppLanguageCodes()) {
                                    if (!pair.first.containsKey(lang)) {
                                        callToActionIsTranslation = true;
                                        PageTitle captionTargetPageTitle = new PageTitle(imageTitle, new WikiSite(Service.COMMONS_URL, lang));
                                        String currentCaption = pair.first.get(getTitle().getWikiSite().languageCode());
                                        captionSourcePageTitle.setDescription(currentCaption);
                                        callToActionSourceSummary = new SuggestedEditsSummary(captionSourcePageTitle.getPrefixedText(), captionSourcePageTitle.getWikiSite().languageCode(), captionSourcePageTitle,
                                                captionSourcePageTitle.getDisplayText(), currentCaption, getLeadImageUrl(),
                                                null, null, null, null);

                                        callToActionTargetSummary = new SuggestedEditsSummary(captionTargetPageTitle.getPrefixedText(), captionTargetPageTitle.getWikiSite().languageCode(), captionTargetPageTitle,
                                                captionTargetPageTitle.getDisplayText(), null, getLeadImageUrl(),
                                                null, null, null, null);
                                        pageHeaderView.setUpCallToAction(app.getResources().getString(R.string.suggested_edits_article_cta_image_caption_in_language, app.language().getAppLanguageLocalizedName(lang)));
                                        break;
                                    }
                                }
                            }
                        },
                        L::e));
    }

    @Nullable private String getLeadImageUrl() {
        String url = getPage() == null ? null : getPage().getPageProperties().getLeadImageUrl();
        if (url == null) {
            return null;
        }

        // Conditionally add the PageTitle's URL scheme and authority if these are missing from the
        // PageProperties' URL.
        Uri fullUri = Uri.parse(url);
        String scheme = getTitle().getWikiSite().scheme();
        String authority = getTitle().getWikiSite().authority();

        if (fullUri.getScheme() != null) {
            scheme = fullUri.getScheme();
        }
        if (fullUri.getAuthority() != null) {
            authority = fullUri.getAuthority();
        }
        return new Uri.Builder()
                .scheme(scheme)
                .authority(authority)
                .path(fullUri.getPath())
                .toString();
    }

    private void initArticleHeaderView() {
        pageHeaderView.setCallback(new PageHeaderView.Callback() {
            @Override
            public void onImageClicked() {
                openImageInGallery(null);
            }

            @Override
            public void onCallToActionClicked() {
                if (callToActionIsTranslation ? (callToActionTargetSummary != null && callToActionSourceSummary != null) : callToActionSourceSummary != null) {
                    getActivity().startActivityForResult(DescriptionEditActivity.newIntent(getActivity(),
                            callToActionIsTranslation ? callToActionTargetSummary.getPageTitle() : callToActionSourceSummary.getPageTitle(), null,
                            callToActionSourceSummary, callToActionTargetSummary, callToActionIsTranslation ? TRANSLATE_CAPTION : ADD_CAPTION, LEAD_IMAGE),
                            ACTIVITY_REQUEST_IMAGE_CAPTION_EDIT);
                }
            }
        });
    }

    public void openImageInGallery(@Nullable  String language) {
        if (getPage() != null && isLeadImageEnabled()) {
            String imageName = getPage().getPageProperties().getLeadImageName();
            if (imageName != null) {
                String filename = "File:" + imageName;
                WikiSite wiki = language == null ? getTitle().getWikiSite() : WikiSite.forLanguageCode(language);
                getActivity().startActivityForResult(GalleryActivity.newIntent(getActivity(),
                        parentFragment.getTitleOriginal(), filename, wiki, parentFragment.getRevision(),
                        GalleryFunnel.SOURCE_LEAD_IMAGE),
                        Constants.ACTIVITY_REQUEST_GALLERY);
            }
        }
    }

    private boolean isMainPage() {
        return getPage() != null && getPage().isMainPage();
    }

    private PageTitle getTitle() {
        return parentFragment.getTitle();
    }

    @Nullable
    private Page getPage() {
        return parentFragment.getPage();
    }

    private FragmentActivity getActivity() {
        return parentFragment.getActivity();
    }

    public void dispose() {
        disposables.clear();
        callToActionSourceSummary = null;
        callToActionTargetSummary = null;
        callToActionIsTranslation = false;
    }

    @Nullable public String getCallToActionEditLang() {
        if (callToActionIsTranslation ? (callToActionTargetSummary == null || callToActionSourceSummary == null) : callToActionSourceSummary == null) {
            return null;
        }
        return callToActionIsTranslation ? callToActionTargetSummary.getPageTitle().getWikiSite().languageCode()
                : callToActionSourceSummary.getPageTitle().getWikiSite().languageCode();
    }
}
