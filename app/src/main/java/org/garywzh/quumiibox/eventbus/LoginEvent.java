package org.garywzh.quumiibox.eventbus;

public class LoginEvent {
    public final String mUsername;

    public LoginEvent() {
        mUsername = null;
    }

    public LoginEvent(String username) {
        mUsername = username;
    }

    public boolean isLogOut() {
        return mUsername == null;
    }
}
