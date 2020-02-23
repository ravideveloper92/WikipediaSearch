package com.wikipedia.feed.mostread;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wikipedia.json.GsonMarshaller;
import com.wikipedia.json.GsonUnmarshaller;
import com.wikipedia.page.ExclusiveBottomSheetPresenter;
import com.wikipedia.page.PageActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wikipedia.R;
import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.view.ListCardItemView;
import com.wikipedia.history.HistoryEntry;
import com.wikipedia.json.GsonMarshaller;
import com.wikipedia.json.GsonUnmarshaller;
import com.wikipedia.page.ExclusiveBottomSheetPresenter;
import com.wikipedia.page.PageActivity;
import com.wikipedia.readinglist.AddToReadingListDialog;
import com.wikipedia.util.FeedbackUtil;
import com.wikipedia.util.ShareUtil;
import com.wikipedia.views.DefaultRecyclerAdapter;
import com.wikipedia.views.DefaultViewHolder;
import com.wikipedia.views.DrawableItemDecoration;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.wikipedia.Constants.InvokeSource.MOST_READ_ACTIVITY;
import static com.wikipedia.feed.mostread.MostReadArticlesActivity.MOST_READ_CARD;
import static com.wikipedia.util.L10nUtil.setConditionalLayoutDirection;

public class MostReadFragment extends Fragment {

    @BindView(R.id.view_most_read_fullscreen_link_card_list) RecyclerView mostReadLinks;
    private ExclusiveBottomSheetPresenter bottomSheetPresenter = new ExclusiveBottomSheetPresenter();
    private Unbinder unbinder;

    @NonNull
    public static MostReadFragment newInstance(@NonNull MostReadItemCard card) {
        MostReadFragment instance = new MostReadFragment();
        Bundle args = new Bundle();
        args.putString(MostReadArticlesActivity.MOST_READ_CARD, GsonMarshaller.marshal(card));
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_most_read, container, false);
        unbinder = ButterKnife.bind(this, view);
        MostReadListCard card = GsonUnmarshaller.unmarshal(MostReadListCard.class, requireActivity().getIntent().getStringExtra(MostReadArticlesActivity.MOST_READ_CARD));

        getAppCompatActivity().getSupportActionBar().setTitle(String.format(getString(R.string.top_on_this_day), card.subtitle()));
        setConditionalLayoutDirection(view, card.wikiSite().languageCode());

        initRecycler();
        mostReadLinks.setAdapter(new RecyclerAdapter(card.items(), new Callback()));
        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    private void initRecycler() {
        mostReadLinks.setLayoutManager(new LinearLayoutManager(getContext()));
        mostReadLinks.addItemDecoration(new DrawableItemDecoration(requireContext(), R.attr.list_separator_drawable));
        mostReadLinks.setNestedScrollingEnabled(false);
    }

    private static class RecyclerAdapter extends DefaultRecyclerAdapter<MostReadItemCard, ListCardItemView> {
        @Nullable
        private Callback callback;

        RecyclerAdapter(@NonNull List<MostReadItemCard> items, @NonNull Callback callback) {
            super(items);
            this.callback = callback;
        }

        @NonNull
        @Override
        public DefaultViewHolder<ListCardItemView> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DefaultViewHolder<>(new ListCardItemView(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(@NonNull DefaultViewHolder<ListCardItemView> holder, int position) {
            MostReadItemCard card = item(position);
            holder.getView().setCard(card)
                    .setHistoryEntry(new HistoryEntry(card.pageTitle(),
                            HistoryEntry.SOURCE_FEED_MOST_READ_ACTIVITY)).setCallback(callback);

        }
    }

    private AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }

    private class Callback implements ListCardItemView.Callback {
        @Override
        public void onSelectPage(@NonNull Card card, @NonNull HistoryEntry entry) {
            startActivity(PageActivity.newIntentForCurrentTab(requireContext(), entry, entry.getTitle()));
        }

        @Override
        public void onAddPageToList(@NonNull HistoryEntry entry) {
            bottomSheetPresenter.show(getChildFragmentManager(),
                    AddToReadingListDialog.newInstance(entry.getTitle(), MOST_READ_ACTIVITY));
        }

        @Override
        public void onRemovePageFromList(@NonNull HistoryEntry entry) {
            FeedbackUtil.showMessage(requireActivity(),
                    getString(R.string.reading_list_item_deleted, entry.getTitle().getDisplayText()));
        }

        @Override
        public void onSharePage(@NonNull HistoryEntry entry) {
            ShareUtil.shareText(getActivity(), entry.getTitle());
        }
    }
}
