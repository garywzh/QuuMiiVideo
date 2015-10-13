package org.garywzh.quumiivideo.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.common.base.Preconditions;

import java.util.List;

import org.garywzh.quumiivideo.R;
import org.garywzh.quumiivideo.model.Comment;
import org.garywzh.quumiivideo.model.Member;

public class CommentAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final OnCommentActionListener mListener;
    private List<Comment> mCommentList;

    public CommentAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mListener = (OnCommentActionListener) context;
    }

    public void setDataSource(List<Comment> comments) {
        mCommentList = comments;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCommentList == null ? 0 : mCommentList.size();
    }

    @Override
    public Object getItem(int position) {
        return mCommentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mCommentList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.view_comment, parent , false);
            viewHolder = new ViewHolder(convertView, mListener);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = ((ViewHolder) convertView.getTag());
            Preconditions.checkNotNull(viewHolder);
        }

        final Comment comment = mCommentList.get(position);
        viewHolder.fillData(comment);

        return convertView;
    }

    private static class ViewHolder implements View.OnClickListener {

        private Comment mComment;
        private final ImageView mAvatar;
        private final TextView mUsername;
        private final TextView mContent;
        private final TextView mReplyTime;
        private OnCommentActionListener mListener;

        public ViewHolder(View view, OnCommentActionListener listener) {
            mAvatar = ((ImageView) view.findViewById(R.id.avatar_img));
            mUsername = (TextView) view.findViewById(R.id.username_tv);
            mContent = (TextView) view.findViewById(R.id.content_tv);
            mReplyTime = ((TextView) view.findViewById(R.id.time_tv));

            mListener = listener;
            mAvatar.setOnClickListener(this);
            mUsername.setOnClickListener(this);
        }

        public void fillData(Comment comment) {
            if (comment.equals(mComment)) {
                return;
            }
            mComment = comment;

            mUsername.setText(comment.getMember().getName());
            mContent.setText(comment.getContent());
            mReplyTime.setText(comment.getTime());

            Glide.with(mAvatar.getContext()).load(comment.getMember().getAvatar())
                    .placeholder(R.drawable.avatar_default).crossFade()
                    .into(mAvatar);
        }

        @Override
        public void onClick(View v) {
            if (v == mAvatar) {
                mListener.onMemberClick(mComment.getMember());
            }
        }
    }

    public interface OnCommentActionListener {
        void onMemberClick(Member member);
    }
}
