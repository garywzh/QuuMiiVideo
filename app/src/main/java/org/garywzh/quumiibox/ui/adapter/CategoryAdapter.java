package org.garywzh.quumiibox.ui.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;

import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.common.UserState;

import java.util.ArrayList;

/**
 * Created by garywzh on 2016/3/9.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryVH> {
    private static final int TYPE_ITEM = 1;

    private final OnCateItemClickListener mOnItemClickListener;
    private final OnStartActionModeListener mOnStartActionModeListener;
    private ArrayList<String> items;
    private MultiSelector mMultiSelector;

    public CategoryAdapter(OnCateItemClickListener clickListener, OnStartActionModeListener startActionModeListener) {
        mOnItemClickListener = clickListener;
        mOnStartActionModeListener = startActionModeListener;
        items = UserState.getInstance().getCategoryItems();
        mMultiSelector = new MultiSelector();
        setHasStableIds(true);
    }

    @Override
    public CategoryVH onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_category_item, parent, false);
        return new CategoryVH(view, mOnItemClickListener, mMultiSelector, mOnStartActionModeListener);
    }

    @Override
    public void onBindViewHolder(CategoryVH holder, int position) {
        holder.fillData(items.get(position));
        holder.setSelectionModeBackgroundDrawable(getHighlightedBackground());
    }

    private StateListDrawable getHighlightedBackground() {
        ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor((Context) mOnItemClickListener, R.color.colorPrimary));
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{16843518}, colorDrawable);
        stateListDrawable.addState(StateSet.WILD_CARD, null);
        return stateListDrawable;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class CategoryVH extends SwappingHolder implements View.OnClickListener, View.OnLongClickListener {
        private final TextView mTitle;
        private String item;
        private OnCateItemClickListener mItemClickListener;
        private final MultiSelector mMultiSelector;
        private final OnStartActionModeListener mOnStartActionModeListener;

        public CategoryVH(View itemView, OnCateItemClickListener listener, MultiSelector multiSelector, OnStartActionModeListener startActionModeListener) {
            super(itemView, multiSelector);
            mItemClickListener = listener;
            mMultiSelector = multiSelector;
            mOnStartActionModeListener = startActionModeListener;
            mTitle = (TextView) itemView.findViewById(R.id.title);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void fillData(String item) {
            this.item = item;
            mTitle.setText(item);
        }

        @Override
        public void onClick(View v) {
            if (!mMultiSelector.tapSelection(CategoryVH.this)) {
                if (mItemClickListener == null) {
                    return;
                }
                mItemClickListener.onCateItemClick(item);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (!mMultiSelector.isSelectable()) {
                mOnStartActionModeListener.onStartActionMode(mMultiSelector);
                mMultiSelector.setSelectable(true);
                mMultiSelector.setSelected(CategoryVH.this, true);
                return true;
            }
            return false;
        }
    }

    public interface OnCateItemClickListener {
        void onCateItemClick(String item);
    }

    public interface OnStartActionModeListener {
        void onStartActionMode(MultiSelector multiSelector);
    }
}
