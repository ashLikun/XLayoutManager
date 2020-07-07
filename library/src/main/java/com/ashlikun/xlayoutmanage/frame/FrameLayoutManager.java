package com.ashlikun.xlayoutmanage.frame;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An implementation of {@link RecyclerView.LayoutManager} that layout items like carousel.
 * Generally there is one center item and bellow this item there are maximum {@link FrameLayoutManager#getMaxVisibleItems()} items on each side of the center
 * item. By default {@link FrameLayoutManager#getMaxVisibleItems()} is {@link FrameLayoutManager#MAX_VISIBLE_ITEMS}.<br />
 * <br />
 * This LayoutManager supports only fixedSized adapter items.<br />
 * <br />
 * This LayoutManager supports {@link FrameLayoutManager#HORIZONTAL} and {@link FrameLayoutManager#VERTICAL} orientations. <br />
 * <br />
 * This LayoutManager supports circle layout. By default it if disabled. We don't recommend to use circle layout with adapter items count less then 3. <br />
 * <br />
 * Please be sure that layout_width of adapter item is a constant value and not {@link ViewGroup.LayoutParams#MATCH_PARENT}
 * for {@link #HORIZONTAL} orientation.
 * So like layout_height is not {@link ViewGroup.LayoutParams#MATCH_PARENT} for {@link FrameLayoutManager#VERTICAL}<br />
 * <br />
 */
public class FrameLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {
    private FrameSnapHelper frameSnapHelper;
    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;
    public static final int VERTICAL = OrientationHelper.VERTICAL;

    public static final int INVALID_POSITION = -1;
    public static final int MAX_VISIBLE_ITEMS = 2;

    private static final boolean CIRCLE_LAYOUT = false;

    private Integer mDecoratedChildWidth;
    private Integer mDecoratedChildHeight;

    private final int mOrientation;
    private final boolean mIsLooper;


    private final LayoutHelper mLayoutHelper = new LayoutHelper(MAX_VISIBLE_ITEMS);

    private OnLayoutListener mViewOnLayout;

    private final List<OnCenterItemSelectionListener> mOnCenterItemSelectionListeners = new ArrayList<>();
    private int mCenterItemPosition = INVALID_POSITION;
    private int mItemsCount;

    private CarouselSavedState mPendingCarouselSavedState;


    public FrameLayoutManager(final int orientation) {
        this(orientation, CIRCLE_LAYOUT);
    }

    public FrameLayoutManager(final int orientation, final boolean isLooper) {
        if (HORIZONTAL != orientation && VERTICAL != orientation) {
            throw new IllegalArgumentException("orientation should be HORIZONTAL or VERTICAL");
        }
        mViewOnLayout = new DefaultZoomOnLayoutListener();
        frameSnapHelper = new FrameSnapHelper();
        mOrientation = orientation;
        mIsLooper = isLooper;
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/1 0001 22:28
     * <p>
     * 方法功能：当子view layou前,用于自定义view的布局
     */
    public void setOnLayoutListener(@Nullable final OnLayoutListener postLayoutListener) {
        mViewOnLayout = postLayoutListener;
        requestLayout();
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/1 0001 22:39
     * <p>
     * 方法功能：中间item选择事件
     */
    public void addOnItemSelectionListener(@NonNull final OnCenterItemSelectionListener onCenterItemSelectionListener) {
        mOnCenterItemSelectionListeners.add(onCenterItemSelectionListener);
    }

    public void removeOnItemSelectionListener(@NonNull final OnCenterItemSelectionListener onCenterItemSelectionListener) {
        mOnCenterItemSelectionListeners.remove(onCenterItemSelectionListener);
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/1 0001 22:31
     * <p>
     * 方法功能：最大左右上下显示的item数目
     */
    public void setMaxVisibleItems(final int maxVisibleItems) {
        if (0 >= maxVisibleItems) {
            throw new IllegalArgumentException("不能小于等于0");
        }
        mLayoutHelper.mMaxVisibleItems = maxVisibleItems;
        requestLayout();
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/2 0002 21:03
     * <p>
     * 方法功能：最大左右上下显示的item数目
     */
    public int getMaxVisibleItems() {
        return mLayoutHelper.mMaxVisibleItems;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/2 0002 21:03
     * <p>
     * 方法功能：获取方向
     */
    public int getOrientation() {
        return mOrientation;
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/2 0002 21:07
     * <p>
     * 方法功能：是否可以水平滚动
     */
    @Override
    public boolean canScrollHorizontally() {
        return 0 != getChildCount() && HORIZONTAL == mOrientation;
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/2 0002 21:08
     * <p>
     * 方法功能：是否可以垂直滚动
     */

    @Override
    public boolean canScrollVertically() {
        return 0 != getChildCount() && VERTICAL == mOrientation;
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/2 0002 21:08
     * <p>
     * 方法功能：获取中间的item位置
     */

    public int getCenterItemPosition() {
        return mCenterItemPosition;
    }


    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/2 0002 21:08
     * <p>
     * 方法功能：滚动到指定的位置
     */
    @Override
    public void scrollToPosition(final int position) {
        if (0 > position) {
            throw new IllegalArgumentException("position can't be less then 0. position is : " + position);
        }
        mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(position);
        requestLayout();
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/2 0002 21:09
     * <p>
     * 方法功能：平滑滚动到指定位置
     */
    @Override
    public void smoothScrollToPosition(@NonNull final RecyclerView recyclerView, @NonNull final RecyclerView.State state, final int position) {
        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            public int calculateDyToMakeVisible(final View view, final int snapPreference) {
                //计算垂直滚动的大小
                if (!canScrollVertically()) {
                    return 0;
                }
                //距离当前position左边为正数，右边为负数
                return getOffsetForCurrentView2(view);
            }

            @Override
            public int calculateDxToMakeVisible(final View view, final int snapPreference) {
                //计算水平滚动的大小
                if (!canScrollHorizontally()) {
                    return 0;
                }
                //距离当前position左边为正数，右边为负数
                return getOffsetForCurrentView2(view);
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }


    /**
     * 监听竖直方向的相对偏移量
     *
     * @param dy
     * @param recycler
     * @param state
     * @return
     */
    @Override
    public int scrollVerticallyBy(final int dy, @NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (HORIZONTAL == mOrientation) {
            return 0;
        }
        return scrollBy(dy, recycler, state);
    }

    /**
     * 监听水平方向的相对偏移量
     *
     * @param dx
     * @param recycler
     * @param state
     * @return
     */
    @Override
    public int scrollHorizontallyBy(final int dx, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        if (VERTICAL == mOrientation) {
            return 0;
        }
        return scrollBy(dx, recycler, state);
    }

    /**
     * 滚动指定的距离
     *
     * @param diff
     * @param recycler
     * @param state
     * @return
     */
    @CallSuper
    protected int scrollBy(final int diff, @NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (null == mDecoratedChildWidth || null == mDecoratedChildHeight) {
            return 0;
        }
        if (0 == getChildCount() || 0 == diff) {
            return 0;
        }
        final int resultScroll;
        if (mIsLooper) {
            resultScroll = diff;

            mLayoutHelper.mScrollOffset += resultScroll;

            final int maxOffset = getScrollItemSize() * mItemsCount;
            while (0 > mLayoutHelper.mScrollOffset) {
                mLayoutHelper.mScrollOffset += maxOffset;
            }
            while (mLayoutHelper.mScrollOffset > maxOffset) {
                mLayoutHelper.mScrollOffset -= maxOffset;
            }

            mLayoutHelper.mScrollOffset -= resultScroll;
        } else {
            final int maxOffset = getMaxScrollOffset();

            if (0 > mLayoutHelper.mScrollOffset + diff) {
                //to make it 0
                resultScroll = -mLayoutHelper.mScrollOffset;
            } else if (mLayoutHelper.mScrollOffset + diff > maxOffset) {
                //to make it maxOffset
                resultScroll = maxOffset - mLayoutHelper.mScrollOffset;
            } else {
                resultScroll = diff;
            }
        }
        if (0 != resultScroll) {
            mLayoutHelper.mScrollOffset += resultScroll;
            fillData(recycler, state);
        }
        return resultScroll;
    }

    @Override
    public void onMeasure(final RecyclerView.Recycler recycler, final RecyclerView.State state, final int widthSpec, final int heightSpec) {
        mDecoratedChildHeight = null;
        mDecoratedChildWidth = null;

        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    @Override
    public void onAdapterChanged(final RecyclerView.Adapter oldAdapter, final RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);
        removeAllViews();
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        frameSnapHelper.attachToRecyclerView(view);
    }

    @Override
    public void onLayoutChildren(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (0 == state.getItemCount()) {
            removeAndRecycleAllViews(recycler);
            selectItemCenterPosition(INVALID_POSITION);
            return;
        }
        if (null == mDecoratedChildWidth) {
            final View view = recycler.getViewForPosition(0);
            addView(view);
            measureChildWithMargins(view, 0, 0);

            mDecoratedChildWidth = getDecoratedMeasuredWidth(view);
            mDecoratedChildHeight = getDecoratedMeasuredHeight(view);
            removeAndRecycleView(view, recycler);

        }
        fillData(recycler, state);
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/2 0002 21:19
     * <p>
     * 方法功能：计算中间距离指定目标的向量给滚动用的
     * ，距离当前position左边为负数，右边为正数
     */
    @Override
    public PointF computeScrollVectorForPosition(final int targetPosition) {
        if (0 == getChildCount()) {
            return null;
        }
        final float directionDistance = getScrollDirection(targetPosition);
        final int direction = (int) -Math.signum(directionDistance);
        if (HORIZONTAL == mOrientation) {
            return new PointF(direction, 0);
        } else {
            return new PointF(0, direction);
        }
    }

    private int calculateScrollForSelectingPosition(final int itemPosition) {
        final int fixedItemPosition = itemPosition < getItemCount() ? itemPosition : getItemCount() - 1;
        return fixedItemPosition * (VERTICAL == mOrientation ? mDecoratedChildHeight : mDecoratedChildWidth);
    }

    private void fillData(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        final float currentScrollPosition = getCurrentScrollPosition();
        generateLayoutOrder(currentScrollPosition, state);
        detachAndScrapAttachedViews(recycler);

        final int width = getWidthNoPadding();
        final int height = getHeightNoPadding();
        if (VERTICAL == mOrientation) {
            fillDataVertical(recycler, width, height);
        } else {
            fillDataHorizontal(recycler, width, height);
        }
        recycler.clear();
        detectOnItemSelectionChanged(currentScrollPosition, state);
    }

    private void detectOnItemSelectionChanged(final float currentScrollPosition, final RecyclerView.State state) {
        final float absCurrentScrollPosition = makeScrollPositionInRange0ToCount(currentScrollPosition, state.getItemCount());
        final int centerItem = Math.round(absCurrentScrollPosition);

        if (mCenterItemPosition != centerItem) {
            mCenterItemPosition = centerItem;
            postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    selectItemCenterPosition(centerItem);
                }
            });
        }
    }

    private void selectItemCenterPosition(final int centerItem) {
        for (final OnCenterItemSelectionListener onCenterItemSelectionListener : mOnCenterItemSelectionListeners) {
            onCenterItemSelectionListener.onCenterItemChanged(centerItem);
        }
    }

    private void fillDataVertical(final RecyclerView.Recycler recycler, final int width, final int height) {
        final int start = (width - mDecoratedChildWidth) / 2;
        final int end = start + mDecoratedChildWidth;

        final int centerViewTop = (height - mDecoratedChildHeight) / 2;

        for (int i = 0, count = mLayoutHelper.mLayoutOrder.length; i < count; ++i) {
            final LayoutOrder layoutOrder = mLayoutHelper.mLayoutOrder[i];
            final int offset = getCardOffsetByPositionDiff(layoutOrder.mItemPositionDiff);
            final int top = centerViewTop + offset;
            final int bottom = top + mDecoratedChildHeight;
            fillChildItem(start, top, end, bottom, layoutOrder, recycler, i);
        }
    }

    private void fillDataHorizontal(final RecyclerView.Recycler recycler, final int width, final int height) {
        final int top = (height - mDecoratedChildHeight) / 2;
        final int bottom = top + mDecoratedChildHeight;
        final int centerViewStart = (width - mDecoratedChildWidth) / 2;
        for (int i = 0, count = mLayoutHelper.mLayoutOrder.length; i < count; ++i) {
            final LayoutOrder layoutOrder = mLayoutHelper.mLayoutOrder[i];
            final int offset = getCardOffsetByPositionDiff(layoutOrder.mItemPositionDiff);
            final int left = centerViewStart + offset;
            final int right = left + mDecoratedChildWidth;
            fillChildItem(left, top, right, bottom, layoutOrder, recycler, i);
        }
    }


    private void fillChildItem(final int left, final int top, final int right, final int bottom, @NonNull final LayoutOrder layoutOrder, @NonNull final RecyclerView.Recycler recycler, final int i) {
        final View view = bindChild(layoutOrder.mItemAdapterPosition, recycler);
        ViewCompat.setElevation(view, i);
        ItemTransformation transformation = null;
        if (null != mViewOnLayout) {
            transformation = mViewOnLayout.transformChild(view, layoutOrder.mItemPositionDiff, mOrientation);
        }
        if (null == transformation) {
            view.layout(left, top, right, bottom);
        } else {
            view.layout(Math.round(left + transformation.mTranslationX), Math.round(top + transformation.mTranslationY),
                    Math.round(right + transformation.mTranslationX), Math.round(bottom + transformation.mTranslationY));
            view.setScaleX(transformation.mScaleX);
            view.setScaleY(transformation.mScaleY);
        }
    }

    /**
     * 当前滚动到的位置(小数,类似进度)
     */
    protected float getCurrentScrollPosition() {
        final int fullScrollSize = getMaxScrollOffset();
        if (0 == fullScrollSize) {
            return 0;
        }
        return 1.0f * mLayoutHelper.mScrollOffset / getScrollItemSize();
    }

    /**
     * @return 最大滚动值，以填充布局中的所有项目。通常，这只需要非周期布局。
     */
    private int getMaxScrollOffset() {
        return getScrollItemSize() * (mItemsCount - 1);
    }

    /**
     * 因为我们可以支持旧的Android版本，所以我们应该将我们的孩子按照特定的顺序进行布局，使我们的中心视图位于布局的顶部
     * (此项目应排在最后)。因此这个方法将计算布局顺序并填充{@link #mLayoutHelper}对象。
     * 此对象将由只需要布局的项目填充。不可见的项目将不会在那里。
     *
     * @param currentScrollPosition 当前滚动位置这是一个值，指示中心项目的位置
     *                              (如果此值为int，那么center项实际上位于布局的中心，否则它接近状态)。
     *                              请注意，这个值可以在任何范围内，它是循环布局
     * @param state                 回收视图的瞬态状态
     * @see #getCurrentScrollPosition()
     */
    private void generateLayoutOrder(final float currentScrollPosition, @NonNull final RecyclerView.State state) {
        mItemsCount = state.getItemCount();
        final float absCurrentScrollPosition = makeScrollPositionInRange0ToCount(currentScrollPosition, mItemsCount);
        final int centerItem = Math.round(absCurrentScrollPosition);

        if (mIsLooper && 1 < mItemsCount) {
            final int layoutCount = Math.min(mLayoutHelper.mMaxVisibleItems, mItemsCount);
            mLayoutHelper.initLayoutOrder(layoutCount);

            final int countLayoutHalf = layoutCount / 2;
            // before center item
            for (int i = 1; i <= countLayoutHalf; ++i) {
                final int position = Math.round(absCurrentScrollPosition - i + mItemsCount) % mItemsCount;
                mLayoutHelper.setLayoutOrder(countLayoutHalf - i, position, centerItem - absCurrentScrollPosition - i);
            }
            // after center item
            for (int i = layoutCount - 1; i >= countLayoutHalf + 1; --i) {
                final int position = Math.round(absCurrentScrollPosition - i + layoutCount) % mItemsCount;
                mLayoutHelper.setLayoutOrder(i - 1, position, centerItem - absCurrentScrollPosition + layoutCount - i);
            }
            mLayoutHelper.setLayoutOrder(layoutCount - 1, centerItem, centerItem - absCurrentScrollPosition);

        } else {
            final int firstVisible = Math.max(centerItem - mLayoutHelper.mMaxVisibleItems, 0);
            final int lastVisible = Math.min(centerItem + mLayoutHelper.mMaxVisibleItems, mItemsCount - 1);
            final int layoutCount = lastVisible - firstVisible + 1;

            mLayoutHelper.initLayoutOrder(layoutCount);

            for (int i = firstVisible; i <= lastVisible; ++i) {
                if (i == centerItem) {
                    mLayoutHelper.setLayoutOrder(layoutCount - 1, i, i - absCurrentScrollPosition);
                } else if (i < centerItem) {
                    mLayoutHelper.setLayoutOrder(i - firstVisible, i, i - absCurrentScrollPosition);
                } else {
                    mLayoutHelper.setLayoutOrder(layoutCount - (i - centerItem) - 1, i, i - absCurrentScrollPosition);
                }
            }
        }
    }

    public int getWidthNoPadding() {
        return getWidth() - getPaddingStart() - getPaddingEnd();
    }

    public int getHeightNoPadding() {
        return getHeight() - getPaddingEnd() - getPaddingStart();
    }

    private View bindChild(final int position, @NonNull final RecyclerView.Recycler recycler) {
        final View view = recycler.getViewForPosition(position);

        addView(view);
        measureChildWithMargins(view, 0, 0);

        return view;
    }

    protected int getCardOffsetByPositionDiff(final float itemPositionDiff) {
        final double smoothPosition = convertItemPositionDiffToSmoothPositionDiff(itemPositionDiff);

        final int dimenDiff;
        if (VERTICAL == mOrientation) {
            dimenDiff = (getHeightNoPadding() - mDecoratedChildHeight) / 4;
        } else {
            dimenDiff = (getWidthNoPadding() - mDecoratedChildWidth) / 4;
        }
        return (int) Math.round(Math.signum(itemPositionDiff) * dimenDiff * smoothPosition);
    }


    protected double convertItemPositionDiffToSmoothPositionDiff(final float itemPositionDiff) {
        final float absIemPositionDiff = Math.abs(itemPositionDiff);
        if (absIemPositionDiff > StrictMath.pow(1.0f / mLayoutHelper.mMaxVisibleItems, 1.0f / 3)) {
            return StrictMath.pow(absIemPositionDiff / mLayoutHelper.mMaxVisibleItems, 1 / 2.0f);
        } else {
            return StrictMath.pow(absIemPositionDiff, 2.0f);
        }
    }

    /**
     * @return full item size
     */
    protected int getScrollItemSize() {
        if (VERTICAL == mOrientation) {
            return mDecoratedChildHeight == null ? 0 : mDecoratedChildHeight;
        } else {
            return mDecoratedChildWidth == null ? 0 : mDecoratedChildWidth;
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        if (null != mPendingCarouselSavedState) {
            return new CarouselSavedState(mPendingCarouselSavedState);
        }
        final CarouselSavedState savedState = new CarouselSavedState(super.onSaveInstanceState());
        savedState.mCenterItemPosition = mCenterItemPosition;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof CarouselSavedState) {
            mPendingCarouselSavedState = (CarouselSavedState) state;

            super.onRestoreInstanceState(mPendingCarouselSavedState.mSuperState);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    /**
     * @return 距离中心的【偏移量
     */
    protected int getOffsetCenterView() {
        return Math.round(getCurrentScrollPosition()) * getScrollItemSize() - mLayoutHelper.mScrollOffset;
    }


    public boolean getCurrentScrollPositionQuyu() {
        if (getScrollItemSize() == 0) {
            return true;
        }
        return mLayoutHelper.mScrollOffset % getScrollItemSize() == 0;
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/2 0002 21:12
     * <p>
     * 方法功能：计算滑动到指定view需要的距离
     */
    protected int getOffsetForCurrentView(@NonNull final View view) {
        final int targetPosition = getPosition(view);
        int pendingScrollOffset = getScrollItemSize() * targetPosition;
        return pendingScrollOffset - mLayoutHelper.mScrollOffset;
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/2 0002 21:12
     * <p>
     * 方法功能：计算view距离中间的大小,给滚动用的
     */
    protected int getOffsetForCurrentView2(@NonNull final View view) {
        final int targetPosition = getPosition(view);
        //获取滚动的进度
        final float directionDistance = getScrollDirection(targetPosition);
        return Math.round(directionDistance * getScrollItemSize());
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/2 0002 21:21
     * <p>
     * 方法功能：计算中间距离指定目标的距离
     */
    private float getScrollDirection(final int targetPosition) {
        final float currentScrollPosition = makeScrollPositionInRange0ToCount(getCurrentScrollPosition(), mItemsCount);

        if (mIsLooper) {
            final float t1 = currentScrollPosition - targetPosition;
            final float t2 = Math.abs(t1) - mItemsCount;
            if (Math.abs(t1) > Math.abs(t2)) {
                return Math.signum(t1) * t2;
            } else {
                return t1;
            }
        } else {
            return currentScrollPosition - targetPosition;
        }
    }

    /**
     * 帮助方法，使滚动范围在[0，计数]。通常这种方法只适用于循环布局。
     *
     * @param currentScrollPosition 任何滚动位置范围。
     * @param count                 适配器的物品数
     * @return 在[0, count]范围内的良好滚动位置
     */
    private static float makeScrollPositionInRange0ToCount(float currentScrollPosition, final int count) {
        while (0 > currentScrollPosition) {
            currentScrollPosition += count;
        }
        while (Math.round(currentScrollPosition) >= count) {
            currentScrollPosition -= count;
        }
        return currentScrollPosition;
    }


    /**
     * 在LayoutManager布局完成后，将为每个可见视图项调用此接口方法
     */
    public interface OnCenterItemSelectionListener {

        /**
         * 侦听器，该侦听器将在中心项的每次更改时调用。
         * 如果项目被更改，这个监听器将在<b>上触发每个</b>布局操作。
         * 不要在这种方法中做任何昂贵的操作，因为这会影响滚动体验。
         */
        void onCenterItemChanged(final int adapterPosition);
    }

    /**
     * 包含当前可见项的助手类。
     * 通常这个类会填充这个列表
     * 该类保存所有滚动项和maxVisible项状态。
     *
     * @see #getMaxVisibleItems()
     */
    private static class LayoutHelper {

        private int mMaxVisibleItems;

        private int mScrollOffset;

        private LayoutOrder[] mLayoutOrder;

        private final List<WeakReference<LayoutOrder>> mReusedItems = new ArrayList<>();

        LayoutHelper(final int maxVisibleItems) {
            mMaxVisibleItems = maxVisibleItems;
        }

        /**
         * 在任何填充调用之前调用。需要回收旧的项目和初始化新的数组列表。通常这个列表是一个被重用的数组。
         */
        void initLayoutOrder(final int layoutCount) {
            if (null == mLayoutOrder || mLayoutOrder.length != layoutCount) {
                if (null != mLayoutOrder) {
                    recycleItems(mLayoutOrder);
                }
                mLayoutOrder = new LayoutOrder[layoutCount];
                fillLayoutOrder();
            }
        }

        /**
         * 在填充此列表的布局生成过程中调用。只能在{@link #initLayoutOrder(int)}方法调用之后调用。
         *
         * @param arrayPosition       布置位置
         * @param itemAdapterPosition 项目的适配器位置，用于将来的数据填充逻辑
         * @param itemPositionDiff    当前项目滚动位置和中心项目位置的差异。
         *                            如果这是一个中心项目，它在真正的布局中心，那么这将是0。
         *                            如果当前布局不在中心，那么这个值将永远不会是int。
         *                            如果这个项目中心是下面的布局中心线，那么这个值大于0，
         *                            否则小于0.
         */
        void setLayoutOrder(final int arrayPosition, final int itemAdapterPosition, final float itemPositionDiff) {
            final LayoutOrder item = mLayoutOrder[arrayPosition];
            item.mItemAdapterPosition = itemAdapterPosition;
            item.mItemPositionDiff = itemPositionDiff;
        }

        /**
         * 检查是这个屏幕布局在布局中有这个adapterPosition视图
         */
        boolean hasAdapterPosition(final int adapterPosition) {
            if (null != mLayoutOrder) {
                for (final LayoutOrder layoutOrder : mLayoutOrder) {
                    if (layoutOrder.mItemAdapterPosition == adapterPosition) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void recycleItems(@NonNull final LayoutOrder... layoutOrders) {
            for (final LayoutOrder layoutOrder : layoutOrders) {
                mReusedItems.add(new WeakReference<>(layoutOrder));
            }
        }

        private void fillLayoutOrder() {
            for (int i = 0, length = mLayoutOrder.length; i < length; ++i) {
                if (null == mLayoutOrder[i]) {
                    mLayoutOrder[i] = createLayoutOrder();
                }
            }
        }

        private LayoutOrder createLayoutOrder() {
            final Iterator<WeakReference<LayoutOrder>> iterator = mReusedItems.iterator();
            while (iterator.hasNext()) {
                final WeakReference<LayoutOrder> layoutOrderWeakReference = iterator.next();
                final LayoutOrder layoutOrder = layoutOrderWeakReference.get();
                iterator.remove();
                if (null != layoutOrder) {
                    return layoutOrder;
                }
            }
            return new LayoutOrder();
        }
    }

    /**
     * Class that holds item data.
     * This class is filled during {@link #generateLayoutOrder(float, RecyclerView.State)} and used during {@link #fillData(RecyclerView.Recycler, RecyclerView.State)}
     */
    private static class LayoutOrder {
        private int mItemAdapterPosition;
        private float mItemPositionDiff;
    }

    protected static class CarouselSavedState implements Parcelable {

        private final Parcelable mSuperState;
        private int mCenterItemPosition;

        protected CarouselSavedState(@Nullable final Parcelable superState) {
            mSuperState = superState;
        }

        private CarouselSavedState(@NonNull final Parcel in) {
            mSuperState = in.readParcelable(Parcelable.class.getClassLoader());
            mCenterItemPosition = in.readInt();
        }

        protected CarouselSavedState(@NonNull final CarouselSavedState other) {
            mSuperState = other.mSuperState;
            mCenterItemPosition = other.mCenterItemPosition;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel parcel, final int i) {
            parcel.writeParcelable(mSuperState, i);
            parcel.writeInt(mCenterItemPosition);
        }

        public static final Parcelable.Creator<CarouselSavedState> CREATOR
                = new Parcelable.Creator<CarouselSavedState>() {
            @Override
            public CarouselSavedState createFromParcel(final Parcel parcel) {
                return new CarouselSavedState(parcel);
            }

            @Override
            public CarouselSavedState[] newArray(final int i) {
                return new CarouselSavedState[i];
            }
        };
    }

    /**
     * 处理item点击事件
     *
     * @return 是否是中间点击了
     */
    public static boolean handleItemClick(RecyclerView mRecyclerView, View view, int position) {
        FrameLayoutManager layoutManager = (FrameLayoutManager) mRecyclerView.getLayoutManager();
        if (position == layoutManager.getCenterItemPosition()) {
            return true;
        } else {
            mRecyclerView.smoothScrollToPosition(layoutManager.getPosition(view));
            return false;
        }
    }
}