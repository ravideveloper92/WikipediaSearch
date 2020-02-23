package com.wikipedia.feed.image;

import com.wikipedia.json.PostProcessingTypeAdapter;
import com.wikipedia.json.annotations.Required;

import androidx.annotation.NonNull;

import com.wikipedia.gallery.GalleryItem;
import com.wikipedia.gallery.ImageInfo;
import com.wikipedia.json.PostProcessingTypeAdapter;
import com.wikipedia.json.annotations.Required;

public final class FeaturedImage extends GalleryItem implements PostProcessingTypeAdapter.PostProcessable {
    @SuppressWarnings("unused,NullableProblems") @Required
    @NonNull private String title;
    @SuppressWarnings("unused,NullableProblems") @Required @NonNull private ImageInfo image;

    private int age;

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    @NonNull
    public String title() {
        return title;
    }

    @Override
    public void postProcess() {
        setTitle(title);
        getOriginal().setSource(image.getSource());
    }
}
