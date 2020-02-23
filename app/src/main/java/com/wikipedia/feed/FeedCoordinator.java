package com.wikipedia.feed;

import android.annotation.SuppressLint;
import android.content.Context;

import com.wikipedia.feed.aggregated.AggregatedFeedContentClient;
import com.wikipedia.feed.announcement.AnnouncementClient;
import com.wikipedia.feed.dataclient.FeedClient;
import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.offline.OfflineCardClient;
import com.wikipedia.feed.onboarding.OnboardingClient;
import com.wikipedia.feed.searchbar.SearchClient;

import androidx.annotation.NonNull;

import com.wikipedia.WikipediaApp;
import com.wikipedia.feed.aggregated.AggregatedFeedContentClient;
import com.wikipedia.feed.announcement.AnnouncementClient;
import com.wikipedia.feed.dataclient.FeedClient;
import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.offline.OfflineCardClient;
import com.wikipedia.feed.onboarding.OnboardingClient;
import com.wikipedia.feed.searchbar.SearchClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FeedCoordinator extends FeedCoordinatorBase {
    @NonNull private AggregatedFeedContentClient aggregatedClient = new AggregatedFeedContentClient();

    FeedCoordinator(@NonNull Context context) {
        super(context);
        FeedContentType.restoreState();
    }

    @Override
    public void reset() {
        super.reset();
        aggregatedClient.invalidate();
    }

    @Override
    protected void buildScript(int age) {
        boolean online = WikipediaApp.getInstance().isOnline();

        conditionallyAddPendingClient(new SearchClient(), age == 0);
        conditionallyAddPendingClient(new AnnouncementClient(), age == 0 && online);
        conditionallyAddPendingClient(new OnboardingClient(), age == 0);

        List<FeedContentType> orderedContentTypes = new ArrayList<>();
        orderedContentTypes.addAll(Arrays.asList(FeedContentType.values()));
        Collections.sort(orderedContentTypes, (FeedContentType a, FeedContentType b)
                -> a.getOrder().compareTo(b.getOrder()));

        for (FeedContentType contentType : orderedContentTypes) {
            addPendingClient(contentType.newClient(aggregatedClient, age));
        }

        conditionallyAddPendingClient(new OfflineCardClient(), age == 0 && !online);

    }

    @SuppressLint("CheckResult")
    public static void postCardsToCallback(@NonNull FeedClient.Callback cb, @NonNull List<Card> cards) {
        Completable.fromAction(() -> {
            final int delayMillis = 150;
            Thread.sleep(delayMillis);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> cb.success(cards));
    }
}
