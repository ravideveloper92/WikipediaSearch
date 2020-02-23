package com.wikipedia.descriptions;

import android.text.TextUtils;

import com.wikipedia.page.Page;
import com.wikipedia.page.PageProperties;

import androidx.annotation.NonNull;

import com.wikipedia.page.Page;
import com.wikipedia.page.PageProperties;

public final class DescriptionEditUtil {
    static final String ABUSEFILTER_DISALLOWED = "abusefilter-disallowed";
    static final String ABUSEFILTER_WARNING = "abusefilter-warning";
    private static final String DESCRIPTION_SOURCE_LOCAL = "local";
    private static final String DESCRIPTION_SOURCE_WIKIDATA = "central";

    public static boolean isEditAllowed(@NonNull Page page) {
        PageProperties props = page.getPageProperties();
        return !TextUtils.isEmpty(props.getWikiBaseItem())
                && !DESCRIPTION_SOURCE_LOCAL.equals(props.getDescriptionSource());
    }

    private DescriptionEditUtil() {
    }
}
