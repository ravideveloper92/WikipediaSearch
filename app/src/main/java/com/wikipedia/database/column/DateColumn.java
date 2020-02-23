package com.wikipedia.database.column;

import android.database.Cursor;

import androidx.annotation.NonNull;

import java.util.Date;

public class DateColumn extends Column<Date> {
    public DateColumn(@NonNull String tbl, @NonNull String name, @NonNull String type) {
        super(tbl, name, type);
    }

    @Override
    public Date val(@NonNull Cursor cursor) {
        return new Date(getLong(cursor));
    }
}
