package com.wikipedia.feed.view;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;
import com.wikipedia.feed.model.Card;

import com.wikipedia.R;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.feed.model.Card;
import com.wikipedia.util.ResourceUtil;

import static com.wikipedia.util.L10nUtil.isLangRTL;

public abstract class DefaultFeedCardView<T extends Card> extends MaterialCardView implements FeedCardView<T> {
    @Nullable private T card;
    @Nullable private FeedAdapter.Callback callback;

    public DefaultFeedCardView(Context context) {
        super(context);
        setCardBackgroundColor(ResourceUtil.getThemedColor(context, R.attr.paper_color));
    }

    @Override public void setCard(@NonNull T card) {
        this.card = card;
    }

    @Nullable @Override public T getCard() {
        return card;
    }

    @Override public void setCallback(@Nullable FeedAdapter.Callback callback) {
        this.callback = callback;
    }

    protected void setAllowOverflow(boolean enabled) {
        setClipChildren(!enabled);
        setClipToOutline(!enabled);
    }

    @Nullable protected FeedAdapter.Callback getCallback() {
        return callback;
    }

    protected void setLayoutDirectionByWikiSite(@NonNull WikiSite wiki, @NonNull View rootView) {
        rootView.setLayoutDirection(isLangRTL(wiki.languageCode()) ? LAYOUT_DIRECTION_RTL : LAYOUT_DIRECTION_LTR);
    }
}
