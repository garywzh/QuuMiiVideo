package org.garywzh.quumiibox.model;

/**
 * Created by garywzh on 2015/9/22.
 */

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.garywzh.quumiibox.network.RequestHelper;

public class Item {

    private final int mId;
    private final String mCoverPic;
    private final String mTimeLength;
    private final String mTitle;
    private final String mTime;
    private  int mThumbUpCount;
    private  int mThumbDownCount;

    Item(int id, String coverPic, String timeLength, String title, String time,
         int thumbUpCount, int thumbDownCount) {
        Preconditions.checkArgument(id != 0);

        mId = id;
        mCoverPic = coverPic;
        mTimeLength = timeLength;
        mTitle = title;
        mTime = time;
        mThumbUpCount = thumbUpCount;
        mThumbDownCount = thumbDownCount;
    }

    public int getId() {
        return mId;
    }

    public String getCoverPic() {
        return mCoverPic;
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

    public String getTimeLength() {
        return mTimeLength;
    }

    public String getTitle() {
        return mTitle;
    }

    public static String buildUrlFromId(int id) {
        return RequestHelper.BASE_URL + "/videolist-id-" + Integer.toString(id)+ ".html";
    }

    public Builder toBuilder() {
        return new Builder()
                .setId(mId)
                .setCoverPic(mCoverPic)
                .setTimeLength(mTimeLength)
                .setTitle(mTitle)
                .setTime(mTime)
                .setThumbUpCount(mThumbUpCount)
                .setThumbDownCount(mThumbDownCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return Objects.equal(mId, item.mId) &&
                Objects.equal(mThumbUpCount, item.mThumbUpCount) &&
                Objects.equal(mThumbDownCount, item.mThumbDownCount) &&
                Objects.equal(mTime, item.mTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mTimeLength, mTitle, mTime);
    }

    public static class Builder {
        private int mId;
        private String mCoverPic;
        private String mVideoLink;
        private String mTimeLength;
        private String mTitle;
        private String mTime;
        private int mThumbUpCount;
        private int mThumbDownCount;

        public Builder setId(int id) {
            mId = id;
            return this;
        }

        public Builder setCoverPic(String coverPic) {
            mCoverPic = coverPic;
            return this;
        }

        public Builder setVideoLink(String videoLink) {
            mVideoLink = videoLink;
            return this;
        }

        public Builder setTimeLength(String timeLength) {
            mTimeLength = timeLength;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setTime(String time) {
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

        public Item createItem() {
            return new Item(mId, mCoverPic, mTimeLength, mTitle, mTime, mThumbUpCount, mThumbDownCount);
        }
    }
}