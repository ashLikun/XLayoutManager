package com.ashlikun.xlayoutmanage.skidright;

import android.content.Context;
import android.graphics.PointF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.ashlikun.xlayoutmanage.echelon.ItemViewInfo;

import java.util.ArrayList;

/**
 * @author　　: 李坤
 * 创建时间: 2018/10/26 17:16
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：右边滑出的LayoutManager
 */
public class SkidRightLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {
    private boolean mHasChild = false;
    private int mItemViewWidth;
    private int mItemViewHeight;
    private int mScrollOffset = Integer.MAX_VALUE;
    private float mItemWidthHeightRatio;
    private float mItemWidthRatio;
    private float mScale;
    private int mItemCount;
    private SkidRightSnapHelper mSkidRightSnapHelper;

    public SkidRightLayoutManager(Context context, float mItemWidthRatio, float scale) {
        this.mItemWidthRatio = mItemWidthRatio;
        this.mScale = scale;
        mSkidRightSnapHelper = new SkidRightSnapHelper();
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        mSkidRightSnapHelper.attachToRecyclerView(view);
    }

    public int getFixedScrollPosition(int direction, float fixValue) {
        if (mHasChild) {
            if (mScrollOffset % mItemViewWidth == 0) {
                return RecyclerView.NO_POSITION;
            }
            float position = mScrollOffset * 1.0f / mItemViewWidth;
            return convert2AdapterPosition((int) (direction > 0 ? position + fixValue : position + (1 - fixValue)) - 1);
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (0 == state.getItemCount()) {
            removeAndRecycleAllViews(recycler);
            return;
        }
        if (!mHasChild) {
            mItemViewHeight = getVerticalSpace();
            if (mItemWidthRatio > 0) {
                mItemViewWidth = (int) (getHorizontalSpace() * mItemWidthRatio);
            } else {
                mItemViewWidth = (int) (mItemViewHeight * mItemWidthHeightRatio);
            }

            mHasChild = true;
        }
        mItemCount = getItemCount();
        mScrollOffset = makeScrollOffsetWithinRange(mScrollOffset);
        fill(recycler);
    }

    public void fill(RecyclerView.Recycler recycler) {
        int bottomItemPosition = (int) Math.floor(mScrollOffset / mItemViewWidth);
        int bottomItemVisibleSize = mScrollOffset % mItemViewWidth;
        final float offsetPercent = bottomItemVisibleSize * 1.0f / mItemViewWidth;
        final int space = getHorizontalSpace();

        ArrayList<ItemViewInfo> layoutInfos = new ArrayList<>();
        for (int i = bottomItemPosition - 1, j = 1, remainSpace = space - mItemViewWidth;
             i >= 0; i--, j++) {
            double maxOffset = (getHorizontalSpace() - mItemViewWidth) / 2 * Math.pow(mScale, j);
            int start = (int) (remainSpace - offsetPercent * maxOffset);
            ItemViewInfo info = new ItemViewInfo(start,
                    (float) (Math.pow(mScale, j - 1) * (1 - offsetPercent * (1 - mScale))),
                    offsetPercent,
                    start * 1.0f / space
            );
            layoutInfos.add(0, info);

            remainSpace -= maxOffset;
            if (remainSpace <= 0) {
                info.setTop((int) (remainSpace + maxOffset));
                info.setPositionOffset(0);
                info.setLayoutPercent(info.getTop() / space);
                info.setScaleXY((float) Math.pow(mScale, j - 1));
                break;
            }
        }

        if (bottomItemPosition < mItemCount) {
            final int start = space - bottomItemVisibleSize;
            layoutInfos.add(new ItemViewInfo(start, 1.0f,
                    bottomItemVisibleSize * 1.0f / mItemViewWidth, start * 1.0f / space).
                    setIsBottom());
        } else {
            bottomItemPosition -= 1;
        }

        int layoutCount = layoutInfos.size();

        final int startPos = bottomItemPosition - (layoutCount - 1);
        final int endPos = bottomItemPosition;
        final int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View childView = getChildAt(i);
            int pos = convert2LayoutPosition(getPosition(childView));
            if (pos > endPos || pos < startPos) {
                removeAndRecycleView(childView, recycler);
            }
        }
        detachAndScrapAttachedViews(recycler);

        for (int i = 0; i < layoutCount; i++) {
            fillChild(recycler.getViewForPosition(convert2AdapterPosition(startPos + i)), layoutInfos.get(i));
        }
    }

    private void fillChild(View view, ItemViewInfo layoutInfo) {
        addView(view);
        measureChildWithExactlySize(view);
        final int scaleFix = (int) (mItemViewWidth * (1 - layoutInfo.getScaleXY()) / 2);

        int top = (int) getPaddingTop();
        layoutDecoratedWithMargins(view, layoutInfo.getTop() - scaleFix, top
                , layoutInfo.getTop() + mItemViewWidth - scaleFix, top + mItemViewHeight);
        view.setScaleX(layoutInfo.getScaleXY());
        view.setScaleY(layoutInfo.getScaleXY());
    }

    private void measureChildWithExactlySize(View child) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(
                mItemViewWidth - lp.leftMargin - lp.rightMargin, View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(
                mItemViewHeight - lp.topMargin - lp.bottomMargin, View.MeasureSpec.EXACTLY);
        child.measure(widthSpec, heightSpec);
    }

    private int makeScrollOffsetWithinRange(int scrollOffset) {
        return Math.min(Math.max(mItemViewWidth, scrollOffset), mItemCount * mItemViewWidth);
    }

    /**
     * 计算中间距离指定目标的向量给滚动用的
     * ，距离当前position左边为负数，右边为正数
     */
    @Override
    public PointF computeScrollVectorForPosition(final int targetPosition) {
        if (0 == getChildCount()) {
            return null;
        }
        final float directionDistance = calculateDistanceToPosition(targetPosition);
        final int direction = (int) Math.signum(directionDistance);
        return new PointF(direction, 0);
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int pendingScrollOffset = mScrollOffset + dx;
        mScrollOffset = makeScrollOffsetWithinRange(pendingScrollOffset);
        fill(recycler);
        return mScrollOffset - pendingScrollOffset + dx;
    }

    @Override
    public int computeHorizontalScrollOffset(RecyclerView.State state) {
        return mScrollOffset <= mItemViewWidth ? 0 : mScrollOffset;
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        return mScrollOffset <= mItemViewWidth ? 0 : mScrollOffset;
    }

    @Override
    public int computeHorizontalScrollRange(@NonNull RecyclerView.State state) {
        return getMaxScrollOffset();
    }

    private int getMaxScrollOffset() {
        return mItemCount * mItemViewWidth;
    }

    @Override
    public int computeVerticalScrollRange(@NonNull RecyclerView.State state) {
        return getMaxScrollOffset();
    }

    public int calculateDistanceToPosition(int targetPos) {
        int pendingScrollOffset = mItemViewWidth * (convert2LayoutPosition(targetPos) + 1);
        return pendingScrollOffset - mScrollOffset;
    }

    /**
     * 平滑滚动到指定位置
     */
    @Override
    public void smoothScrollToPosition(@NonNull final RecyclerView recyclerView, @NonNull final RecyclerView.State state, final int position) {
        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            public int calculateDxToMakeVisible(final View view, final int snapPreference) {
                //计算水平滚动的大小
                if (!canScrollHorizontally()) {
                    return 0;
                }
                //距离当前position左边为正数，右边为负数
                return -calculateDistanceToPosition(getPosition(view));
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    @Override
    public void scrollToPosition(int position) {
        if (position > 0 && position < mItemCount) {
            mScrollOffset = mItemViewWidth * (convert2LayoutPosition(position) + 1);
            requestLayout();
        }
    }


    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    public int convert2AdapterPosition(int layoutPosition) {
        return mItemCount - 1 - layoutPosition;
    }

    public int convert2LayoutPosition(int adapterPostion) {
        return mItemCount - 1 - adapterPostion;
    }

    public int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }


    public void setItemWidthRatio(float mItemWidthRatio) {
        this.mItemWidthRatio = mItemWidthRatio;
    }

    public float getItemWidthHeightRatio() {
        return mItemWidthHeightRatio;
    }

    public void setItemWidthHeightRatio(float mItemWidthHeightRatio) {
        this.mItemWidthHeightRatio = mItemWidthHeightRatio;
    }
}
