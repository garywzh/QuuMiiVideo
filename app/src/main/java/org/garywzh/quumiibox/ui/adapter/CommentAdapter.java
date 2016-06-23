package org.garywzh.quumiibox.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.common.UserState;
import org.garywzh.quumiibox.model.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_COMMENT = 1;
    private static final int TYPE_REPLY = 2;

    private final OnReplyActionListener mListener;
    private List<Comment> mData;
    private boolean isLoggedIn = false;

    public CommentAdapter(@NonNull OnReplyActionListener listener) {
        mListener = listener;
        setHasStableIds(true);
        isLoggedIn = UserState.getInstance().isLoggedIn();
    }

    public void setDataSource(List<Comment> comments) {
        mData = comments;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_COMMENT) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_comment, parent, false);
            return new CommentViewHolder(view);
        } else {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_reply, parent, false);
            return new ReplyViewHolder(mListener, view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentViewHolder) {
            ((CommentViewHolder) holder).fillData(mData.get(position));
        } else {
            ((ReplyViewHolder) holder).fillData();
        }
    }

    @Override
    public long getItemId(int position) {
        if (mData == null) {
            return RecyclerView.NO_ID;
        } else if (position <= mData.size() - 1) {
            return Integer.parseInt(mData.get(position).cid);
        } else {
//            一个不与commentid冲突的数字
            return 1000000;
        }
    }

    @Override
    public int getItemCount() {
        if (isLoggedIn) {
            return mData == null ? 1 : mData.size() + 1;
        } else {
            return mData == null ? 0 : mData.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mData != null) {
            if (position <= mData.size() - 1) {
                return TYPE_COMMENT;
            } else {
                return TYPE_REPLY;
            }
        } else {
            return TYPE_REPLY;
        }
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mAvatar;
        private final TextView mUsername;
        private final TextView mContent;
        private final TextView mReplyTime;

        public CommentViewHolder(View view) {
            super(view);
            mAvatar = ((ImageView) view.findViewById(R.id.avatar_img));
            mUsername = (TextView) view.findViewById(R.id.tv_username);
            mContent = (TextView) view.findViewById(R.id.content_tv);
            mReplyTime = ((TextView) view.findViewById(R.id.tv_time));
        }

        public void fillData(Comment comment) {
            mUsername.setText(comment.author);
            mContent.setText(comment.message);
            mReplyTime.setText(comment.dateline);

            Glide.with(mAvatar.getContext()).load(comment.avatar)
                    .placeholder(R.drawable.avatar_default).crossFade()
                    .into(mAvatar);
        }
    }

    public static class ReplyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final OnReplyActionListener mListener;
        private final ImageView mAvatar;
        private final EditText mEditText;
        private final String avatarUrl;

        public ReplyViewHolder(OnReplyActionListener listener, View itemView) {
            super(itemView);
            mListener = listener;
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar_img);
            mEditText = (EditText) itemView.findViewById(R.id.reply);
            avatarUrl = UserState.getInstance().getAvatar();
            mEditText.setOnClickListener(this);
        }

        public void fillData() {
            Glide.with(mAvatar.getContext()).load(avatarUrl)
                    .placeholder(R.drawable.avatar_default).crossFade()
                    .into(mAvatar);
        }

        @Override
        public void onClick(View v) {
            mListener.onReplyClick();
        }
    }

    public interface OnReplyActionListener {
        void onReplyClick();
    }
}
