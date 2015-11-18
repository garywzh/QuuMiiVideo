package org.garywzh.quumiibox.common;

import android.content.SharedPreferences;

import org.garywzh.quumiibox.AppContext;
import org.garywzh.quumiibox.eventbus.LoginEvent;
import org.garywzh.quumiibox.model.LoginResult;
import org.garywzh.quumiibox.model.Member;
import org.garywzh.quumiibox.network.RequestHelper;

public class UserState {
    private static final UserState instance;
    private SharedPreferences userStatePrefs;

    private int mId;
    private String mName;
    private String mAvatar;
    private int mCredit;

    static {
        instance = new UserState();
    }

    public static UserState getInstance() {
        return instance;
    }

    public void init() {
        userStatePrefs = AppContext.getInstance().getSharedPreferences("UserStateFile", 0);
        mId = userStatePrefs.getInt("id", 0);
        mName = userStatePrefs.getString("name", null);
        mAvatar = userStatePrefs.getString("avatar", null);
        mCredit = userStatePrefs.getInt("credit",0);
    }

    public void login(LoginResult loginResult) {
        mCredit = loginResult.mCredit;

        final Member member = loginResult.mMember;

        mId = member.getId();
        mName = member.getName();
        mAvatar = member.getAvatar();

        userStatePrefs = AppContext.getInstance().getSharedPreferences("UserStateFile", 0);
        final SharedPreferences.Editor userPrefsWriter = userStatePrefs.edit();
        userPrefsWriter.putInt("id",mId);
        userPrefsWriter.putString("name", mName);
        userPrefsWriter.putString("avatar", mAvatar);
        userPrefsWriter.putInt("credit", mCredit);
        userPrefsWriter.apply();

        AppContext.getEventBus().post(new LoginEvent(mName));
    }

    public void logout() {

        mId = 0;
        mName = null;
        mAvatar = null;
        mCredit = 0;

        userStatePrefs = AppContext.getInstance().getSharedPreferences("UserStateFile", 0);
        final SharedPreferences.Editor userPrefsWriter = userStatePrefs.edit();
        userPrefsWriter.clear().apply();
        RequestHelper.clearCookies();

        AppContext.getEventBus().post(new LoginEvent());
    }

    public boolean isLoggedIn() {
        return mName != null;
    }

    public int getId() {
        return mId;
    }

    public String getUsername() {
        return mName;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public int getCredit() {
        return mCredit;
    }
}
