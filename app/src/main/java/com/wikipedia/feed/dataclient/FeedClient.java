package com.wikipedia.feed.dataclient;

import android.content.Context;

import androidx.annotation.NonNull;

import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.feed.model.Card;

import java.util.List;

public interface FeedClient {
    void request(@NonNull Context context, @NonNull WikiSite wiki, int age, @NonNull final Callback cb);
    void cancel();

    interface Callback {
        void success(@NonNull List<? extends Card> cards);
        void error(@NonNull Throwable caught);
    }
}
