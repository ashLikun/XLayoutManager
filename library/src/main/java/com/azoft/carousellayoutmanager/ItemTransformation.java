package com.azoft.carousellayoutmanager;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/8/1 0001  22:24
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：item转换的实体类
 */

public class ItemTransformation {

    final float mScaleX;
    final float mScaleY;
    final float mTranslationX;
    final float mTranslationY;

    public ItemTransformation(final float scaleX, final float scaleY, final float translationX, final float translationY) {
        mScaleX = scaleX;
        mScaleY = scaleY;
        mTranslationX = translationX;
        mTranslationY = translationY;
    }
}