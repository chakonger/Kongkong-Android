package cn.leanvision.normalkongkong.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Scroller;

import cn.leanvision.common.util.DensityUtil;
import cn.leanvision.common.util.LogUtil;
import cn.leanvision.normalkongkong.R;

/********************************
 * Created by lvshicheng on 15/12/2.
 * description 暂时不考虑效率问题，复用View之类的算法
 ********************************/
public class LvVerticalViewGroup extends ViewGroup {

    private static final String TAG = LvVerticalViewGroup.class.getSimpleName();

    private static final int TOUCH_STATE_PRESS = 3;
    private static final int TOUCH_STATE_MOVE = 4;
    private static final int TOUCH_STATE_AUTO = 5;

    public float speed_accelerate = 3f;//dp为单位
    public float speed_recover = 5f;//dp为单位

    private BaseAdapter mAdapter;
    private int mSingleViewWidth;

    protected int mLeft;
    protected int mRight;
    protected int mTop;
    protected int mBottom;
    private int mHeight;

    private Scroller mScroller;
    private int mTouchSlop;
    private int mTouchState;

    private float mLastMotionX;
    private float mLastMotionY;

    private float mFloatA;
    /**
     * 当前居中的Item
     */
    private int currentItem;
    private int lastItem;

    /**
     * 功能：  根据触摸位置计算每像素的移动速率
     */
    private VelocityTracker mVelocityTracker;
    /**
     * 用于判断点击事件
     */
    private int mMoveOffset;
    private boolean mSelected;
    /**
     * 临时记录需要移动的距离
     */
    private int mMoveY;
    private OnItemChangedListener onItemChangedListener;

    public LvVerticalViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        currentItem = -1;

        mSingleViewWidth = DensityUtil.dip2px(getContext(), 50);
        mMoveOffset = DensityUtil.dip2px(context, 5);

        mScroller = new Scroller(context);

        DisplayMetrics me = getContext().getResources().getDisplayMetrics();
        speed_recover = me.density * speed_recover;
        speed_accelerate = me.density * speed_accelerate;
        // 初始化最小滑动距离
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        heightMeasureSpec = MeasureSpec.makeMeasureSpec((mSingleViewWidth * 4 / 3), MeasureSpec.getMode(heightMeasureSpec));
        int measureWidth = measureWidth(widthMeasureSpec);
        int measureHeight = measureHeight(heightMeasureSpec);
        mHeight = measureHeight;
//        Log.d(TAG, "Height : " + mHeight);
        //这里需要测量子View的尺寸，否则显示不出来
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth, measureHeight);
    }

    private int measureWidth(int pWidthMeasureSpec) {
        int result = 0;
        int widthMode = MeasureSpec.getMode(pWidthMeasureSpec);// 得到模式
        int widthSize = MeasureSpec.getSize(pWidthMeasureSpec);// 得到尺寸
        switch (widthMode) {
            /**
             * mode共有三种情况，取值分别为MeasureSpec.UNSPECIFIED, MeasureSpec.EXACTLY,
             * MeasureSpec.AT_MOST。
             *
             *
             * MeasureSpec.EXACTLY是精确尺寸，
             * 当我们将控件的layout_width或layout_height指定为具体数值时如andorid
             * :layout_width="50dip"，或者为FILL_PARENT是，都是控件大小已经确定的情况，都是精确尺寸。
             *
             *
             * MeasureSpec.AT_MOST是最大尺寸，
             * 当控件的layout_width或layout_height指定为WRAP_CONTENT时
             * ，控件大小一般随着控件的子空间或内容进行变化，此时控件尺寸只要不超过父控件允许的最大尺寸即可
             * 。因此，此时的mode是AT_MOST，size给出了父控件允许的最大尺寸。
             *
             *
             * MeasureSpec.UNSPECIFIED是未指定尺寸，这种情况不多，一般都是父控件是AdapterView，
             * 通过measure方法传入的模式。
             */
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = widthSize;
                break;
        }
        return result;
    }

    private int measureHeight(int pHeightMeasureSpec) {
        int result = 0;

        int heightMode = MeasureSpec.getMode(pHeightMeasureSpec);
        int heightSize = MeasureSpec.getSize(pHeightMeasureSpec);

        switch (heightMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = heightSize;
                break;
        }
        return result;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
//        Log.e(TAG, "dispatchDraw");
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        Log.e(TAG, String.format("changed : %s, l : %d, t : %d, r : %d, b : %d", changed, l, t, r, b));
        if (mLeft != l || mRight != r || mTop != t || mBottom != b) {
            mLeft = l;
            mRight = r;
            mTop = t;
            mBottom = b;
        }

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                MarginLayoutParams st =
                        (MarginLayoutParams) child.getLayoutParams();
//                Log.e(TAG, String.format("%d , %d , %d , %d", l + st.leftMargin, t + st.topMargin, r, t + st.topMargin + mSingleViewWidth));
//                Log.e(TAG, String.format("%d, %d", child.getWidth(), child.getHeight()));
                //这里不要使用parentView的t值， 这里只需要去在意child view在parent view中的位置，不关心parent view的位置情况。
//                child.layout(l + st.leftMargin, t + st.topMargin, r, t + st.topMargin + mSingleViewWidth);
                child.layout(st.leftMargin, st.topMargin, r, st.topMargin + mSingleViewWidth);
            }
        }
    }

    @Override
    public void computeScroll() {
//        Log.e(TAG, " -------- computeScroll ------------");
//        Log.e(TAG, String.format("mTouchState : %d ", mTouchState));
        if (mSelected) {
            return;
        }
        if (mScroller.computeScrollOffset()) {
//            Log.e(TAG, "computeScrollOffset true");
//            Log.e(TAG, "current scroll Y : " + mScroller.getCurrY());
            mMoveY = mScroller.getCurrY();
            if (mTouchState == TOUCH_STATE_AUTO) {
                int needScrollY = currentItem * mSingleViewWidth;
                int realScrollY = getScrollY();
//                Log.e(TAG, String.format("needScrollY : %d, realScrollY : %d", needScrollY, realScrollY));
                if (needScrollY == realScrollY) {
                    mScroller.abortAnimation();
                    mScroller.forceFinished(true);
                }
            }

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                postOnAnimation(new Runnable() {
                    @Override
                    public void run() {
                        showView();
                    }
                });
            } else {
                post(new Runnable() {
                    @Override
                    public void run() {
                        showView();
                    }
                });
            }

            ViewCompat.postInvalidateOnAnimation(this);
        } else {
//            Log.e(TAG, "computeScrollOffset false");
            if (mTouchState == TOUCH_STATE_AUTO)
                recoverMiddle();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        Log.e(TAG, "onInterceptTouchEvent ---- slop:" + mTouchSlop);
        int action = ev.getAction();
        //表示已经开始滑动了，不需要走该Action_MOVE方法了(第一次时可能调用)。
        //该方法主要用于用户快速松开手指，又快速按下的行为。此时认为是出于滑屏状态的。
//        if (action == MotionEvent.ACTION_MOVE && mTouchState == TOUCH_STATE_REST)
        if (mTouchState == TOUCH_STATE_MOVE)
            return true;

        float x = ev.getX();
        float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
//                Log.e(TAG, "onInterceptTouchEvent down");
                mLastMotionX = x;
                mLastMotionY = y;

                mTouchState = TOUCH_STATE_PRESS;
                mSelected = false;
//                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.e(TAG, "onInterceptTouchEvent move");
                final int xDiff = (int) Math.abs(mLastMotionX - x);
                final int yDiff = (int) Math.abs(mLastMotionY - y);

                if (xDiff > mMoveOffset || yDiff > mMoveOffset) {
                    mTouchState = TOUCH_STATE_MOVE;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
//                Log.e(TAG, "onInterceptTouchEvent up or cancel");
                if (mTouchState == TOUCH_STATE_PRESS) {

                } else if (mTouchState == TOUCH_STATE_MOVE) {

                }
                break;
        }
        // 暂时直接返回true
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.e(TAG, "onTouchEvent ---- event : " + event.getAction());
        int curAction = event.getActionMasked();
        if (!isEnabled() || mAdapter == null)
            return false;
        //获得VelocityTracker对象，并且添加滑动对象
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        //触摸点
        float y = event.getY();
        float x = event.getX();
        switch (curAction & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //如果屏幕的动画还没结束，你就按下了，我们就结束上一次动画，即开始这次新ACTION_DOWN的动画
                if (mScroller != null) {
                    if (!mScroller.isFinished()) {
                        mScroller.abortAnimation();
                    }
                }
                mLastMotionY = y; //记住开始落下的屏幕点
                mLastMotionX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                float xDiff = Math.abs(mLastMotionX - x);
                float yDiff = Math.abs(mLastMotionY - y);
//                Log.e(TAG, String.format("yDiff : %d ----- mMoveOffset : %d", yDiff, mMoveOffset));
                if (mTouchState == TOUCH_STATE_MOVE || xDiff > mMoveOffset || yDiff > mMoveOffset) {
                    mTouchState = TOUCH_STATE_MOVE;

                    yDiff = mLastMotionY - y;
                    scrollBy(yDiff);
                    mLastMotionY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
//                Log.e(TAG, "onTouchEvent up or cancel");
                if (mTouchState == TOUCH_STATE_PRESS) {
                    float locationY = y + getScrollY() - mHeight / 2;
                    int index = (int) ((locationY + mSingleViewWidth / 2) / mSingleViewWidth);
//                    Log.e(TAG, "currentItem : " + currentItem + "   index : " + index);
                    //TODO 点击事件,处理点击的滚动
//                    Log.e(TAG, String.format("单击事件 y : %f , center : %f , scroll :  %d, showCount : %d, index : %d", y, getCenterY(), currentItem * mSingleViewWidth, getShowCount(), index));
                    if (currentItem == index)
                        return true;
                    smoothScrollTo(index);
                } else {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000);
                    //计算速率
                    int yVelocity = (int) velocityTracker.getYVelocity();
//                    Log.e(TAG, String.format("yVelocity : %d", yVelocity));
                    mTouchState = TOUCH_STATE_AUTO;
                    mScroller.fling(0, getScrollY(), 0, -yVelocity, 0, 0, 0, (mAdapter.getCount() - 1) * mSingleViewWidth);
                    ViewCompat.postInvalidateOnAnimation(this);

                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        return true;
    }

    /*****************************************************
     *        以下是自己的逻辑
     * ***************************************************/
    /**
     * DataSetObserver used to capture adapter data change events
     */
    private DataSetObserver mAdapterDataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
//            Log.e(TAG, " mAdapterDataObserver  :  onChanged --- totalCount : " + mAdapter.getCount());
            //TODO
            int initIndex = mAdapter.getCount() - 1;
            initView(initIndex);
        }

        @Override
        public void onInvalidated() {
            Log.e(TAG, " mAdapterDataObserver  :  onInvalidated");
        }
    };

    public void setAdapter(BaseAdapter adapter, int initIndex) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mAdapterDataObserver);
        }
        if (adapter != null) {
            this.mAdapter = adapter;
            mAdapter.registerDataSetObserver(mAdapterDataObserver);
        }
        initView(initIndex);
    }

    /**
     * 重新布局子VIEW
     *
     * @param initIndex
     */
    private void initView(int initIndex) {
        LogUtil.log(getClass(), "initIndex : " + initIndex);
        removeAllViews();
        //这里初始化第一个位置  -- 第一次get有问题
        setCurrentItem(initIndex);
        if (currentItem < 0 || currentItem >= mAdapter.getCount())
            setCurrentItem(0);
        mFloatA = mHeight / (0.4f * getShowCount());
        mMoveY = 0;
        // 因为有时候已经有滚动距离了，如果直接初始化会出现移动有问题
        scrollTo(0, mMoveY);
        showView();
    }

    public void setViewHeight(int height) {
        this.mHeight = height;
    }

    private float getCenterY() {
        return mHeight / 2.0f;
    }

    /**
     * 移动过程中的view缩放问题
     */
    private void scrollBy(float distanceY) {
        int scrollY = getScrollY();
        if (-scrollY > mSingleViewWidth / 2) {
            return;
        } else if (scrollY > getTotalViewHeight() - mSingleViewWidth / 2) {
            return;
        }
        scrollBy(0, (int) distanceY);
        //检测移动的距离
//        Log.e(TAG, "showView scroll Y : " + mScroller.getCurrY() + " --- mMoveY : " + mMoveY + " ---- scrollY : " + scrollY);
//        float distance = getScrollY() % mSingleViewWidth;
        //计算中心位置,以及每个View 的偏离位置
        //FIXME
//        if (scrollY % mSingleViewWidth < 0.01) {
////            Log.e(TAG, "change a item");
//            setCurrentItem(scrollY / mSingleViewWidth);
////            Log.e(TAG, "currentItem : " + currentItem);
////            currentItem = (getScrollY() + mSingleViewWidth / 2) / 2;
//            distance = 0.0f;
//        }
//        Log.e(TAG, String.format("current Item : %d , mSingleViewWidth : %d, scrollY : %d", currentItem, mSingleViewWidth, scrollY));
        scaleView(false);
    }

    public void showView() {
        if (getChildCount() == 0) {
            scaleView(true);
//            Log.e(TAG, "showView currentItem : " + currentItem + "" + mScroller.getCurrY());
            scrollBy(0, currentItem * mSingleViewWidth);
        } else {
//            Log.e(TAG, String.format("scrollY : %d , currY : %d", scrollY, mScroller.getCurrY()));
//            Log.e(TAG, "showView mMoveY currentItem : " + currentItem);
            if (mMoveY > mAdapter.getCount() * mSingleViewWidth) {
                mMoveY = mAdapter.getCount() * mSingleViewWidth;
            }
            scrollTo(0, mMoveY);
            //TODO 这里如何根据移动距离去计算每个图标的缩放
            //移动的距离偏移中心点位置
            scaleView(false);
        }
    }

    public void scaleView(boolean isFirst) {
        //获取控件高度
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View view;
            float scale;
            MarginLayoutParams params;
            if (isFirst) {
                view = mAdapter.getView(i, null, this);
                params = new MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT, MarginLayoutParams.WRAP_CONTENT);
                //初始位置计算
                params.topMargin = (int) (getCenterY() + i * mSingleViewWidth - mSingleViewWidth / 2.0f);

                float point = Math.abs(Math.abs(params.topMargin + mSingleViewWidth / 2.0f - currentItem * mSingleViewWidth) - getCenterY());
                scale = mFloatA / (mFloatA + point);
            } else {
                view = getChildAt(i);

                params = (MarginLayoutParams) view.getLayoutParams();
                float point = Math.abs(Math.abs(params.topMargin + mSingleViewWidth / 2.0f) - getCenterY() - getScrollY());
                scale = mFloatA / (mFloatA + point);
            }
            
            //TODO 暂时先这么处理，应该封装一个view
            View iv = view.findViewById(R.id.iv);
            iv.setScaleX(scale);
            iv.setScaleY(scale);

            iv.setAlpha(scale > 0.9f ? 1.0f : 0.5f);
            if (view.getParent() == null)
                addView(view, params);
        }
    }

    /**
     * 计算当前显示的middle view
     */
    public void recoverMiddle() {
        if (mTouchState == TOUCH_STATE_MOVE)
            return;
        setCurrentItem((getScrollY() + mSingleViewWidth / 2) / mSingleViewWidth);

        //TODO 确认中心位置, 一次确认还需要滚动的距离
        // 向上滚动为正
        // 计算应该滚动的距离
        int needScrollY = currentItem * mSingleViewWidth;
        int realScrollY = getScrollY();

        int distance = needScrollY - realScrollY;
//        Log.e(TAG, String.format("currentItem : %d, distance : %d", currentItem, distance));
//        scrollBy(0, distance);
//        smoothScrollTo(currentItem);

        if (distance == 0) {
            mScroller.abortAnimation();
            mScroller.forceFinished(true);

            mMoveY = realScrollY;
            showView();
        } else {
            mScroller.startScroll(0, realScrollY, 0, distance, 500);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 滚动到指定的MIDDLE VIEW -- 主要配合点击事件
     */
    public void smoothScrollTo(int position) {
        if (position < 0 || position >= mAdapter.getCount()) // 越界不处理
            return;
        setCurrentItem(position);

        int needScrollY = position * mSingleViewWidth;
        int scrollY = getScrollY();

//        Log.e(TAG, String.format("index : %d, scrollY : %d", position, scrollY));
        mTouchState = TOUCH_STATE_AUTO;
        mScroller.startScroll(0, scrollY, 0, needScrollY - scrollY, 500);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private int getShowCount() {
        return (mSingleViewWidth / 2 + mHeight) / mSingleViewWidth;
    }

    private int getTotalViewHeight() {
        return mAdapter.getCount() * mSingleViewWidth;
    }

    public void setCurrentItem(int index) {
//        Log.e(TAG, "currentItem : " + index);
        this.lastItem = currentItem;
        this.currentItem = index;

        if (currentItem != lastItem && onItemChangedListener != null) {
            onItemChangedListener.onItemChanged(index);
        }
    }

    public int getCurrentItem() {
//        Log.e(TAG, "currentItem : " + currentItem);
        return currentItem;
    }

    public void setOnItemChangedListener(OnItemChangedListener onItemChangedListener) {
        this.onItemChangedListener = onItemChangedListener;
    }

    public interface OnItemChangedListener {
        void onItemChanged(int index);
    }
}
