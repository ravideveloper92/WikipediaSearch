package com.wikipedia.recurring;

import android.content.Context;

import com.wikipedia.WikipediaApp;

import androidx.annotation.NonNull;

import com.wikipedia.R;
import com.wikipedia.WikipediaApp;
import com.wikipedia.analytics.DailyStatsFunnel;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DailyEventTask extends RecurringTask {
    @NonNull private final String name;

    public DailyEventTask(Context context) {
        name = context.getString(R.string.preference_key_daily_event_time_task_name);
    }

    @Override
    protected boolean shouldRun(Date lastRun) {
        return isDailyEventDue(lastRun);
    }

    @Override
    protected void run(Date lastRun) {
        onDailyEvent();
    }

    @Override
    @NonNull
    protected String getName() {
        return name;
    }

    private void onDailyEvent() {
        logDailyEventReport();
    }

    private void logDailyEventReport() {
        new DailyStatsFunnel(WikipediaApp.getInstance()).log(WikipediaApp.getInstance());
    }

    private boolean isDailyEventDue(Date lastRun) {
        return timeSinceLastDailyEvent(lastRun) > TimeUnit.DAYS.toMillis(1);
    }

    private long timeSinceLastDailyEvent(Date lastRun) {
        return Math.min(Integer.MAX_VALUE,
                Math.max(0, getAbsoluteTime() - lastRun.getTime()));
    }
}
