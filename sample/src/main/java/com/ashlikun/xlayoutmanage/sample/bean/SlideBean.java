package com.ashlikun.xlayoutmanage.sample.bean;


/**
 * @author　　: 李坤
 * 创建时间: 2018/10/26 17:41
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：
 */
public class SlideBean {
    private int mItemBg;
    private String mTitle;
    private int mUserIcon;
    private String mUserSay;

    public SlideBean(int mItemBg, String mTitle, int mUserIcon, String mUserSay) {
        this.mItemBg = mItemBg;
        this.mTitle = mTitle;
        this.mUserIcon = mUserIcon;
        this.mUserSay = mUserSay;
    }

    public int getItemBg() {
        return mItemBg;
    }

    public void setItemBg(int mItemBg) {
        this.mItemBg = mItemBg;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public int getUserIcon() {
        return mUserIcon;
    }

    public void setUserIcon(int mUserIcon) {
        this.mUserIcon = mUserIcon;
    }

    public String getUserSay() {
        return mUserSay;
    }

    public void setUserSay(String mUserSay) {
        this.mUserSay = mUserSay;
    }
}
