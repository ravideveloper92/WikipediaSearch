package com.wikipedia.edit.richtext;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.wikipedia.R;
import com.wikipedia.util.ResourceUtil;

import static com.wikipedia.util.ResourceUtil.getThemedColor;

public enum SyntaxRuleStyle {
    TEMPLATE {
        @NonNull @Override public SpanExtents createSpan(@NonNull Context ctx, int spanStart,
                                                         SyntaxRule syntaxItem) {
            @ColorInt int color = getThemedColor(ctx, R.attr.secondary_text_color);
            return new ColorSpanEx(color, Color.TRANSPARENT, spanStart, syntaxItem);
        }
    },
    INTERNAL_LINK {
        @NonNull @Override public SpanExtents createSpan(@NonNull Context ctx, int spanStart,
                                                         SyntaxRule syntaxItem) {
            @ColorInt int color = getThemedColor(ctx, R.attr.colorAccent);
            return new ColorSpanEx(color, Color.TRANSPARENT, spanStart, syntaxItem);
        }
    },
    EXTERNAL_LINK {
        @NonNull @Override public SpanExtents createSpan(@NonNull Context ctx, int spanStart,
                                                         SyntaxRule syntaxItem) {
            @ColorInt int color = getThemedColor(ctx, R.attr.colorAccent);
            return new ColorSpanEx(color, Color.TRANSPARENT, spanStart, syntaxItem);
        }
    },
    REF {
        @NonNull @Override public SpanExtents createSpan(@NonNull Context ctx, int spanStart,
                                                         SyntaxRule syntaxItem) {
            return new ColorSpanEx(ResourceUtil.getThemedColor(ctx, R.attr.green_highlight_color), Color.TRANSPARENT, spanStart,
                    syntaxItem);
        }
    },
    BOLD_ITALIC {
        @NonNull @Override public SpanExtents createSpan(@NonNull Context ctx, int spanStart,
                                                         SyntaxRule syntaxItem) {
            return new StyleSpanEx(Typeface.BOLD_ITALIC, spanStart, syntaxItem);
        }
    },
    BOLD {
        @NonNull @Override public SpanExtents createSpan(@NonNull Context ctx, int spanStart,
                                                         SyntaxRule syntaxItem) {
            return new StyleSpanEx(Typeface.BOLD, spanStart, syntaxItem);
        }
    },
    ITALIC {
        @NonNull @Override public SpanExtents createSpan(@NonNull Context ctx, int spanStart,
                                                         SyntaxRule syntaxItem) {
            return new StyleSpanEx(Typeface.ITALIC, spanStart, syntaxItem);
        }
    },
    SEARCH_MATCHES {
        @NonNull @Override public SpanExtents createSpan(@NonNull Context ctx, int spanStart, SyntaxRule syntaxItem) {
            @ColorInt int foreColor = ctx.getResources().getColor(android.R.color.black);
            @ColorInt int backColor = ctx.getResources().getColor(R.color.find_in_page);
            return new ColorSpanEx(foreColor, backColor, spanStart, syntaxItem);
        }
    },
    SEARCH_MATCH_SELECTED {
        @NonNull @Override public SpanExtents createSpan(@NonNull Context ctx, int spanStart, SyntaxRule syntaxItem) {
            @ColorInt int foreColor = ctx.getResources().getColor(android.R.color.black);
            @ColorInt int backColor = ctx.getResources().getColor(R.color.find_in_page_active);
            return new ColorSpanEx(foreColor, backColor, spanStart, syntaxItem);
        }
    };

    @NonNull public abstract SpanExtents createSpan(@NonNull Context ctx, int spanStart,
                                                    SyntaxRule syntaxItem);
}
