package com.wikipedia.database.contract;

import android.net.Uri;
import android.provider.BaseColumns;

import com.wikipedia.database.DbUtil;
import com.wikipedia.database.column.DateColumn;
import com.wikipedia.database.column.LongColumn;
import com.wikipedia.database.column.StrColumn;

@SuppressWarnings("checkstyle:interfaceistype")
public interface SearchHistoryContract {
    String TABLE = "recentsearches";

    interface Col {
        LongColumn ID = new LongColumn(TABLE, BaseColumns._ID, "integer primary key");
        StrColumn TEXT = new StrColumn(TABLE, "text", "string");
        DateColumn TIMESTAMP = new DateColumn(TABLE, "timestamp", "integer");

        String[] SELECTION = DbUtil.qualifiedNames(TEXT);
    }

    interface Query extends Col {
        String TABLES = TABLE;
        String PATH = "history/query";
        Uri URI = Uri.withAppendedPath(AppContentProviderContract.AUTHORITY_BASE, PATH);
        String[] PROJECTION = null;
        String ORDER_MRU = TIMESTAMP.qualifiedName() + " desc";
    }
}
