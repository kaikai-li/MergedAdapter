package com.lkk.free.mergedadapter;

import android.database.DataSetObserver;
import android.support.annotation.Keep;
import android.support.annotation.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by likaikai on 28/08/2017.
 */
@Keep
public class MergedListAdapter<T extends ListAdapter> extends BaseAdapter {

    private List<T> mAdapters;
    private final DataSetObserver mObserver;

    private static class LocalAdapterPosition<T extends ListAdapter> {
        private final T mAdapter;
        private final int mLocalPosition;

        private LocalAdapterPosition(T adapter, int offset) {
            mAdapter = adapter;
            mLocalPosition = offset;
        }

        public T getAdapter() {
            return mAdapter;
        }

        public int getLocalPosition() {
            return mLocalPosition;
        }
    }

    private MergedListAdapter() {
        mObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }
        };
    }

    private MergedListAdapter(Builder builder) {
        this();
        updateAdapters(Arrays.asList(builder.adapters));
    }

    public static MergedListAdapter newInstance() {
        return new MergedListAdapter();
    }

    @SuppressWarnings("unchecked")
    public MergedListAdapter setAdapters(@Size(min = 1) T... adapters) {
        updateAdapters(Arrays.asList(adapters));
        return this;
    }

    @SuppressWarnings("unchecked")
    private void updateAdapters(List adapters) {
        if (mAdapters != null) {
            for (T adapter : mAdapters) {
                adapter.unregisterDataSetObserver(mObserver);
            }
        }

        mAdapters = adapters;

        if (mAdapters != null) {
            for (T adapter : mAdapters) {
                adapter.registerDataSetObserver(mObserver);
            }
        }
    }

    @Override
    public int getCount() {
        int count = 0;
        if (mAdapters != null) {
            for (T adapter : mAdapters) {
                count += adapter.getCount();
            }
        }
        return count;
        // TODO: cache counts until next onChanged
    }

    /**
     * For a given merged position, find the corresponding Adapter and local position within that
     * Adapter by iterating through Adapters and summing their counts until the merged position is
     * found.
     *
     * @param position a merged (global) position
     * @return the matching Adapter and local position, or null if not found
     */
    private LocalAdapterPosition<T> getAdapterOffsetForItem(final int position) {
        if (mAdapters == null) {
            return null;
        }
        final int adapterCount = mAdapters.size();
        int i = 0;
        int count = 0;

        while (i < adapterCount) {
            T a = mAdapters.get(i);
            int newCount = count + a.getCount();
            if (position < newCount) {
                return new LocalAdapterPosition<>(a, position - count);
            }
            count = newCount;
            i++;
        }
        return null;
    }

    @Override
    public Object getItem(int position) {
        LocalAdapterPosition<T> result = getAdapterOffsetForItem(position);
        if (result == null) {
            return null;
        }
        return result.mAdapter.getItem(result.mLocalPosition);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        int count = 1;
        if (mAdapters != null) {
            for (T adapter : mAdapters) {
                count += adapter.getViewTypeCount();
            }
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        LocalAdapterPosition<T> result = getAdapterOffsetForItem(position);
        if (result == null) {
            return IGNORE_ITEM_VIEW_TYPE;
        }
        int otherViewTypeCount = 0;
        for (T adapter : mAdapters) {
            if (adapter == result.mAdapter) {
                break;
            }
            otherViewTypeCount += adapter.getViewTypeCount();
        }
        int type = result.mAdapter.getItemViewType(result.mLocalPosition);
        // Headers (negative types) are in a separate global namespace and their values should not
        // be affected by preceding adapter view types.
        if (type >= 0) {
            type += otherViewTypeCount;
        }
        return type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LocalAdapterPosition<T> result = getAdapterOffsetForItem(position);
        if (result == null) {
            return null;
        }
        return result.mAdapter.getView(result.mLocalPosition, convertView, parent);
    }

    @Override
    public boolean areAllItemsEnabled() {
        boolean enabled = true;
        if (mAdapters != null) {
            for (T adapter : mAdapters) {
                enabled &= adapter.areAllItemsEnabled();
            }
        }
        return enabled;
    }

    @Override
    public boolean isEnabled(int position) {
        LocalAdapterPosition<T> result = getAdapterOffsetForItem(position);
        return result != null && result.mAdapter.isEnabled(result.mLocalPosition);
    }

    @Keep
    public static class Builder<T extends ListAdapter> {
        private final T[] adapters;

        @SuppressWarnings("unchecked")
        public Builder(@Size(min = 1) T... adapters) {
            this.adapters = adapters;
        }

        public MergedListAdapter build() {
            return new MergedListAdapter(this);
        }
    }
}
