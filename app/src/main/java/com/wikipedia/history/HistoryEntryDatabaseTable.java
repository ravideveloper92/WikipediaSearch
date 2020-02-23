package com.wikipedia.history;

import android.content.ContentValues;
import android.database.Cursor;

import com.wikipedia.database.DatabaseTable;
import com.wikipedia.database.column.Column;
import com.wikipedia.database.contract.PageHistoryContract;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.page.PageTitle;

import androidx.annotation.NonNull;

import com.wikipedia.database.DatabaseTable;
import com.wikipedia.database.column.Column;
import com.wikipedia.database.contract.PageHistoryContract;
import com.wikipedia.database.contract.PageHistoryContract.Col;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.page.PageTitle;

import java.util.Date;

public class HistoryEntryDatabaseTable extends DatabaseTable<HistoryEntry> {
    private static final int DB_VER_NAMESPACE_ADDED = 6;
    private static final int DB_VER_LANG_ADDED = 10;
    private static final int DB_VER_TIME_SPENT_ADDED = 15;
    private static final int DB_VER_DISPLAY_TITLE_ADDED = 19;

    public HistoryEntryDatabaseTable() {
        super(PageHistoryContract.TABLE, PageHistoryContract.Page.URI);
    }

    @Override
    public HistoryEntry fromCursor(Cursor cursor) {
        WikiSite wiki = new WikiSite(PageHistoryContract.Col.SITE.val(cursor), PageHistoryContract.Col.LANG.val(cursor));
        PageTitle title = new PageTitle(PageHistoryContract.Col.NAMESPACE.val(cursor), PageHistoryContract.Col.API_TITLE.val(cursor), wiki);
        Date timestamp = PageHistoryContract.Col.TIMESTAMP.val(cursor);
        int source = PageHistoryContract.Col.SOURCE.val(cursor);
        title.setDisplayText(PageHistoryContract.Col.DISPLAY_TITLE.val(cursor));
        return new HistoryEntry(title, timestamp, source);
    }

    @Override
    protected ContentValues toContentValues(HistoryEntry obj) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PageHistoryContract.Col.SITE.getName(), obj.getTitle().getWikiSite().authority());
        contentValues.put(PageHistoryContract.Col.LANG.getName(), obj.getTitle().getWikiSite().languageCode());
        contentValues.put(PageHistoryContract.Col.API_TITLE.getName(), obj.getTitle().getText());
        contentValues.put(PageHistoryContract.Col.DISPLAY_TITLE.getName(), obj.getTitle().getDisplayText());
        contentValues.put(PageHistoryContract.Col.NAMESPACE.getName(), obj.getTitle().getNamespace());
        contentValues.put(PageHistoryContract.Col.TIMESTAMP.getName(), obj.getTimestamp().getTime());
        contentValues.put(PageHistoryContract.Col.SOURCE.getName(), obj.getSource());
        contentValues.put(PageHistoryContract.Col.TIME_SPENT.getName(), obj.getTimeSpentSec());
        return contentValues;
    }

    @NonNull
    @Override
    public Column<?>[] getColumnsAdded(int version) {
        switch (version) {
            case INITIAL_DB_VERSION:
                return new Column<?>[] {PageHistoryContract.Col.ID, PageHistoryContract.Col.SITE, PageHistoryContract.Col.API_TITLE, PageHistoryContract.Col.TIMESTAMP, PageHistoryContract.Col.SOURCE};
            case DB_VER_NAMESPACE_ADDED:
                return new Column<?>[] {PageHistoryContract.Col.NAMESPACE};
            case DB_VER_LANG_ADDED:
                return new Column<?>[] {PageHistoryContract.Col.LANG};
            case DB_VER_TIME_SPENT_ADDED:
                return new Column<?>[] {PageHistoryContract.Col.TIME_SPENT};
            case DB_VER_DISPLAY_TITLE_ADDED:
                return new Column<?>[] {PageHistoryContract.Col.DISPLAY_TITLE};
            default:
                return super.getColumnsAdded(version);
        }
    }

    @Override
    protected String getPrimaryKeySelection(@NonNull HistoryEntry obj,
                                            @NonNull String[] selectionArgs) {
        return super.getPrimaryKeySelection(obj, PageHistoryContract.Col.SELECTION);
    }

    @Override
    protected String[] getUnfilteredPrimaryKeySelectionArgs(@NonNull HistoryEntry obj) {
        return new String[] {
                obj.getTitle().getWikiSite().authority(),
                obj.getTitle().getWikiSite().languageCode(),
                obj.getTitle().getNamespace(),
                obj.getTitle().getText()
        };
    }

    @Override
    protected int getDBVersionIntroducedAt() {
        return INITIAL_DB_VERSION;
    }
}
