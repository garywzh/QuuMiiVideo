package org.garywzh.quumiivideo.model;

import com.google.common.base.Objects;

import org.garywzh.quumiivideo.network.RequestHelper;

/**
 * Created by WZH on 2015/10/10.
 */
public class Member {
    private final int mId;
    private final String mName;
    private final String mAvatar;

    Member(int id, String name, String avtar){
        mId = id;
        mName = name;
        mAvatar = avtar;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public static String buildUrlFromId(int id) {
        return RequestHelper.BASE_URL + "/space-" + Integer.toString(id)+ ".html";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return Objects.equal(mId, member.mId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId);
    }

    public static class Builder {
        private int mId;
        private String mName;
        private String mAvatar;

        public Builder setId(int id) {
            mId = id;
            return this;
        }

        public Builder setName(String name) {
            mName = name;
            return this;
        }

        public Builder setAvatar(String avatar) {
            mAvatar = avatar;
            return this;
        }

        public Member createMember() {
            return new Member(mId, mName, mAvatar);
        }
    }

}
