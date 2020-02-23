package com.wikipedia.search;

import android.content.ContentValues;
import android.database.Cursor;

import com.wikipedia.database.DatabaseTable;
import com.wikipedia.database.column.Column;
import com.wikipedia.database.contract.SearchHistoryContract;

import androidx.annotation.NonNull;

import com.wikipedia.database.DatabaseTable;
import com.wikipedia.database.column.Column;
import com.wikipedia.database.contract.SearchHistoryContract;
import com.wikipedia.database.contract.SearchHistoryContract.Col;

import java.util.Date;

public class RecentSearchDatabaseTable extends DatabaseTable<RecentSearch> {
    private static final int DB_VER_INTRODUCED = 5;

    public RecentSearchDatabaseTable() {
        super(SearchHistoryContract.TABLE, SearchHistoryContract.Query.URI);
    }

    @Override
    public RecentSearch fromCursor(Cursor cursor) {
        String title = SearchHistoryContract.Col.TEXT.val(cursor);
        Date timestamp = SearchHistoryContract.Col.TIMESTAMP.val(cursor);
        return new RecentSearch(title, timestamp);
    }

    @Override
    protected ContentValues toContentValues(RecentSearch obj) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SearchHistoryContract.Col.TEXT.getName(), obj.getText());
        contentValues.put(SearchHistoryContract.Col.TIMESTAMP.getName(), obj.getTimestamp().getTime());
        return contentValues;
    }

    @Override
    protected int getDBVersionIntroducedAt() {
        return DB_VER_INTRODUCED;
    }

    @NonNull
    @Override
    public Column<?>[] getColumnsAdded(int version) {
        switch (version) {
            case DB_VER_INTRODUCED:
                return new Column<?>[] {SearchHistoryContract.Col.ID, SearchHistoryContract.Col.TEXT, SearchHistoryContract.Col.TIMESTAMP};
            default:
                return super.getColumnsAdded(version);
        }
    }

    @Override
    protected String getPrimaryKeySelection(@NonNull RecentSearch obj, @NonNull String[] selectionArgs) {
        return super.getPrimaryKeySelection(obj, SearchHistoryContract.Col.SELECTION);
    }

    @Override
    protected String[] getUnfilteredPrimaryKeySelectionArgs(@NonNull RecentSearch obj) {
        return new String[] {obj.getText()};
    }
}
