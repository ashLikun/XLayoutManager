package com.azoft.carousellayoutmanager;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/8/1 0001  22:32
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：子view被选中的时候
 */

public abstract class OnItemClickListener {

    protected final RecyclerView mRecyclerView;

    protected final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
            final int position = holder.getAdapterPosition();

            if (position == getLayoutManager().getCenterItemPosition()) {
                onCenterItemClicked(mRecyclerView, getLayoutManager(), v);
            } else {
                onBackItemClicked(mRecyclerView, getLayoutManager(), v);
            }
        }
    };
    private FrameLayoutManager getLayoutManager(){
        return (FrameLayoutManager) mRecyclerView.getLayoutManager();
    }

    protected OnItemClickListener( final RecyclerView recyclerView) {
        mRecyclerView = recyclerView;

    }

    public static  void init(@NonNull final RecyclerView recyclerView){

    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/1 0001 22:36
     * <p>
     * 方法功能：中间item点击的时候
     */

    protected abstract void onCenterItemClicked(@NonNull final RecyclerView recyclerView, @NonNull final FrameLayoutManager carouselLayoutManager, @NonNull final View v);

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/1 0001 22:36
     * <p>
     * 方法功能：其他item点击的时候
     */

    protected void onBackItemClicked(@NonNull final RecyclerView recyclerView, @NonNull final FrameLayoutManager carouselLayoutManager, @NonNull final View v) {
        recyclerView.smoothScrollToPosition(carouselLayoutManager.getPosition(v));
    }
}