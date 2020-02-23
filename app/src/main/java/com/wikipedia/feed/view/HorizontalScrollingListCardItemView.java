package com.wikipedia.feed.view;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.wikipedia.R;
import com.wikipedia.richtext.RichTextUtil;
import com.wikipedia.util.ResourceUtil;
import com.wikipedia.views.FaceAndColorDetectImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HorizontalScrollingListCardItemView extends CardView {
    @BindView(R.id.horizontal_scroll_list_item_image) FaceAndColorDetectImageView imageView;
    @BindView(R.id.horizontal_scroll_list_item_text) TextView textView;
    @Nullable private FeedAdapter.Callback callback;

    public HorizontalScrollingListCardItemView(@NonNull Context context) {
        super(context);
        inflate(getContext(), R.layout.view_horizontal_scroll_list_item_card, this);
        setCardBackgroundColor(ResourceUtil.getThemedColor(context, R.attr.paper_color));
        ButterKnife.bind(this);
        imageView.setLegacyVisibilityHandlingEnabled(true);
    }

    public void setCallback(@Nullable FeedAdapter.Callback callback) {
        this.callback = callback;
    }

    @Nullable
    public FeedAdapter.Callback getCallback() {
        return callback;
    }

    public void setText(@NonNull CharSequence text) {
        textView.setText(text);
        RichTextUtil.removeUnderlinesFromLinksAndMakeBold(textView);
    }

    public void setImage(@Nullable Uri image) {
        if (image == null) {
            imageView.setVisibility(GONE);
            textView.setMaxLines(10);
        } else {
            imageView.setVisibility(VISIBLE);
            imageView.loadImage(image);
        }
    }

    public View getImageView() {
        return imageView;
    }
}
