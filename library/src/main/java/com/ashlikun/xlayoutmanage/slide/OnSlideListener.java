package com.ashlikun.xlayoutmanage.slide;

import android.support.v7.widget.RecyclerView;

/**
 * @author　　: 李坤
 * 创建时间: 2018/10/26 17:24
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：左右滑出
 */

public interface OnSlideListener<T> {

    void onSliding(RecyclerView.ViewHolder viewHolder, float ratio, int direction);

    void onSlided(RecyclerView.ViewHolder viewHolder, T t, int direction);

    void onClear();

}
