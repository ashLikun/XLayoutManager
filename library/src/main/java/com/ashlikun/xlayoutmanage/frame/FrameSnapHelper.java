/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific languag`e governing permissions and
 * limitations under the License.
 */

package com.ashlikun.xlayoutmanage.frame;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

/**
 * @author　　: 李坤
 * 创建时间: 2018/10/26 17:16
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：FrameLayoutManager的SnapHelper
 */

public class FrameSnapHelper extends SnapHelper {
    private int mDirection;

    @Override
    public int[] calculateDistanceToFinalSnap(
            @NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {

        if (layoutManager instanceof FrameLayoutManager) {
            int[] out = new int[2];
            if (layoutManager.canScrollHorizontally()) {
                out[0] = ((FrameLayoutManager) layoutManager).getOffsetForCurrentView(targetView);
                out[1] = 0;
            } else {
                out[0] = 0;
                out[1] = ((FrameLayoutManager) layoutManager).getOffsetForCurrentView(targetView);
            }
            return out;
        }
        return null;
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX,
                                      int velocityY) {
        if (layoutManager.canScrollHorizontally()) {
            mDirection = velocityX;
        } else {
            mDirection = velocityY;
        }
        return RecyclerView.NO_POSITION;
    }


    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof FrameLayoutManager) {
            if (((FrameLayoutManager) layoutManager).getCurrentScrollPositionQuyu()) {
                return null;
            }
            float position = ((FrameLayoutManager) layoutManager).getCurrentScrollPosition();
            position = (int) (mDirection > 0 ? position + 0.8f : position + 0.5);
            mDirection = 0;
            if (position != RecyclerView.NO_POSITION) {
                return layoutManager.findViewByPosition((int) position);
            }
        }
        return null;
    }
}
