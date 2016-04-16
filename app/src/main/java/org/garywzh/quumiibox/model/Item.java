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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (!blogid.equals(item.blogid)) return false;
        if (!uid.equals(item.uid)) return false;
        if (!vid.equals(item.vid)) return false;
        if (link != null ? !link.equals(item.link) : item.link != null) return false;
        if (img != null ? !img.equals(item.img) : item.img != null) return false;
        if (subject != null ? !subject.equals(item.subject) : item.subject != null) return false;
        if (description != null ? !description.equals(item.description) : item.description != null)
            return false;
        if (source != null ? !source.equals(item.source) : item.source != null) return false;
        if (dateline != null ? !dateline.equals(item.dateline) : item.dateline != null)
            return false;
        return type.equals(item.type);

    }

    @Override
    public int hashCode() {
        int result = blogid.hashCode();
        result = 31 * result + uid.hashCode();
        result = 31 * result + vid.hashCode();
        result = 31 * result + (link != null ? link.hashCode() : 0);
        result = 31 * result + (img != null ? img.hashCode() : 0);
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (dateline != null ? dateline.hashCode() : 0);
        result = 31 * result + type.hashCode();
        return result;
    }

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
