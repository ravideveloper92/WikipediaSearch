package com.wikipedia.views;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class DefaultRecyclerAdapter<T, V extends View>
        extends RecyclerView.Adapter<DefaultViewHolder<V>> {
    @NonNull private final List<T> items;

    public DefaultRecyclerAdapter(@NonNull List<T> items) {
        this.items = items;
    }

    @Override public int getItemCount() {
        return items.size();
    }

    protected T item(int position) {
        return items.get(position);
    }

    @NonNull protected List<T> items() {
        return items;
    }
}
