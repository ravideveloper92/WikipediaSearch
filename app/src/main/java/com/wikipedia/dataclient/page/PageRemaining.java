package com.wikipedia.dataclient.page;

import com.wikipedia.page.Section;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.page.Section;

import java.util.Collections;
import java.util.List;

public class PageRemaining {
    @SuppressWarnings("unused") @Nullable private List<Section> sections;

    @NonNull public List<Section> sections() {
        if (sections == null) {
            return Collections.emptyList();
        }
        return sections;
    }
}
