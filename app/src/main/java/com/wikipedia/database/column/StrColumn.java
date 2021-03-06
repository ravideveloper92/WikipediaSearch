package com.wikipedia.database.column;

import android.database.Cursor;

import androidx.annotation.NonNull;

public class StrColumn extends Column<String> {
    public StrColumn(@NonNull String tbl, @NonNull String name, @NonNull String type) {
        super(tbl, name, type);
    }

    @Override
    public String val(@NonNull Cursor cursor) {
        return getString(cursor);
    }
}
