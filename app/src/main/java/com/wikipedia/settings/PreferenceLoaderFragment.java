package com.wikipedia.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.wikipedia.R;
import com.wikipedia.util.ResourceUtil;

abstract class PreferenceLoaderFragment extends PreferenceFragmentCompat
        implements PreferenceLoader {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        requireActivity().getWindow().getDecorView().post(() -> {
            if (!isAdded()) {
                return;
            }
            loadPreferences();
        });
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        RecyclerView v = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        v.setBackgroundColor(ResourceUtil.getThemedColor(requireContext(), R.attr.paper_color));
        return v;
    }
}
