package com.wikipedia.analytics;

import com.wikipedia.Constants;
import com.wikipedia.WikipediaApp;
import com.wikipedia.dataclient.WikiSite;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import com.wikipedia.Constants.InvokeSource;
import com.wikipedia.WikipediaApp;
import com.wikipedia.dataclient.WikiSite;

public class RandomizerFunnel extends TimedFunnel {
    private static final String SCHEMA_NAME = "MobileWikiAppRandomizer";
    private static final int REV_ID = 18118733;

    private final Constants.InvokeSource source;
    private int numSwipesForward;
    private int numSwipesBack;
    private int numClicksForward;
    private int numClicksBack;

    public RandomizerFunnel(WikipediaApp app, WikiSite wiki, Constants.InvokeSource source) {
        super(app, SCHEMA_NAME, REV_ID, SAMPLE_LOG_ALL, wiki);
        this.source = source;
    }

    @Override protected void preprocessSessionToken(@NonNull JSONObject eventData) { }

    public void swipedForward() {
        numSwipesForward++;
    }

    public void swipedBack() {
        numSwipesBack++;
    }

    public void clickedForward() {
        numClicksForward++;
    }

    public void clickedBack() {
        numClicksBack++;
    }

    public void done() {
        log(
                "source", source.ordinal(),
                "fingerSwipesForward", numSwipesForward,
                "fingerSwipesBack", numSwipesBack,
                "diceClicks", numClicksForward,
                "backClicks", numClicksBack
        );
    }
}
