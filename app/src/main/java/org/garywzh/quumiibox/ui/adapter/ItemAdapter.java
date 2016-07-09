package org.garywzh.quumiibox.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.model.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final View.OnClickListener mClickListener;
    private List<Item> mData;

    public ItemAdapter(final OnItemActionListener listener) {
        mClickListener = new OnViewHolderClickListener(listener);
        setHasStableIds(true);
    }

    public void setDataSource(List<Item> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == Item.TYPE_VIDEO) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_video, parent, false);
            view.setOnClickListener(mClickListener);
            return new VideoViewHolder(view);
        } else if (viewType == Item.TYPE_PIC | viewType == Item.TYPE_LONGPIC | viewType == Item.TYPE_GIF) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_image, parent, false);
            view.setOnClickListener(mClickListener);
            return new ImageViewHolder(view);
        } else if (viewType == Item.TYPE_LINK | viewType == Item.TYPE_TUJI | viewType == Item.TYPE_DUANZI) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_link, parent, false);
            view.setOnClickListener(mClickListener);
            return new TopicViewHolder(view);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof VideoViewHolder) {
            ((VideoViewHolder) holder).fillData(mData.get(position));
        } else if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).fillData(mData.get(position));
        } else if (holder instanceof TopicViewHolder) {
            ((TopicViewHolder) holder).fillData(mData.get(position));
        }
    }

    @Override
    public long getItemId(int position) {
        return mData == null ? RecyclerView.NO_ID : Integer.parseInt(mData.get(position).blogid);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        int type;

        switch (mData.get(position).type) {
            case "video":
                type = Item.TYPE_VIDEO;
                break;
            case "pic":
                type = Item.TYPE_PIC;
                break;
            case "longpic":
                type = Item.TYPE_LONGPIC;
                break;
            case "gif":
                type = Item.TYPE_GIF;
                break;
            case "link":
                type = Item.TYPE_LINK;
                break;
            case "duanzi":
                type = Item.TYPE_DUANZI;
                break;
            case "tuji":
                type = Item.TYPE_TUJI;
                break;
            default:
                throw new RuntimeException("unknown type");
        }

        return type;
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        public final View mRoot;
        public final ImageView mCoverPic;
        public final TextView mTitle;
        public final TextView mTime;
        public final TextView mReplyCount;
        public final TextView mThumbUpCount;

        public VideoViewHolder(View view) {
            super(view);

            mRoot = view;
            mCoverPic = ((ImageView) view.findViewById(R.id.cover_img));
            mTitle = ((TextView) view.findViewById(R.id.tv_title));
            mTime = ((TextView) view.findViewById(R.id.tv_time));
            mThumbUpCount = (TextView) view.findViewById(R.id.tv_thumbupcount);
            mReplyCount = (TextView) view.findViewById(R.id.tv_replycount);
        }

        public void fillData(Item item) {
            mRoot.setTag(item);
            mTitle.setText(item.subject);
            mTime.setText(item.dateline);
            mThumbUpCount.setText(item.like);
            mReplyCount.setText(item.replynum);

            setCoverPic(item);
        }

        private void setCoverPic(Item item) {
            final String url = item.img;
            Glide.with(mCoverPic.getContext()).load(url)
                    .placeholder(R.drawable.coverpic_default).crossFade()
                    .into(mCoverPic);
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        public final View mRoot;
        public final ImageView mCoverPic;
        public final TextView mTitle;
        public final TextView mTime;
        public final TextView mReplyCount;
        public final TextView mThumbUpCount;

        public ImageViewHolder(View view) {
            super(view);

            mRoot = view;
            mTitle = ((TextView) view.findViewById(R.id.tv_title));
            mCoverPic = ((ImageView) view.findViewById(R.id.cover_img));
            mTime = ((TextView) view.findViewById(R.id.tv_time));
            mThumbUpCount = (TextView) view.findViewById(R.id.tv_thumbupcount);
            mReplyCount = (TextView) view.findViewById(R.id.tv_replycount);
        }

        public void fillData(Item item) {
            mRoot.setTag(item);
            mTitle.setText(item.subject);
            mTime.setText(item.dateline);
            mThumbUpCount.setText(item.like);
            mReplyCount.setText(item.replynum);

            setCoverPic(item);
        }

        private void setCoverPic(Item item) {
            final String url = item.img;
            Glide.with(mCoverPic.getContext()).load(url).asBitmap()
                    .placeholder(R.drawable.coverpic_default)
                    .into(mCoverPic);
        }
    }

    public static class TopicViewHolder extends RecyclerView.ViewHolder {

        public final View mRoot;
        public final TextView mTitle;
        public final TextView mTime;
        public final TextView mReplyCount;
        public final TextView mThumbUpCount;

        public TopicViewHolder(View view) {
            super(view);

            mRoot = view;
            mTitle = ((TextView) view.findViewById(R.id.tv_title));
            mTime = ((TextView) view.findViewById(R.id.tv_time));
            mThumbUpCount = (TextView) view.findViewById(R.id.tv_thumbupcount);
            mReplyCount = (TextView) view.findViewById(R.id.tv_replycount);
        }

        public void fillData(Item item) {
            mRoot.setTag(item);
            mTitle.setText(item.subject);
            mTime.setText(item.dateline);
            mThumbUpCount.setText(item.like);
            mReplyCount.setText(item.replynum);
        }
    }

    private static class OnViewHolderClickListener implements View.OnClickListener {
        private OnItemActionListener mListener;

        public OnViewHolderClickListener(OnItemActionListener listener) {
            mListener = listener;
        }

        @Override
        public void onClick(View v) {
            if (mListener == null)
                return;
            mListener.onItemOpen((Item) (v.getTag()));
        }
    }

    public interface OnItemActionListener {
        boolean onItemOpen(Item item);
    }
}
