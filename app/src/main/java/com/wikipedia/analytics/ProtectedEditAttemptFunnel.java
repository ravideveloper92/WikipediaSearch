package com.wikipedia.analytics;

import com.wikipedia.WikipediaApp;
import com.wikipedia.dataclient.WikiSite;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import com.wikipedia.WikipediaApp;
import com.wikipedia.dataclient.WikiSite;

public class ProtectedEditAttemptFunnel extends Funnel {
    private static final String SCHEMA_NAME = "MobileWikiAppProtectedEditAttempt";
    private static final int REV_ID = 18118725;

    public ProtectedEditAttemptFunnel(WikipediaApp app, WikiSite wiki) {
        super(app, SCHEMA_NAME, REV_ID, wiki);
    }

    @Override protected void preprocessSessionToken(@NonNull JSONObject eventData) { }

    public void log(String protectionStatus) {
        log(
                "protectionStatus", protectionStatus
        );
    }
}
