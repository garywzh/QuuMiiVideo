package org.garywzh.quumiivideo.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.garywzh.quumiivideo.model.Item;

import org.garywzh.quumiivideo.R;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private final OnItemActionListener mListener;
    private List<Item> mData;

    public ItemAdapter(@NonNull OnItemActionListener listener) {
        mListener = listener;
        setHasStableIds(true);
    }

    public void setDataSource(List<Item> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_item, parent, false);
        return new ViewHolder(mListener, view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Item item = mData.get(position);
        holder.fillData(item);
    }

    @Override
    public long getItemId(int position) {
        return mData == null ? RecyclerView.NO_ID : mData.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final ImageView mCoverPic;
        public final TextView mTimeLength;
        public final TextView mTitle;
        public final TextView mTime;

        private final OnItemActionListener mListener;
        private Item mItem;

        public ViewHolder(View view) {
            this(null, view);
        }

        public ViewHolder(OnItemActionListener listener, View view) {
            super(view);
            mListener = listener;

            view.setOnClickListener(this);

            mCoverPic = ((ImageView) view.findViewById(R.id.cover_img));
            mTimeLength = ((TextView) view.findViewById(R.id.timelength_tv));
            mTitle = ((TextView) view.findViewById(R.id.title_tv));
            mTime = ((TextView) view.findViewById(R.id.time_tv));
        }

        public void fillData(Item topic) {
            if (topic.equals(mItem)) {
                return;
            }
            mItem = topic;

            mTimeLength.setText(topic.getTimeLength());
            mTitle.setText(topic.getTitle());
            mTime.setText(topic.getTime());

            setCoverPic(topic);
        }

        private void setCoverPic(Item item) {
            final String url = item.getCoverPic();
            Glide.with(mCoverPic.getContext()).load(url)
                    .placeholder(R.drawable.coverpic_default).crossFade()
                    .into(mCoverPic);
        }

        @Override
        public void onClick(View v) {
            if (mListener == null) {
                return;
            }

            mListener.onItemOpen(v, mItem);
        }
    }

    public interface OnItemActionListener {
        /**
         * @return should refresh data
         */
        boolean onItemOpen(View view, Item topic);
    }
}
