package com.wikipedia.events;

import com.wikipedia.readinglist.database.ReadingListPage;
import com.wikipedia.readinglist.database.ReadingListPage;

public class ArticleSavedOrDeletedEvent {
    private ReadingListPage[] pages;

    private boolean isAdded;

    public ReadingListPage[] getPages() {
        return pages;
    }

    public ArticleSavedOrDeletedEvent(boolean isAdded, ReadingListPage... pages) {
        this.pages = pages;
        this.isAdded = isAdded;
    }

    public boolean isAdded() {
        return isAdded;
    }

}
