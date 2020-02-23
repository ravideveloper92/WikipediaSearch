package com.wikipedia.database.column;

import com.wikipedia.page.Namespace;

import androidx.annotation.NonNull;

import com.wikipedia.page.Namespace;

public class NamespaceColumn extends CodeEnumColumn<Namespace> {
    public NamespaceColumn(@NonNull String tbl, @NonNull String name) {
        super(tbl, name, Namespace.CODE_ENUM);
    }
}
