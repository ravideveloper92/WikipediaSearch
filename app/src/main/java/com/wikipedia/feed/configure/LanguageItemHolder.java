package com.wikipedia.feed.configure;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.wikipedia.R;
import com.wikipedia.util.ResourceUtil;
import com.wikipedia.views.DefaultViewHolder;

public class LanguageItemHolder extends DefaultViewHolder<View> {
    private Context context;
    private TextView langCodeView;

    LanguageItemHolder(Context context, View itemView) {
        super(itemView);
        this.context = context;
        langCodeView = itemView.findViewById(R.id.feed_content_type_lang_code);
    }

    void bindItem(@NonNull String langCode, boolean enabled) {
        langCodeView.setText(langCode);
        langCodeView.setTextColor(enabled ? ContextCompat.getColor(context, android.R.color.white)
                : ResourceUtil.getThemedColor(context, R.attr.material_theme_de_emphasised_color));
        langCodeView.setBackground(AppCompatResources.getDrawable(context,
                enabled ? R.drawable.lang_button_shape : R.drawable.lang_button_shape_border));
        langCodeView.getBackground().setColorFilter(enabled ? ContextCompat.getColor(context, R.color.base30)
                        : ResourceUtil.getThemedColor(context, R.attr.material_theme_de_emphasised_color),
                PorterDuff.Mode.SRC_IN);
    }
}
