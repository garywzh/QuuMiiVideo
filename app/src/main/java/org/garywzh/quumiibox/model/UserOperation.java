package org.garywzh.quumiibox.model;

/**
 * Created by garywzh on 2016/3/15.
 */
public class UserOperation {
    public static final String TYPE_LIKE = "like";
    public static final String TYPE_UNLIKE = "unlike";
    public static final String TYPE_FAV = "fav";
    public static final String TYPE_UNFAV = "unfav";

    public String uid;
    public String blogid;
    public String type;

    public UserOperation() {
    }

    public UserOperation(String uid, String blogid, String type) {
        this.uid = uid;
        this.blogid = blogid;
        this.type = type;
    }


}
