package com.wikipedia.analytics;

import com.wikipedia.WikipediaApp;
import com.wikipedia.notifications.Notification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;
import com.wikipedia.WikipediaApp;
import com.wikipedia.notifications.Notification;

public class NotificationFunnel extends Funnel {
    private static final String SCHEMA_NAME = "MobileWikiAppNotificationInteraction";
    private static final int REV_ID = 18325732;

    private final Notification notification;

    public NotificationFunnel(WikipediaApp app, Notification notification) {
        super(app, SCHEMA_NAME, REV_ID);
        this.notification = notification;
    }

    @Override
    protected JSONObject preprocessData(@NonNull JSONObject eventData) {
        preprocessData(eventData, "notification_id", notification.id());
        preprocessData(eventData, "notification_wiki", notification.wiki());
        preprocessData(eventData, "notification_type", notification.type());
        return super.preprocessData(eventData);
    }

    @Override protected void preprocessSessionToken(@NonNull JSONObject eventData) { }

    public void logMarkRead(@Nullable Long selectionToken) {
        log(
                "action_rank", 0,
                "selection_token", selectionToken
        );
    }

    public void logAction(int index, @NonNull Notification.Link link) {
        log(
                "action_rank", index + 1,
                "action_icon", link.getIcon()
        );
    }
}
