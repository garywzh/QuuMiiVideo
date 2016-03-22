package org.garywzh.quumiibox.eventbus;

/**
 * Created by WZH on 2015/11/18.
 */
public class UserOperationResponseEvent {
    public final String type;
    public final String message;

    public UserOperationResponseEvent(String type, String message) {
        this.type = type;
        this.message = message;
    }
}
