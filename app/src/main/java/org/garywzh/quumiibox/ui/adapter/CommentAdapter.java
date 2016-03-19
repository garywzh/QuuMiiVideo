package org.garywzh.quumiibox.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.model.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private final OnCommentActionListener mListener;
    private List<Comment> mData;

    public CommentAdapter(@NonNull OnCommentActionListener listener) {
        mListener = listener;
        setHasStableIds(true);
    }

    public void setDataSource(List<Comment> comments) {
        mData = comments;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_comment, parent, false);
        return new ViewHolder(mListener, view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Comment comment = mData.get(position);
        holder.fillData(comment);
    }

    @Override
    public long getItemId(int position) {
        return mData == null ? RecyclerView.NO_ID : Integer.parseInt(mData.get(position).cid);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView mAvatar;
        private final TextView mUsername;
        private final TextView mContent;
        private final TextView mReplyTime;

        private OnCommentActionListener mListener;
        private Comment mComment;

        public ViewHolder(View view) {
            this(null, view);
        }

        public ViewHolder(OnCommentActionListener listener, View view) {
            super(view);
            mListener = listener;

            mAvatar = ((ImageView) view.findViewById(R.id.avatar_img));
            mUsername = (TextView) view.findViewById(R.id.tv_username);
            mContent = (TextView) view.findViewById(R.id.content_tv);
            mReplyTime = ((TextView) view.findViewById(R.id.tv_time));

            mAvatar.setOnClickListener(this);
            mUsername.setOnClickListener(this);
        }

        public void fillData(Comment comment) {
            if (comment.equals(mComment)) {
                return;
            }
            mComment = comment;

            mUsername.setText(comment.author);
            mContent.setText(comment.message);
            mReplyTime.setText(comment.dateline);

            Glide.with(mAvatar.getContext()).load(comment.avatar)
                    .placeholder(R.drawable.avatar_default).crossFade()
                    .into(mAvatar);
        }

        @Override
        public void onClick(View v) {
            if (v == mAvatar) {
//                mListener.onMemberClick(mComment.getMember());
            }
        }
    }

    public interface OnCommentActionListener {
        void onMemberClick();
    }
}
