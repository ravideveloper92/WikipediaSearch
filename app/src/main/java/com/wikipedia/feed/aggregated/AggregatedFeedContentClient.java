package com.wikipedia.feed.aggregated;

import android.content.Context;

import com.wikipedia.feed.dataclient.FeedClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.wikipedia.WikipediaApp;
import com.wikipedia.dataclient.ServiceFactory;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.feed.FeedContentType;
import com.wikipedia.feed.FeedCoordinator;
import com.wikipedia.feed.dataclient.FeedClient;
import com.wikipedia.feed.featured.FeaturedArticleCard;
import com.wikipedia.feed.image.FeaturedImageCard;
import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.model.UtcDate;
import com.wikipedia.feed.mostread.MostReadListCard;
import com.wikipedia.feed.news.NewsListCard;
import com.wikipedia.feed.onthisday.OnThisDayCard;
import com.wikipedia.util.DateUtil;
import com.wikipedia.util.log.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AggregatedFeedContentClient {
    @NonNull private Map<String, AggregatedFeedContent> aggregatedResponses = new HashMap<>();
    private int aggregatedResponseAge = -1;
    private CompositeDisposable disposables = new CompositeDisposable();

    public static class OnThisDayFeed extends BaseClient {
        public OnThisDayFeed(@NonNull AggregatedFeedContentClient aggregatedClient) {
            super(aggregatedClient);
        }

        @Override
        void getCardFromResponse(@NonNull Map<String, AggregatedFeedContent> responses,
                                 @NonNull WikiSite wiki, int age, @NonNull List<Card> outCards) {
            for (String appLangCode : WikipediaApp.getInstance().language().getAppLanguageCodes()) {
                if (responses.containsKey(appLangCode)
                        && !FeedContentType.ON_THIS_DAY.getLangCodesDisabled().contains(appLangCode)) {
                    AggregatedFeedContent content = responses.get(appLangCode);
                    if (content.onthisday() != null && !content.onthisday().isEmpty()) {
                        outCards.add(new OnThisDayCard(content.onthisday(), WikiSite.forLanguageCode(appLangCode), age));
                    }
                }
            }
        }
    }
    public static class InTheNews extends BaseClient {
        public InTheNews(@NonNull AggregatedFeedContentClient aggregatedClient) {
            super(aggregatedClient);
        }

        @Override
        void getCardFromResponse(@NonNull Map<String, AggregatedFeedContent> responses,
                                 @NonNull WikiSite wiki, int age, @NonNull List<Card> outCards) {
            // todo: remove age check when news endpoint provides dated content, T139481.
            for (String appLangCode : WikipediaApp.getInstance().language().getAppLanguageCodes()) {
                if (responses.containsKey(appLangCode)
                        && !FeedContentType.NEWS.getLangCodesDisabled().contains(appLangCode)) {
                    AggregatedFeedContent content = responses.get(appLangCode);
                    if (age == 0 && content.news() != null) {
                        outCards.add(new NewsListCard(content.news(), age, WikiSite.forLanguageCode(appLangCode)));
                    }
                }
            }
        }
    }

    public static class FeaturedArticle extends BaseClient {
        public FeaturedArticle(@NonNull AggregatedFeedContentClient aggregatedClient) {
            super(aggregatedClient);
        }

        @Override
        void getCardFromResponse(@NonNull Map<String, AggregatedFeedContent> responses,
                                 @NonNull WikiSite wiki, int age, @NonNull List<Card> outCards) {
            for (String appLangCode : WikipediaApp.getInstance().language().getAppLanguageCodes()) {
                if (responses.containsKey(appLangCode)
                        && !FeedContentType.FEATURED_ARTICLE.getLangCodesDisabled().contains(appLangCode)) {
                    AggregatedFeedContent content = responses.get(appLangCode);
                    if (content.tfa() != null) {
                        outCards.add(new FeaturedArticleCard(content.tfa(), age, WikiSite.forLanguageCode(appLangCode)));
                    }
                }
            }
        }
    }

    public static class TrendingArticles extends BaseClient {
        public TrendingArticles(@NonNull AggregatedFeedContentClient aggregatedClient) {
            super(aggregatedClient);
        }

        @Override
        void getCardFromResponse(@NonNull Map<String, AggregatedFeedContent> responses,
                                 @NonNull WikiSite wiki, int age, @NonNull List<Card> outCards) {
            for (String appLangCode : WikipediaApp.getInstance().language().getAppLanguageCodes()) {
                if (responses.containsKey(appLangCode)
                        && !FeedContentType.TRENDING_ARTICLES.getLangCodesDisabled().contains(appLangCode)) {
                    AggregatedFeedContent content = responses.get(appLangCode);
                    if (content.mostRead() != null) {
                        outCards.add(new MostReadListCard(content.mostRead(), WikiSite.forLanguageCode(appLangCode)));
                    }
                }
            }
        }
    }

    public static class FeaturedImage extends BaseClient {
        public FeaturedImage(@NonNull AggregatedFeedContentClient aggregatedClient) {
            super(aggregatedClient);
        }

        @Override
        void getCardFromResponse(@NonNull Map<String, AggregatedFeedContent> responses,
                                 @NonNull WikiSite wiki, int age, @NonNull List<Card> outCards) {
            if (responses.containsKey(wiki.languageCode())) {
                AggregatedFeedContent content = responses.get(wiki.languageCode());
                if (content.potd() != null) {
                    outCards.add(new FeaturedImageCard(content.potd(), age, wiki));
                }
            }
        }
    }

    public void invalidate() {
        aggregatedResponseAge = -1;
    }

    public void cancel() {
        disposables.clear();
    }

    private abstract static class BaseClient implements FeedClient {
        @NonNull private AggregatedFeedContentClient aggregatedClient;
        @Nullable private Callback cb;
        private WikiSite wiki;
        private int age;

        BaseClient(@NonNull AggregatedFeedContentClient aggregatedClient) {
            this.aggregatedClient = aggregatedClient;
        }

        abstract void getCardFromResponse(@NonNull Map<String, AggregatedFeedContent> responses,
                                          @NonNull WikiSite wiki, int age, @NonNull List<Card> outCards);

        @Override
        public void request(@NonNull Context context, @NonNull WikiSite wiki, int age, @NonNull Callback cb) {
            this.cb = cb;
            this.age = age;
            this.wiki = wiki;
            if (aggregatedClient.aggregatedResponseAge == age
                    && aggregatedClient.aggregatedResponses.containsKey(wiki.languageCode())) {
                List<Card> cards = new ArrayList<>();
                getCardFromResponse(aggregatedClient.aggregatedResponses, wiki, age, cards);
                FeedCoordinator.postCardsToCallback(cb, cards);
            } else {
                requestAggregated();
            }
        }

        @Override
        public void cancel() {
        }

        private void requestAggregated() {
            aggregatedClient.cancel();
            UtcDate date = DateUtil.getUtcRequestDateFor(age);
            aggregatedClient.disposables.add(Observable.fromIterable(FeedContentType.getAggregatedLanguages())
                    .flatMap(lang -> ServiceFactory.getRest(WikiSite.forLanguageCode(lang)).getAggregatedFeed(date.year(), date.month(), date.date()).subscribeOn(Schedulers.io()), Pair::new)
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(pairList -> {
                        List<Card> cards = new ArrayList<>();
                        for (Pair<String, AggregatedFeedContent> pair : pairList) {
                            AggregatedFeedContent content = pair.second;
                            if (content == null) {
                                continue;
                            }
                            aggregatedClient.aggregatedResponses.put(WikiSite.forLanguageCode(pair.first).languageCode(), content);
                            aggregatedClient.aggregatedResponseAge = age;
                        }
                        if (aggregatedClient.aggregatedResponses.containsKey(wiki.languageCode())) {
                            getCardFromResponse(aggregatedClient.aggregatedResponses, wiki, age, cards);
                        }
                        if (cb != null) {
                            FeedCoordinator.postCardsToCallback(cb, cards);
                        }
                    }, caught -> {
                        L.v(caught);
                        if (cb != null) {
                            cb.error(caught);
                        }
                    }));
        }
    }
}
