package com.wikipedia.feed.random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import com.wikipedia.page.PageTitle;

import androidx.annotation.NonNull;

import com.wikipedia.R;
import com.wikipedia.WikipediaApp;
import com.wikipedia.dataclient.ServiceFactory;
import com.wikipedia.feed.view.StaticCardView;
import com.wikipedia.history.HistoryEntry;
import com.wikipedia.page.PageTitle;
import com.wikipedia.readinglist.database.ReadingListDbHelper;
import com.wikipedia.readinglist.database.ReadingListPage;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RandomCardView extends StaticCardView<RandomCard> {
    public interface Callback {
        void onRandomClick(@NonNull RandomCardView view);
        void onGetRandomError(@NonNull Throwable t, @NonNull RandomCardView view);
    }

    public RandomCardView(@NonNull Context context) {
        super(context);
        setTransitionName(getString(R.string.transition_random_activity));
    }

    @Override public void setCard(@NonNull RandomCard card) {
        super.setCard(card);
        setTitle(getString(R.string.view_random_card_title));
        setSubtitle(getString(R.string.view_random_card_subtitle));
        setIcon(R.drawable.ic_casino_accent50_24dp);
        setContainerBackground(R.color.accent50);
        setAction(R.drawable.ic_casino_accent50_24dp, R.string.view_random_card_action);
    }

    protected void onContentClick(View v) {
        if (getCallback() != null) {
            getCallback().onRandomClick(RandomCardView.this);
        }
    }

    protected void onActionClick(View v) {
        if (getCallback() != null) {
            getCallback().onRandomClick(RandomCardView.this);
        }
    }

    @SuppressLint("CheckResult")
    public void getRandomPage() {
        setProgress(true);
        ServiceFactory.getRest(WikipediaApp.getInstance().getWikiSite()).getRandomSummary()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(pageSummary -> new PageTitle(null, pageSummary.getApiTitle(), WikipediaApp.getInstance().getWikiSite()))
                .onErrorResumeNext(throwable -> {
                    return Observable.fromCallable(() -> {
                                ReadingListPage page = ReadingListDbHelper.instance().getRandomPage();
                                if (page == null) {
                                    throw (Exception) throwable;
                                }
                                return ReadingListPage.toPageTitle(page);
                            }
                    );
                })
                .doAfterTerminate(() -> setProgress(false))
                .subscribe(pageTitle -> {
                    if (getCallback() != null && getCard() != null) {
                        getCallback().onSelectPage(getCard(),
                                new HistoryEntry(pageTitle, HistoryEntry.SOURCE_FEED_RANDOM));
                    }
                }, t -> {
                    if (getCallback() != null && getCard() != null) {
                        getCallback().onGetRandomError(t, RandomCardView.this);
                    }
                });
    }
}
