package org.garywzh.quumiibox.model;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Comment {

    private final int mId;
    private final Member mMember;
    private final String mContent;
    private final String mTime;
    private  int mThumbUpCount;
    private  int mThumbDownCount;

    Comment(int id, Member member, String content, String time, int thumbUpCount, int thumbDownCount){
        Preconditions.checkArgument(id != 0);

        mId = id;
        mMember = member;
        mContent = content;
        mTime = time;
        mThumbUpCount = thumbUpCount;
        mThumbDownCount = thumbDownCount;
    }

    public String getContent() {
        return mContent;
    }

    public int getId() {
        return mId;
    }

    public Member getMember() {
        return mMember;
    }

    public int getThumbDownCount() {
        return mThumbDownCount;
    }

    public int getThumbUpCount() {
        return mThumbUpCount;
    }

    public String getTime() {
        return mTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        Comment comment = (Comment) o;
        return Objects.equal(mId, comment.mId) &&
                Objects.equal(mTime, comment.mTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mTime);
    }

    public static class Builder {
        private int mId;
        private Member mMember;
        private String mContent;
        private String mTime;
        private  int mThumbUpCount;
        private  int mThumbDownCount;

        public Builder setId(int id) {
            mId = id;
            return this;
        }

        public Builder setMember(Member member) {
            mMember = member;
            return this;
        }

        public Builder setContent(String content) {
            mContent = content;
            return this;
        }

        public Builder setTime(String time){
            mTime = time;
            return this;
        }

        public  Builder setThumbUpCount(int thumbUpCount){
            mThumbUpCount = thumbUpCount;
            return this;
        }

        public Builder setThumbDownCount(int thumbDownCount){
            mThumbDownCount = thumbDownCount;
            return this;
        }

        public Comment createComment() {
            return new Comment(mId, mMember, mContent, mTime, mThumbUpCount, mThumbDownCount);
        }
    }
}
