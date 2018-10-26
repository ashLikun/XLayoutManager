package com.ashlikun.xlayoutmanage.frame;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/8/1 0001  22:25
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：默认提供一种item转换的样式
 */

public class DefaultZoomOnLayoutListener implements OnLayoutListener {
    @Override
    public ItemTransformation transformChild(@NonNull final View child, final float itemPositionToCenterDiff, final int orientation) {
        //final float scale = (float) (2 * (2 * -StrictMath.atan(Math.abs(itemPositionToCenterDiff) + 1.0) / Math.PI + 1));
        final float scale = (float) (1 - Math.min(1, Math.abs(itemPositionToCenterDiff) * 0.1));
        final float translateY;
        final float translateX;
        if (FrameLayoutManager.VERTICAL == orientation) {
            final float translateYGeneral = child.getMeasuredHeight() * (1 - scale);
            translateY = Math.signum(itemPositionToCenterDiff) * translateYGeneral;
            translateX = 0;
        } else {
            Log.e("transformChild", "itemPositionToCenterDiff = " + itemPositionToCenterDiff + "   getMeasuredWidth" + child.getMeasuredWidth() + "  scale=" + scale);
            final float translateXGeneral = child.getMeasuredWidth() * (1 - scale);
            translateX = Math.signum(itemPositionToCenterDiff) * translateXGeneral;
            translateY = 0;
        }

        return new ItemTransformation(scale, scale, translateX, translateY);
    }
}