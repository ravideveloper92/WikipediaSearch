package com.wikipedia.dataclient.wikidata;

import androidx.annotation.Nullable;

import com.wikipedia.dataclient.mwapi.MwPostResponse;

@SuppressWarnings("unused")
public class EntityPostResponse extends MwPostResponse {
    @Nullable private Entities.Entity entity;

    @Nullable public Entities.Entity getEntity() {
        return entity;
    }
}
