package com.wikipedia.feed.image;

import android.net.Uri;

import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.model.CardType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.R;
import com.wikipedia.WikipediaApp;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.model.CardType;
import com.wikipedia.util.DateUtil;

public class FeaturedImageCard extends Card {
    @NonNull private FeaturedImage featuredImage;
    private int age;
    @NonNull private WikiSite wiki;

    public FeaturedImageCard(@NonNull FeaturedImage featuredImage, int age, @NonNull WikiSite wiki) {
        this.featuredImage = featuredImage;
        this.age = age;
        this.wiki = wiki;
    }

    @NonNull
    public FeaturedImage baseImage() {
        return featuredImage;
    }

    public int age() {
        return age;
    }

    @Override
    @NonNull
    public String title() {
        return WikipediaApp.getInstance().getString(R.string.view_featured_image_card_title);
    }

    @Override
    @NonNull
    public String subtitle() {
        return DateUtil.getFeedCardDateString(age);
    }

    @NonNull
    public WikiSite wikiSite() {
        return wiki;
    }

    @NonNull
    public String filename() {
        return featuredImage.title();
    }

    @Override
    @NonNull
    public Uri image() {
        return Uri.parse(featuredImage.getThumbnailUrl());
    }

    @NonNull @Override public CardType type() {
        return CardType.FEATURED_IMAGE;
    }

    @Nullable
    public String description() {
        return featuredImage.getDescription().getText();
    }

    @Override
    protected int dismissHashCode() {
        return featuredImage.title().hashCode();
    }
}
