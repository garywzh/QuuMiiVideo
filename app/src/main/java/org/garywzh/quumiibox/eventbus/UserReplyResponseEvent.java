package org.garywzh.quumiibox.eventbus;

/**
 * Created by garywzh on 2016/3/22.
 */
public class UserReplyResponseEvent {
    public final String response;

    public UserReplyResponseEvent(String response) {
        this.response = response;
    }
}
