package com.ashlikun.xlayoutmanage.frame;

import android.support.v7.widget.RecyclerView;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/8/2 0002  21:30
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：保证每次滑动后停止后item选中，就像viewpager
 */

public class FrameScrollListener extends RecyclerView.OnScrollListener {
    /**
     * 是否已经设置过了
     */
    private boolean mAutoSet = true;

    @Override
    public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (!(layoutManager instanceof FrameLayoutManager)) {
            mAutoSet = true;
            return;
        }

        final FrameLayoutManager lm = (FrameLayoutManager) layoutManager;
        if (!mAutoSet) {
            //只有滑动停止后
            if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                final int scrollNeeded = lm.getOffsetCenterView();
                if (FrameLayoutManager.HORIZONTAL == lm.getOrientation()) {
                    recyclerView.smoothScrollBy(scrollNeeded, 0);
                } else {
                    recyclerView.smoothScrollBy(0, scrollNeeded);
                }
                mAutoSet = true;
            }
        }
        if (RecyclerView.SCROLL_STATE_DRAGGING == newState || RecyclerView.SCROLL_STATE_SETTLING == newState) {
            mAutoSet = false;
        }
    }
}