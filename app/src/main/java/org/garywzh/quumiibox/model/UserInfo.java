package org.garywzh.quumiibox.model;

/**
 * Created by garywzh on 2016/2/28.
 */
public class UserInfo {
    public String uid;
    public String username;
    public String nickname;
    public String sex;
    public String avatar;
    public String credit;
    public String residecity;
    public String notenum;
    public String newpm;
    public String note;

    public static String buildUrlFormUid(String uid) {
        return "http://www.huoji.tv/space-" + uid + ".html";
    }
}
