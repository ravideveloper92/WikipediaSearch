package com.wikipedia.recurring;

import com.wikipedia.WikipediaApp;
import com.wikipedia.alphaupdater.AlphaUpdateChecker;
import com.wikipedia.page.shareafact.SharedImageCleanupTask;
import com.wikipedia.WikipediaApp;
import com.wikipedia.alphaupdater.AlphaUpdateChecker;
import com.wikipedia.page.shareafact.SharedImageCleanupTask;
import com.wikipedia.settings.RemoteConfigRefreshTask;
import com.wikipedia.util.ReleaseUtil;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

public class RecurringTasksExecutor {
    private final WikipediaApp app;

    public RecurringTasksExecutor(WikipediaApp app) {
        this.app = app;
    }

    public void run() {
        Completable.fromAction(() -> {
            RecurringTask[] allTasks = new RecurringTask[] {
                    // Has list of all rotating tasks that need to be run
                    new RemoteConfigRefreshTask(),
                    new SharedImageCleanupTask(),
                    new DailyEventTask(app)
            };
            for (RecurringTask task: allTasks) {
                task.runIfNecessary();
            }
            if (ReleaseUtil.isAlphaRelease()) {
                new AlphaUpdateChecker(app).runIfNecessary();
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }
}
