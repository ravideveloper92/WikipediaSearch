package com.wikipedia.settings;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.wikipedia.page.LinkMovementMethodExt;
import com.wikipedia.util.UriUtil;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.preference.PreferenceViewHolder;

import com.wikipedia.R;
import com.wikipedia.page.LinkMovementMethodExt;
import com.wikipedia.util.ResourceUtil;

import static com.wikipedia.util.UriUtil.handleExternalLink;

public class SwitchPreferenceWithLinks extends SwitchPreferenceMultiLine {

    private LinkMovementMethodExt movementMethod
            = new LinkMovementMethodExt((@NonNull String url) -> UriUtil.handleExternalLink(getContext(), Uri.parse(url)));

    private View.OnClickListener onClickListener = (v) -> {
        SwitchPreferenceWithLinks sw = SwitchPreferenceWithLinks.this;
        sw.setChecked(!sw.isChecked());
        sw.callChangeListener(sw.isChecked());
    };

    public SwitchPreferenceWithLinks(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public SwitchPreferenceWithLinks(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public SwitchPreferenceWithLinks(Context ctx) {
        super(ctx);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView view = (TextView) holder.findViewById(android.R.id.summary);
        view.setMovementMethod(movementMethod);
        view.setOnClickListener(onClickListener);
        view.setBackground(AppCompatResources.getDrawable(getContext(),
                ResourceUtil.getThemedAttributeId(getContext(), R.attr.selectableItemBackgroundBorderless)));
    }
}
