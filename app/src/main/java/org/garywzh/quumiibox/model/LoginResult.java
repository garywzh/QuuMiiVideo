package org.garywzh.quumiibox.model;

public class LoginResult {
    public final Member mMember;
    public final int mCredit;

    public LoginResult(Member member, int credit) {
        mMember = member;
        mCredit = credit;
    }
}
