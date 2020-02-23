package com.wikipedia.database.contract;

import android.net.Uri;

import com.wikipedia.database.DbUtil;
import com.wikipedia.database.column.IdColumn;
import com.wikipedia.database.column.LongColumn;
import com.wikipedia.database.column.NamespaceColumn;
import com.wikipedia.database.column.StrColumn;

@SuppressWarnings("checkstyle:interfaceistype")
public interface ReadingListPageContract {
    String TABLE = "localreadinglistpage";
    Uri URI = Uri.withAppendedPath(AppContentProviderContract.AUTHORITY_BASE, "/locallistpage");

    interface Col {
        IdColumn ID = new IdColumn(TABLE);
        LongColumn LISTID = new LongColumn(TABLE, "listId", "integer");
        StrColumn SITE = new StrColumn(TABLE, "site", "text not null");
        StrColumn LANG = new StrColumn(TABLE, "lang", "text");
        NamespaceColumn NAMESPACE = new NamespaceColumn(TABLE, "namespace");
        StrColumn DISPLAY_TITLE = new StrColumn(TABLE, "title", "text not null"); // display title
        StrColumn API_TITLE = new StrColumn(TABLE, "apiTitle", "string"); // the "original" title
        LongColumn MTIME = new LongColumn(TABLE, "mtime", "integer not null");
        LongColumn ATIME = new LongColumn(TABLE, "atime", "integer not null");
        StrColumn THUMBNAIL_URL = new StrColumn(TABLE, "thumbnailUrl", "text");
        StrColumn DESCRIPTION = new StrColumn(TABLE, "description", "text");
        LongColumn REVID = new LongColumn(TABLE, "revId", "integer");
        LongColumn OFFLINE = new LongColumn(TABLE, "offline", "integer");
        LongColumn STATUS = new LongColumn(TABLE, "status", "integer");
        LongColumn SIZEBYTES = new LongColumn(TABLE, "sizeBytes", "integer");
        LongColumn REMOTEID = new LongColumn(TABLE, "remoteId", "integer not null");

        String[] SELECTION = DbUtil.qualifiedNames(API_TITLE);
        String[] ALL = DbUtil.qualifiedNames(ID, LISTID, SITE, LANG, NAMESPACE, DISPLAY_TITLE, API_TITLE, MTIME, ATIME,
                THUMBNAIL_URL, DESCRIPTION, REVID, OFFLINE, STATUS, SIZEBYTES, REMOTEID);
    }
}
