package org.garywzh.quumiibox.common;

import android.content.SharedPreferences;

import org.garywzh.quumiibox.AppContext;
import org.garywzh.quumiibox.eventbus.LoginEvent;
import org.garywzh.quumiibox.model.Category;
import org.garywzh.quumiibox.model.LoginResult;
import org.garywzh.quumiibox.model.UserInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class UserState {
    private static final UserState instance;
    private SharedPreferences userStatePrefs;
    private SharedPreferences categoryItemsPrefs;

    private UserInfo mUserInfo;
    private ArrayList<String> mCategoryItems;

    static {
        instance = new UserState();
    }

    public static UserState getInstance() {
        return instance;
    }

    public void init() {
        userStatePrefs = AppContext.getInstance().getSharedPreferences("UserStateFile", 0);
        mUserInfo = new UserInfo();
        Map<String, ?> map = userStatePrefs.getAll();
        mUserInfo.uid = (String) map.get("uid");
        mUserInfo.username = (String) map.get("username");
        mUserInfo.nickname = (String) map.get("nickname");
        mUserInfo.sex = (String) map.get("sex");
        mUserInfo.avatar = (String) map.get("avatar");
        mUserInfo.credit = (String) map.get("credit");
        mUserInfo.residecity = (String) map.get("residecity");
        mUserInfo.notenum = (String) map.get("notenum");
        mUserInfo.newpm = (String) map.get("newpm");
        mUserInfo.note = (String) map.get("note");

        mCategoryItems = new ArrayList<>();
        categoryItemsPrefs = AppContext.getInstance().getSharedPreferences("CategoryItemsFile", 0);
        int size = categoryItemsPrefs.getInt("array_size", 0);
        if (size != 0) {
            for (int i = 0; i < size; i++)
                mCategoryItems.add(categoryItemsPrefs.getString("array_" + i, null));
        } else {
            resetCateItemPrefs();
        }
    }

    public void resetCateItemPrefs() {
        mCategoryItems.clear();
        mCategoryItems.addAll(Arrays.asList(Category.cateItems));
        updateCategoryItemsPrefs();
    }

    public void login(LoginResult loginResult) {
        mUserInfo = loginResult.userinfo;
        userStatePrefs = AppContext.getInstance().getSharedPreferences("UserStateFile", 0);
        final SharedPreferences.Editor userPrefsWriter = userStatePrefs.edit();

        userPrefsWriter.putString("uid", mUserInfo.uid)
                .putString("username", mUserInfo.username)
                .putString("nickname", mUserInfo.nickname)
                .putString("sex", mUserInfo.sex)
                .putString("avatar", mUserInfo.avatar)
                .putString("credit", mUserInfo.credit)
                .putString("residecity", mUserInfo.residecity)
                .putString("notenum", mUserInfo.notenum)
                .putString("newpm", mUserInfo.newpm)
                .putString("note", mUserInfo.note)
                .apply();

        AppContext.getEventBus().post(new LoginEvent(mUserInfo.username));
    }

    public void logout() {
        mUserInfo = new UserInfo();

        userStatePrefs = AppContext.getInstance().getSharedPreferences("UserStateFile", 0);
        final SharedPreferences.Editor userPrefsWriter = userStatePrefs.edit();
        userPrefsWriter.clear().apply();
        AppContext.getEventBus().post(new LoginEvent());
    }

    public void updateCategoryItemsPrefs() {
        categoryItemsPrefs = AppContext.getInstance().getSharedPreferences("CategoryItemsFile", 0);
        final SharedPreferences.Editor prefsWriter = categoryItemsPrefs.edit();
        prefsWriter.clear();
        prefsWriter.putInt("array_size", mCategoryItems.size());
        for (int i = 0; i < mCategoryItems.size(); i++)
            prefsWriter.putString("array_" + i, mCategoryItems.get(i));
        prefsWriter.apply();
    }

    public ArrayList<String> getCategoryItems() {
        return mCategoryItems;
    }

    public void addCateoryItem(String item) {
        mCategoryItems.add(item);
        updateCategoryItemsPrefs();
    }

    public boolean isLoggedIn() {
        return mUserInfo.uid != null;
    }

    public String getId() {
        return mUserInfo.uid;
    }

    public String getUsername() {
        return mUserInfo.username;
    }

    public String getAvatar() {
        return mUserInfo.avatar;
    }

    public String getCredit() {
        return mUserInfo.credit;
    }
}
