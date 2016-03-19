package org.garywzh.quumiibox.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by garywzh on 2016/2/28.
 */
public class Item implements Parcelable {
    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_PIC = 1;
    public static final int TYPE_LONGPIC = 2;
    public static final int TYPE_GIF = 3;
    public static final int TYPE_LINK = 4;
    public static final int TYPE_DUANZI = 5;
    public static final int TYPE_TUJI = 6;

    public String blogid;
    public String uid;
    public String vid;
    public String link;
    public String img;
    public String subject;
    public String description;
    public String like;
    public String unlike;
    public String replynum;
    public String favnum;
    public String source;
    public String dateline;
    public String type;

    public static String buildUrlFromBlogid(String blogid) {
        return "http://www.huoji.tv/itemlist.php?id=" + blogid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(blogid);
        dest.writeString(vid);
        dest.writeString(link);
        dest.writeString(img);
        dest.writeString(subject);
        dest.writeString(description);
        dest.writeString(like);
        dest.writeString(unlike);
        dest.writeString(replynum);
        dest.writeString(favnum);
        dest.writeString(source);
        dest.writeString(dateline);
        dest.writeString(type);
    }

    protected Item(Parcel in) {
        this.blogid = in.readString();
        this.vid = in.readString();
        this.link = in.readString();
        this.img = in.readString();
        this.subject = in.readString();
        this.description = in.readString();
        this.like = in.readString();
        this.unlike = in.readString();
        this.replynum = in.readString();
        this.favnum = in.readString();
        this.source = in.readString();
        this.dateline = in.readString();
        this.type = in.readString();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}
