package com.wikipedia.page.shareafact;

import com.wikipedia.util.FileUtil;
import com.wikipedia.WikipediaApp;
import com.wikipedia.recurring.RecurringTask;
import com.wikipedia.util.FileUtil;
import com.wikipedia.util.ShareUtil;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Mainly to clean up images shared through SnippetShareAdapter.
 */
public class SharedImageCleanupTask extends RecurringTask {
    private static final long RUN_INTERVAL_MILLI = TimeUnit.DAYS.toMillis(1);

    @Override
    protected boolean shouldRun(Date lastRun) {
        return System.currentTimeMillis() - lastRun.getTime() >= RUN_INTERVAL_MILLI;
    }

    @Override
    protected void run(Date lastRun) {
        FileUtil.deleteRecursively(new File(ShareUtil.getShareFolder(WikipediaApp.getInstance()), "share"));
    }

    @Override
    protected String getName() {
        return "shared-image-cleanup";
    }
}
