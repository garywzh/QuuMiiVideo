package org.garywzh.quumiibox.model;

/**
 * Created by garywzh on 2016/3/22.
 */
public class UserReply {
    public String uid;
    public String blogid;
    public String replyto;
    public String cvalue;

    public UserReply(String uid, String blogid, String replyto, String cvalue) {
        this.uid = uid;
        this.blogid = blogid;
        this.replyto = replyto;
        this.cvalue = cvalue;
    }
}
