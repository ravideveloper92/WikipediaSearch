package com.wikipedia.database.column;

import android.database.Cursor;

import androidx.annotation.NonNull;

import com.wikipedia.model.CodeEnum;

public class CodeEnumColumn<T> extends Column<T> {
    @NonNull private final CodeEnum<T> codeEnum;

    public CodeEnumColumn(@NonNull String tbl, @NonNull String name, @NonNull CodeEnum<T> codeEnum) {
        super(tbl, name, "integer not null");
        this.codeEnum = codeEnum;
    }

    @Override public T val(@NonNull Cursor cursor) {
        return codeEnum.enumeration(getInt(cursor));
    }
}
