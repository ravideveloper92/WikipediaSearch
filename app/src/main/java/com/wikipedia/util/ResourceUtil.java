package com.wikipedia.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.TypedValue;
import android.view.MenuItem;

import androidx.annotation.AnyRes;
import androidx.annotation.ArrayRes;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.MenuItemCompat;

public final class ResourceUtil {
    // See Resources.getIdentifier().
    private static final int NO_ID = 0;

    public static int[] getIdArray(Context context, @ArrayRes int id) {
        return getIdArray(context.getResources(), id);
    }

    public static int[] getIdArray(Resources resources, @ArrayRes int id) {
        TypedArray typedArray = resources.obtainTypedArray(id);
        int[] ids = new int[typedArray.length()];
        for (int i = 0; i < typedArray.length(); ++i) {
            @IdRes int itemId = typedArray.getResourceId(i, NO_ID);
            ids[i] = itemId;
            checkId(itemId);
        }
        typedArray.recycle();
        return ids;
    }

    @NonNull
    public static Bitmap bitmapFromVectorDrawable(@NonNull Context context, @DrawableRes int id, @ColorRes Integer tintColor) {
        Drawable vectorDrawable = AppCompatResources.getDrawable(context, id);
        int width = vectorDrawable.getIntrinsicWidth();
        int height = vectorDrawable.getIntrinsicHeight();
        vectorDrawable.setBounds(0, 0, width, height);
        if (tintColor != null) {
            DrawableCompat.setTint(vectorDrawable, ContextCompat.getColor(context, tintColor));
        }
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return bm;
    }

    @Nullable public static TypedValue getThemedAttribute(@NonNull Context context, @AttrRes int id) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(id, typedValue, true)) {
            return typedValue;
        }
        return null;
    }

    /**
     * Resolves the resource ID of a theme-dependent attribute (for example, a color value
     * that changes based on the selected theme)
     * @param context The context whose theme contains the attribute.
     * @param id Theme-dependent attribute ID to be resolved.
     * @return The actual resource ID of the requested theme-dependent attribute.
     */
    @AnyRes public static int getThemedAttributeId(@NonNull Context context, @AttrRes int id) {
        TypedValue typedValue = getThemedAttribute(context, id);
        if (typedValue == null) {
            throw new IllegalArgumentException("Attribute not found; ID=" + id);
        }
        return typedValue.resourceId;
    }

    @ColorInt public static int getThemedColor(@NonNull Context context, @AttrRes int id) {
        TypedValue typedValue = getThemedAttribute(context, id);
        if (typedValue == null) {
            throw new IllegalArgumentException("Attribute not found; ID=" + id);
        }
        return typedValue.data;
    }

    public static Uri uri(@NonNull Context context, @AnyRes int id) throws Resources.NotFoundException {
        Resources res = context.getResources();
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(res.getResourcePackageName(id))
                .appendPath(res.getResourceTypeName(id))
                .appendPath(res.getResourceEntryName(id))
                .build();
    }

    public static void setMenuItemTint(@NonNull Context context, @NonNull MenuItem item, @AttrRes int colorAttr) {
        MenuItemCompat.setIconTintList(item, ColorStateList.valueOf(ResourceUtil.getThemedColor(context, colorAttr)));
    }

    private static void checkId(@IdRes int id) {
        if (!isIdValid(id)) {
            throw new RuntimeException("id is invalid");
        }
    }

    private static boolean isIdValid(@IdRes int id) {
        return id != NO_ID;
    }

    private ResourceUtil() { }
}
