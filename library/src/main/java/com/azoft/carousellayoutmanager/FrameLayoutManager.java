package com.azoft.carousellayoutmanager;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

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

    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;
    public static final int VERTICAL = OrientationHelper.VERTICAL;

    public static final int INVALID_POSITION = -1;
    public static final int MAX_VISIBLE_ITEMS = 2;

    private static final boolean CIRCLE_LAYOUT = false;

    private Integer mDecoratedChildWidth;
    private Integer mDecoratedChildHeight;

    private final int mOrientation;
    private final boolean mIsLooper;

    private int mPendingScrollPosition;

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
        mOrientation = orientation;
        mIsLooper = isLooper;
        mPendingScrollPosition = INVALID_POSITION;
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/1 0001 22:28
     * <p>
     * 方法功能：当子view layou前
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
        mPendingScrollPosition = position;
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
                return getOffsetForCurrentView(view);
            }

            @Override
            public int calculateDxToMakeVisible(final View view, final int snapPreference) {
                //计算水平滚动的大小
                if (!canScrollHorizontally()) {
                    return 0;
                }
                return getOffsetForCurrentView(view);
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/2 0002 21:19
     * <p>
     * 方法功能：计算中间距离指定目标的距离
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

    //垂直滚动指定的距离
    @Override
    public int scrollVerticallyBy(final int dy, @NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (HORIZONTAL == mOrientation) {
            return 0;
        }
        return scrollBy(dy, recycler, state);
    }

    //水平滚动指定的距离
    @Override
    public int scrollHorizontallyBy(final int dx, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        if (VERTICAL == mOrientation) {
            return 0;
        }
        return scrollBy(dx, recycler, state);
    }

    //滚动指定的距离
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
                resultScroll = -mLayoutHelper.mScrollOffset; //to make it 0
            } else if (mLayoutHelper.mScrollOffset + diff > maxOffset) {
                resultScroll = maxOffset - mLayoutHelper.mScrollOffset; //to make it maxOffset
            } else {
                resultScroll = diff;
            }
        }
        if (0 != resultScroll) {
            mLayoutHelper.mScrollOffset += resultScroll;
            fillData(recycler, state, false);
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

    @SuppressWarnings("RefusedBequest")
    @Override
    @CallSuper
    public void onLayoutChildren(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (0 == state.getItemCount()) {
            removeAndRecycleAllViews(recycler);
            selectItemCenterPosition(INVALID_POSITION);
            return;
        }
        boolean childMeasuringNeeded = false;
        if (null == mDecoratedChildWidth) {
            final View view = recycler.getViewForPosition(0);
            addView(view);
            measureChildWithMargins(view, 0, 0);

            mDecoratedChildWidth = getDecoratedMeasuredWidth(view);
            mDecoratedChildHeight = getDecoratedMeasuredHeight(view);
            removeAndRecycleView(view, recycler);

            if (INVALID_POSITION == mPendingScrollPosition && null == mPendingCarouselSavedState) {
                mPendingScrollPosition = mCenterItemPosition;
            }

            childMeasuringNeeded = true;
        }

        if (INVALID_POSITION != mPendingScrollPosition) {
            final int itemsCount = state.getItemCount();
            mPendingScrollPosition = 0 == itemsCount ? INVALID_POSITION : Math.max(0, Math.min(itemsCount - 1, mPendingScrollPosition));
        }
        if (INVALID_POSITION != mPendingScrollPosition) {
            mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(mPendingScrollPosition, state);
            mPendingScrollPosition = INVALID_POSITION;
            mPendingCarouselSavedState = null;
        } else if (null != mPendingCarouselSavedState) {
            mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(mPendingCarouselSavedState.mCenterItemPosition, state);
            mPendingCarouselSavedState = null;
        } else if (state.didStructureChange() && INVALID_POSITION != mCenterItemPosition) {
            mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(mCenterItemPosition, state);
        }

        fillData(recycler, state, childMeasuringNeeded);
    }

    private int calculateScrollForSelectingPosition(final int itemPosition, final RecyclerView.State state) {
        final int fixedItemPosition = itemPosition < state.getItemCount() ? itemPosition : state.getItemCount() - 1;
        return fixedItemPosition * (VERTICAL == mOrientation ? mDecoratedChildHeight : mDecoratedChildWidth);
    }

    private void fillData(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state, final boolean childMeasuringNeeded) {
        final float currentScrollPosition = getCurrentScrollPosition();
        generateLayoutOrder(currentScrollPosition, state);
        detachAndScrapAttachedViews(recycler);

        final int width = getWidthNoPadding();
        final int height = getHeightNoPadding();
        if (VERTICAL == mOrientation) {
            fillDataVertical(recycler, width, height, childMeasuringNeeded);
        } else {
            fillDataHorizontal(recycler, width, height, childMeasuringNeeded);
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

    private void fillDataVertical(final RecyclerView.Recycler recycler, final int width, final int height, final boolean childMeasuringNeeded) {
        final int start = (width - mDecoratedChildWidth) / 2;
        final int end = start + mDecoratedChildWidth;

        final int centerViewTop = (height - mDecoratedChildHeight) / 2;

        for (int i = 0, count = mLayoutHelper.mLayoutOrder.length; i < count; ++i) {
            final LayoutOrder layoutOrder = mLayoutHelper.mLayoutOrder[i];
            final int offset = getCardOffsetByPositionDiff(layoutOrder.mItemPositionDiff);
            final int top = centerViewTop + offset;
            final int bottom = top + mDecoratedChildHeight;
            fillChildItem(start, top, end, bottom, layoutOrder, recycler, i, childMeasuringNeeded);
        }
    }

    private void fillDataHorizontal(final RecyclerView.Recycler recycler, final int width, final int height, final boolean childMeasuringNeeded) {
        final int top = (height - mDecoratedChildHeight) / 2;
        final int bottom = top + mDecoratedChildHeight;

        final int centerViewStart = (width - mDecoratedChildWidth) / 2;
        Log.e("aaaa", "mLayoutHelper.mLayoutOrder.length =" + mLayoutHelper.mLayoutOrder.length);
        for (int i = 0, count = mLayoutHelper.mLayoutOrder.length; i < count; ++i) {
            final LayoutOrder layoutOrder = mLayoutHelper.mLayoutOrder[i];
            final int offset = getCardOffsetByPositionDiff(layoutOrder.mItemPositionDiff);
            final int left = centerViewStart + offset;
            final int right = left + mDecoratedChildWidth;
            fillChildItem(left, top, right, bottom, layoutOrder, recycler, i, childMeasuringNeeded);
        }
    }


    @SuppressWarnings("MethodWithTooManyParameters")
    private void fillChildItem(final int left, final int top, final int right, final int bottom, @NonNull final LayoutOrder layoutOrder, @NonNull final RecyclerView.Recycler recycler, final int i, final boolean childMeasuringNeeded) {
        final View view = bindChild(layoutOrder.mItemAdapterPosition, recycler, childMeasuringNeeded);
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

            ViewCompat.setScaleX(view, transformation.mScaleX);
            ViewCompat.setScaleY(view, transformation.mScaleY);
        }
    }

    /**
     * @return current scroll position of center item. this value can be in any range if it is cycle layout.
     * if this is not, that then it is in [0, {@link #mItemsCount - 1}]
     */
    private float getCurrentScrollPosition() {
        final int fullScrollSize = getMaxScrollOffset();
        if (0 == fullScrollSize) {
            return 0;
        }
        return 1.0f * mLayoutHelper.mScrollOffset / getScrollItemSize();
    }

    /**
     * @return maximum scroll value to fill up all items in layout. Generally this is only needed for non cycle layouts.
     */
    private int getMaxScrollOffset() {
        return getScrollItemSize() * (mItemsCount - 1);
    }

    /**
     * Because we can support old Android versions, we should layout our children in specific order to make our center view in the top of layout
     * (this item should layout last). So this method will calculate layout order and fill up {@link #mLayoutHelper} object.
     * This object will be filled by only needed to layout items. Non visible items will not be there.
     *
     * @param currentScrollPosition current scroll position this is a value that indicates position of center item
     *                              (if this value is int, then center item is really in the center of the layout, else it is near state).
     *                              Be aware that this value can be in any range is it is cycle layout
     * @param state                 Transient state of RecyclerView
     * @see #getCurrentScrollPosition()
     */
    private void generateLayoutOrder(final float currentScrollPosition, @NonNull final RecyclerView.State state) {
        mItemsCount = state.getItemCount();
        final float absCurrentScrollPosition = makeScrollPositionInRange0ToCount(currentScrollPosition, mItemsCount);
        final int centerItem = Math.round(absCurrentScrollPosition);

        if (mIsLooper && 1 < mItemsCount) {
            final int layoutCount = Math.min(mLayoutHelper.mMaxVisibleItems, mItemsCount);//

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

    private View bindChild(final int position, @NonNull final RecyclerView.Recycler recycler, final boolean childMeasuringNeeded) {
        final View view = recycler.getViewForPosition(position);

        addView(view);
        measureChildWithMargins(view, 0, 0);

        return view;
    }

    /**
     * Called during {@link #fillData(RecyclerView.Recycler, RecyclerView.State, boolean)} to calculate item offset from layout center line. <br />
     * <br />
     * Returns {@link #convertItemPositionDiffToSmoothPositionDiff(float)} * (size off area above center item when it is on the center). <br />
     * Sign is: plus if this item is bellow center line, minus if not<br />
     * <br />
     * ----- - area above it<br />
     * ||||| - center item<br />
     * ----- - area bellow it (it has the same size as are above center item)<br />
     *
     * @param itemPositionDiff current item difference with layout center line. if this is 0, then this item center is in layout center line.
     *                         if this is 1 then this item is bellow the layout center line in the full item size distance.
     * @return offset in scroll px coordinates.
     */
    protected int getCardOffsetByPositionDiff(final float itemPositionDiff) {
        final double smoothPosition = convertItemPositionDiffToSmoothPositionDiff(itemPositionDiff);

        final int dimenDiff;
        if (VERTICAL == mOrientation) {
            dimenDiff = (getHeightNoPadding() - mDecoratedChildHeight) / 4;
        } else {
            dimenDiff = (getWidthNoPadding() - mDecoratedChildWidth) / 4;
        }
        //noinspection NumericCastThatLosesPrecision
        return (int) Math.round(Math.signum(itemPositionDiff) * dimenDiff * smoothPosition);
    }

    /**
     * Called during {@link #getCardOffsetByPositionDiff(float)} for better item movement. <br/>
     * Current implementation speed up items that are far from layout center line and slow down items that are close to this line.
     * This code is full of maths. If you want to make items move in a different way, probably you should override this method.<br />
     * Please see code comments for better explanations.
     *
     * @param itemPositionDiff current item difference with layout center line. if this is 0, then this item center is in layout center line.
     *                         if this is 1 then this item is bellow the layout center line in the full item size distance.
     * @return smooth position offset. needed for scroll calculation and better user experience.
     * @see #getCardOffsetByPositionDiff(float)
     */
    @SuppressWarnings({"MagicNumber", "InstanceMethodNamingConvention"})
    protected double convertItemPositionDiffToSmoothPositionDiff(final float itemPositionDiff) {
        // generally item moves the same way above center and bellow it. So we don't care about diff sign.
        final float absIemPositionDiff = Math.abs(itemPositionDiff);

        // we detect if this item is close for center or not. We use (1 / maxVisibleItem) ^ (1/3) as close definer.
        if (absIemPositionDiff > StrictMath.pow(1.0f / mLayoutHelper.mMaxVisibleItems, 1.0f / 3)) {
            // this item is far from center line, so we should make it move like square root function
            return StrictMath.pow(absIemPositionDiff / mLayoutHelper.mMaxVisibleItems, 1 / 2.0f);
        } else {
            // this item is close from center line. we should slow it down and don't make it speed up very quick.
            // so square function in range of [0, (1/maxVisible)^(1/3)] is quite good in it;
            return StrictMath.pow(absIemPositionDiff, 2.0f);
        }
    }

    /**
     * @return full item size
     */
    protected int getScrollItemSize() {
        if (VERTICAL == mOrientation) {
            return mDecoratedChildHeight;
        } else {
            return mDecoratedChildWidth;
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
     * @return Scroll offset from nearest item from center
     */
    protected int getOffsetCenterView() {
        return Math.round(getCurrentScrollPosition()) * getScrollItemSize() - mLayoutHelper.mScrollOffset;
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/8/2 0002 21:12
     * <p>
     * 方法功能：计算view距离中间的大小
     */

    protected int getOffsetForCurrentView(@NonNull final View view) {
        final int targetPosition = getPosition(view);
        final float directionDistance = getScrollDirection(targetPosition);//获取滚动的个数
        return Math.round(directionDistance * getScrollItemSize());
    }

    /**
     * Helper method that make scroll in range of [0, count). Generally this method is needed only for cycle layout.
     *
     * @param currentScrollPosition any scroll position range.
     * @param count                 adapter items count
     * @return good scroll position in range of [0, count)
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
     * This interface methods will be called for each visible view item after general LayoutManager layout finishes. <br />
     * <br />
     * Generally this method should be used for scaling and translating view item for better (different) view presentation of layouting.
     */


    public interface OnCenterItemSelectionListener {

        /**
         * Listener that will be called on every change of center item.
         * This listener will be triggered on <b>every</b> layout operation if item was changed.
         * Do not do any expensive operations in this method since this will effect scroll experience.
         *
         * @param adapterPosition current layout center item
         */
        void onCenterItemChanged(final int adapterPosition);
    }

    /**
     * Helper class that holds currently visible items.
     * Generally this class fills this list. <br />
     * <br />
     * This class holds all scroll and maxVisible items state.
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
         * Called before any fill calls. Needed to recycle old items and init new array list. Generally this list is an array an it is reused.
         *
         * @param layoutCount items count that will be layout
         */
        void initLayoutOrder(final int layoutCount) {
            Log.e("aaaa", "layoutCount = " + layoutCount);
            if (null == mLayoutOrder || mLayoutOrder.length != layoutCount) {
                if (null != mLayoutOrder) {
                    recycleItems(mLayoutOrder);
                }
                mLayoutOrder = new LayoutOrder[layoutCount];
                fillLayoutOrder();
            }
        }

        /**
         * Called during layout generation process of filling this list. Should be called only after {@link #initLayoutOrder(int)} method call.
         *
         * @param arrayPosition       position in layout order
         * @param itemAdapterPosition adapter position of item for future data filling logic
         * @param itemPositionDiff    difference of current item scroll position and center item position.
         *                            if this is a center item and it is in real center of layout, then this will be 0.
         *                            if current layout is not in the center, then this value will never be int.
         *                            if this item center is bellow layout center line then this value is greater then 0,
         *                            else less then 0.
         */
        void setLayoutOrder(final int arrayPosition, final int itemAdapterPosition, final float itemPositionDiff) {
            final LayoutOrder item = mLayoutOrder[arrayPosition];
            item.mItemAdapterPosition = itemAdapterPosition;
            item.mItemPositionDiff = itemPositionDiff;
        }

        /**
         * Checks is this screen Layout has this adapterPosition view in layout
         *
         * @param adapterPosition adapter position of item for future data filling logic
         * @return true is adapterItem is in layout
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

        @SuppressWarnings("VariableArgumentMethod")
        private void recycleItems(@NonNull final LayoutOrder... layoutOrders) {
            for (final LayoutOrder layoutOrder : layoutOrders) {
                //noinspection ObjectAllocationInLoop
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
     * This class is filled during {@link #generateLayoutOrder(float, RecyclerView.State)} and used during {@link #fillData(RecyclerView.Recycler, RecyclerView.State, boolean)}
     */
    private static class LayoutOrder {

        /**
         * Item adapter position
         */
        private int mItemAdapterPosition;
        /**
         * Item center difference to layout center. If center of item is bellow layout center, then this value is greater then 0, else it is less.
         */
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