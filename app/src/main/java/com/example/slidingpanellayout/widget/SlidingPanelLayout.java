package com.example.slidingpanellayout.widget;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.example.slidingpanellayout.R;

public class SlidingPanelLayout extends LinearLayout {
    private final String TAG = "SlidingPanelLayout";

    private ViewDragHelper mDragHelper;
    private View mPanelView, mContentView;
    private int mDragRange;
    private int mPannelViewResId = -1;
    private int mContentViewRestId= -1;



    public static final int STATE_OPENED_ENTIRELY = 0;
    public static final int STATE_OPENED = 1;

    private int mState;
    private SlidingPanelListener mListener;

    public SlidingPanelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SlidingPanelLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public SlidingPanelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mDragHelper = ViewDragHelper.create(this, 1f, callback);
        mState = STATE_OPENED;

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SlidingPanelLayout);
        if(ta != null) {
            mPannelViewResId = ta.getResourceId(R.styleable.SlidingPanelLayout_panelView, -1);
            mContentViewRestId = ta.getResourceId(R.styleable.SlidingPanelLayout_contentView, -1);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(mPannelViewResId != -1) {
            mPanelView = findViewById(mPannelViewResId);
        }
        if(mContentViewRestId != -1) {
            mContentView = findViewById(mContentViewRestId);
        }
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mPanelView;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mContentView.layout(0, top + mPanelView.getHeight(), getWidth(), top + mPanelView.getHeight() + mDragRange);
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            int topBound = getHeight() - mDragRange - mPanelView.getHeight();
            int bottomBound = getHeight() - mPanelView.getHeight();
            final int newHeight = Math.min(Math.max(topBound, top), bottomBound);
            return newHeight;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mDragRange;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (yvel > 0) {
                smoothToBottom();
            } else if (yvel < 0) {
                smoothToTop();
            } else {
                int halfHeight = getRootView().getHeight()/2;
                if (releasedChild.getTop() > halfHeight) {
                    smoothToTop();
                } else {
                    smoothToBottom();
                }
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if(mDragHelper.getViewDragState() == ViewDragHelper.STATE_DRAGGING) {
                if(mListener != null) {
                    mListener.onPanelClicked();
                }
            } else if (mDragHelper.getViewDragState() == ViewDragHelper.STATE_IDLE) {
                if(mListener != null) {
                    if(mState == STATE_OPENED) {
                        mListener.onPanelOpened();
                    } else if(mState == STATE_OPENED_ENTIRELY) {
                        mListener.onPanelOpenedEntirely();
                    }
                }
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mDragRange = mContentView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if(mState == STATE_OPENED_ENTIRELY) {

        } else if(mState == STATE_OPENED) {
            mPanelView.layout(0, getHeight() - mPanelView.getHeight(), getWidth(), getHeight());
            mContentView.layout(0, getHeight(), getWidth(), getHeight() + mDragRange);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDragHelper.shouldInterceptTouchEvent(event);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        boolean isHeaderViewUnder = mDragHelper.isViewUnder(mPanelView, (int) x, (int) y);

        mDragHelper.processTouchEvent(event);

        return isHeaderViewUnder || isViewHit(mContentView, (int) x, (int) y);
    }

    private boolean isViewHit(View view, int x, int y) {
        int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        int[] parentLocation = new int[2];
        this.getLocationOnScreen(parentLocation);
        int screenX = parentLocation[0] + x;
        int screenY = parentLocation[1] + y;
        return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.getWidth() &&
                screenY >= viewLocation[1] && screenY < viewLocation[1] + view.getHeight();
    }


    private boolean smoothToTop() {
        if (mDragHelper.smoothSlideViewTo(mPanelView, getPaddingLeft(), getHeight() - mDragRange - mPanelView.getHeight())) {
            ViewCompat.postInvalidateOnAnimation(this);
            mState = STATE_OPENED_ENTIRELY;
            return true;
        } else {
            return false;
        }
    }

    private boolean smoothToBottom() {
        if (mDragHelper.smoothSlideViewTo(mPanelView, getPaddingLeft(), getHeight() - mPanelView.getHeight())) {
            ViewCompat.postInvalidateOnAnimation(this);
            mState = STATE_OPENED;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    public interface SlidingPanelListener {
        public void onPanelOpened();
        public void onPanelOpenedEntirely();
        public void onPanelClicked();
    }

    public void setSlidingPanelListener(SlidingPanelListener l) {
        mListener = l;
    }
    public boolean openPanel() {
        return smoothToBottom();
    }
    public boolean openPanelEntirely() {
        return smoothToTop();
    }
    public int getPanelState() {
        return mState;
    }

}