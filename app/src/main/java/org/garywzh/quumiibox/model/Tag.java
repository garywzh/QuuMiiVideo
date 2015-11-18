package org.garywzh.quumiibox.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by WZH on 2015/11/1.
 */
public class Tag implements Parcelable {
    private final int mId;
    private final String mName;

    Tag(int id, String name) {
        mId = id;
        mName = name;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public static class Builder {
        private int mId;
        private String mName;

        public Builder setId(int id) {
            mId = id;
            return this;
        }

        public Builder setName(String name) {
            mName = name;
            return this;
        }

        public Tag createTag() {
            return new Tag(mId, mName);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeString(this.mName);
    }

    protected Tag(Parcel in) {
        this.mId = in.readInt();
        this.mName = in.readString();
    }

    public static final Creator<Tag> CREATOR = new Creator<Tag>() {
        public Tag createFromParcel(Parcel source) {
            return new Tag(source);
        }

        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
}
