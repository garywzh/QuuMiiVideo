package org.garywzh.quumiibox.eventbus;

/**
 * Created by WZH on 2015/11/18.
 */
public class UserOptionEvent {
    public final boolean isFavSucceed;

    public UserOptionEvent(boolean isFavSucceed){
        this.isFavSucceed = isFavSucceed;
    }
}
