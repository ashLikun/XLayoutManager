package com.azoft.carousellayoutmanager;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/8/1 0001 22:18
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：当item 要去layout的时候
 * 一般用于item的转换
 */
public interface OnLayoutListener {
    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/1 0001 22:21
     * <p>
     * 方法功能：
     *
     * @param child                    需要layout的view
     * @param itemPositionToCenterDiff 距离中间的偏移量 偏移多少个item ，中间是0，左边<0, 右边>0
     * @param orientation              垂直或者水平
     * @return Item转变的实体
     */
    ItemTransformation transformChild(@NonNull final View child, final float itemPositionToCenterDiff, final int orientation);
}
