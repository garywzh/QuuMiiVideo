package org.garywzh.quumiibox.common;

import android.content.SharedPreferences;

import org.garywzh.quumiibox.AppContext;
import org.garywzh.quumiibox.eventbus.LoginEvent;
import org.garywzh.quumiibox.model.Category;
import org.garywzh.quumiibox.model.LoginResult;
import org.garywzh.quumiibox.model.UserInfo;

import java.util.ArrayList;
import java.util.Arrays;

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
        mUserInfo.uid = userStatePrefs.getString("uid", null);
        mUserInfo.username = userStatePrefs.getString("username", null);
        mUserInfo.nickname = userStatePrefs.getString("nickname", null);
        mUserInfo.sex = userStatePrefs.getString("sex", null);
        mUserInfo.avatar = userStatePrefs.getString("avatar", null);
        mUserInfo.credit = userStatePrefs.getString("credit", null);
        mUserInfo.residecity = userStatePrefs.getString("residecity", null);
        mUserInfo.notenum = userStatePrefs.getString("notenum", null);
        mUserInfo.newpm = userStatePrefs.getString("newpm", null);
        mUserInfo.note = userStatePrefs.getString("note", null);

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

        userPrefsWriter.putString("uid", mUserInfo.uid);
        userPrefsWriter.putString("username", mUserInfo.username);
        userPrefsWriter.putString("nickname", mUserInfo.nickname);
        userPrefsWriter.putString("sex", mUserInfo.sex);
        userPrefsWriter.putString("avatar", mUserInfo.avatar);
        userPrefsWriter.putString("credit", mUserInfo.credit);
        userPrefsWriter.putString("residecity", mUserInfo.residecity);
        userPrefsWriter.putString("notenum", mUserInfo.notenum);
        userPrefsWriter.putString("newpm", mUserInfo.newpm);
        userPrefsWriter.putString("note", mUserInfo.note);
        userPrefsWriter.apply();

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
