package com.wikipedia.page.references;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.wikipedia.util.StringUtil;

import com.wikipedia.R;
import com.wikipedia.page.LinkHandler;
import com.wikipedia.page.LinkMovementMethodExt;
import com.wikipedia.util.DimenUtil;
import com.wikipedia.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.wikipedia.util.L10nUtil.setConditionalLayoutDirection;

/**
 * A dialog that displays the currently clicked reference.
 */
public class ReferenceDialog extends BottomSheetDialog {
    @BindView(R.id.reference_pager) ViewPager2 referencesViewPager;
    @BindView(R.id.page_indicator_view) TabLayout pageIndicatorView;
    @BindView(R.id.indicator_divider) View pageIndicatorDivider;
    @BindView(R.id.reference_title_text) TextView titleTextView;
    private LinkHandler referenceLinkHandler;

    public ReferenceDialog(@NonNull Context context, int selectedIndex, List<References.Reference> adjacentReferences, LinkHandler referenceLinkHandler) {
        super(context);
        View rootView = LayoutInflater.from(context).inflate(R.layout.fragment_references_pager, null);
        setContentView(rootView);
        ButterKnife.bind(this);
        this.referenceLinkHandler = referenceLinkHandler;

        if (adjacentReferences.size() == 1) {
            pageIndicatorView.setVisibility(View.GONE);
            ((ViewGroup) pageIndicatorView.getParent()).removeView(pageIndicatorView);
            pageIndicatorDivider.setVisibility(View.GONE);
        } else {
            BottomSheetBehavior behavior = BottomSheetBehavior.from((View) rootView.getParent());
            behavior.setPeekHeight(DimenUtil.getDisplayHeightPx() / 2);
        }
        titleTextView.setText(getContext().getString(R.string.reference_title, ""));

        referencesViewPager.setOffscreenPageLimit(2);
        referencesViewPager.setAdapter(new ReferencesAdapter(adjacentReferences));
        new TabLayoutMediator(pageIndicatorView, referencesViewPager, (tab, position) -> { }).attach();
        referencesViewPager.setCurrentItem(selectedIndex, true);

        setConditionalLayoutDirection(rootView, referenceLinkHandler.getWikiSite().languageCode());
    }

    @NonNull private String processLinkTextWithAlphaReferences(@NonNull String linkText) {
        boolean isLowercase = linkText.contains("lower");
        if (linkText.contains("alpha ")) {
            String[] strings = linkText.split(" ");
            String alphaReference = StringUtil.getBase26String(Integer.valueOf(strings[strings.length - 1].replace("]", "")));
            alphaReference = isLowercase ? alphaReference.toLowerCase() : alphaReference;
            linkText = alphaReference;
        }
        return linkText.replace("[", "").replace("]", "") + ".";
    }

    @Override
    public void onBackPressed() {
        if (referencesViewPager.getCurrentItem() > 0) {
            referencesViewPager.setCurrentItem(referencesViewPager.getCurrentItem() - 1, true);
        } else {
            super.onBackPressed();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView pagerReferenceText;
        private TextView pagerIdText;

        ViewHolder(View itemView) {
            super(itemView);
            pagerReferenceText = itemView.findViewById(R.id.reference_text);
            pagerReferenceText.setMovementMethod(new LinkMovementMethodExt(referenceLinkHandler));
            pagerIdText = itemView.findViewById(R.id.reference_id);
        }

        void bindItem(CharSequence idText, CharSequence contents) {
            pagerIdText.setText(idText);
            pagerReferenceText.setText(contents);
        }
    }

    private class ReferencesAdapter extends RecyclerView.Adapter {
        private List<References.Reference> references = new ArrayList<>();

        ReferencesAdapter(@NonNull List<References.Reference> adjacentReferences) {
            references.addAll(adjacentReferences);
        }

        @Override
        public int getItemCount() {
            return references.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.view_reference_pager_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((ViewHolder) holder).bindItem(processLinkTextWithAlphaReferences(references.get(position).getText()),
                    StringUtil.fromHtml(StringUtil.removeCiteMarkup(StringUtil.removeStyleTags(references.get(position).getContent()))));
        }
    }
}
