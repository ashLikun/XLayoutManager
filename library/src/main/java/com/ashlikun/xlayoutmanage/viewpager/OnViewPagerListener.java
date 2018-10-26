package com.ashlikun.xlayoutmanage.viewpager;

/**
 * @author　　: 李坤
 * 创建时间: 2018/10/26 17:26
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：ViewPager样式
 */
public interface OnViewPagerListener {

    /**
     * 初始化完成
     */
    void onInitComplete();

    /**
     * 释放的监听
     */
    void onPageRelease(boolean isNext, int position);

    /**
     * 选中的监听以及判断是否滑动到底部
     */
    void onPageSelected(int position, boolean isBottom);

}
