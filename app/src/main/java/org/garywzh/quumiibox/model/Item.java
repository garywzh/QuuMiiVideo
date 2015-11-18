package org.garywzh.quumiibox.model;

/**
 * Created by garywzh on 2015/9/22.
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.garywzh.quumiibox.network.RequestHelper;

import java.util.ArrayList;
import java.util.List;

public class Item implements Parcelable {

    private final int mId;
    private final Type mType;
    private final String mInfoBasedType;
    private final int mThumbUpCount;
    private final String mTitle;
    private final String mTime;
    private final List<Tag> mTags;
    private final int mReplyCount;

    Item(int id, Type type, String infoBasedType, int thumbUpCount, String title, String time,
         List<Tag> tags, int replyCount) {
        Preconditions.checkArgument(id != 0);

        mId = id;
        mType = type;
        mInfoBasedType = infoBasedType;
        mThumbUpCount = thumbUpCount;
        mTitle = title;
        mTime = time;
        mTags = tags;
        mReplyCount = replyCount;
    }

    public int getId() {
        return mId;
    }

    public Type getType() {
        return mType;
    }

    public String getinfoBasedType() {
        return mInfoBasedType;
    }

    public int getThumbUpCount() {
        return mThumbUpCount;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getTime() {
        return mTime;
    }

    public List<Tag> getTags() {
        return mTags;
    }

    public int getReplyCount() {
        return mReplyCount;
    }

    public String getUpUrl(){
        return RequestHelper.BASE_URL+"/itemlist/click.php?id="+mId+"&clickid=113";
    }

    public String getDowmUrl(){
        return RequestHelper.BASE_URL+"/itemlist/click.php?id="+mId+"&clickid=114";
    }

    public String getFavUrl(){
        return RequestHelper.BASE_URL+"/itemlist/op.php?op=fav&blogid="+mId;
    }

    public String getUnFavUrl(){
        return RequestHelper.BASE_URL+"/itemlist/op.php?op=unfav&blogid="+mId;
    }


    public static String buildUrlFromId(int id) {
        return RequestHelper.BASE_URL + "/itemlist-id-" + Integer.toString(id) + ".html";
    }

    public static String buildCoverPicUrlFromId(int id) {

        int firstNum = id / 1000;
        int secondNum = id / 100;

        return RequestHelper.BASE_URL + "/img/itemlist/" + Integer.toString(firstNum) + "/" + Integer.toString(secondNum) + "/" + Integer.toString(id) + ".jpg";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return Objects.equal(mId, item.mId)
                && Objects.equal(mThumbUpCount, item.mThumbUpCount)
                && Objects.equal(mReplyCount, item.mReplyCount);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mThumbUpCount, mReplyCount);
    }

    public static class Builder {
        private int mId;
        private Type mType;
        private String mInfoBasedType;
        private int mThumbUpCount;
        private String mTitle;
        private String mTime;
        private List<Tag> mTags;
        private int mReplyCount;

        public Builder setId(int id) {
            mId = id;
            return this;
        }

        public Builder setType(Type type) {
            mType = type;
            return this;
        }

        public Builder serInfoBasedType(String infoBasedType) {
            mInfoBasedType = infoBasedType;
            return this;
        }

        public Builder setThumbUpCount(int thumbUpCount) {
            mThumbUpCount = thumbUpCount;
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

        public Builder setTags(List<Tag> tags) {
            mTags = tags;
            return this;
        }

        public Builder setReplyCount(int replyCount) {
            mReplyCount = replyCount;
            return this;
        }

        public Item createItem() {
            return new Item(mId, mType, mInfoBasedType, mThumbUpCount, mTitle, mTime, mTags, mReplyCount);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeSerializable(this.mType);
        dest.writeString(this.mInfoBasedType);
        dest.writeInt(this.mThumbUpCount);
        dest.writeString(this.mTitle);
        dest.writeString(this.mTime);
        dest.writeList(this.mTags);
        dest.writeInt(this.mReplyCount);
    }

    protected Item(Parcel in) {
        this.mId = in.readInt();
        this.mType = (Type) in.readSerializable();
        this.mInfoBasedType = in.readString();
        this.mThumbUpCount = in.readInt();
        this.mTitle = in.readString();
        this.mTime = in.readString();

        List<Tag> tags = new ArrayList<>();
        in.readList(tags, Tag.class.getClassLoader());
        this.mTags = tags;

        this.mReplyCount = in.readInt();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public enum Type {
        VIDEO, IMAGE, TOPIC, NEWS
    }
}