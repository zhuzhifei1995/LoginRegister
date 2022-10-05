package com.test.chat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.M)
public class CustomListView extends ListView implements OnScrollListener {

    private static final String TAG = ActivityUtil.TAG;
    private final static int RELEASE_TO_REFRESH = 0;
    private final static int PULL_TO_REFRESH = 1;
    private final static int REFRESHING = 2;
    private final static int DONE = 3;
    private final static int LOADING = 4;
    private final static int RADIO = 3;
    private LinearLayout refreshHeadLinearLayout;
    private TextView head_tips_TextView;
    private TextView mLastUpdatedTextView;
    private ImageView mArrowImageView;
    private ProgressBar mProgressBar;
    private RotateAnimation mAnimation;
    private RotateAnimation mReverseAnimation;
    private boolean mIsRecored;
    private int mHeadContentHeight;
    private int mStartY;
    private int mFirstItemIndex;
    private int mState;
    private boolean mIsBack;
    private boolean mISRefreshable;
    private OnRefreshListener mRefreshListener;

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @SuppressLint("InflateParams")
    private void init(Context context) {
        Log.e(TAG, "init: ");
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        refreshHeadLinearLayout = (LinearLayout) layoutInflater.inflate(R.layout.refresh_head_list_view, null);
        mArrowImageView = (ImageView) refreshHeadLinearLayout.findViewById(R.id.head_arrowImageView);
        mProgressBar = (ProgressBar) refreshHeadLinearLayout.findViewById(R.id.head_progressBar);
        head_tips_TextView = (TextView) refreshHeadLinearLayout.findViewById(R.id.head_tips_TextView);
        mLastUpdatedTextView = (TextView) refreshHeadLinearLayout.findViewById(R.id.head_lastUpdatedTextView);
        measureView(refreshHeadLinearLayout);
        mHeadContentHeight = refreshHeadLinearLayout.getMeasuredHeight();
        System.out.println("mHeadContentHeight = " + mHeadContentHeight);
        int mHeadContentWidth = refreshHeadLinearLayout.getMeasuredWidth();
        System.out.println("mHeadContentWidth = " + mHeadContentWidth);
        refreshHeadLinearLayout.setPadding(0, -1 * mHeadContentHeight, 0, 0);
        refreshHeadLinearLayout.invalidate();
        addHeaderView(refreshHeadLinearLayout, null, false);
        setOnScrollListener(this);
        mAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF,
                0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setDuration(250);
        mAnimation.setFillAfter(true);
        mReverseAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF,
                0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseAnimation.setInterpolator(new LinearInterpolator());
        mReverseAnimation.setDuration(250);
        mReverseAnimation.setFillAfter(true);
        mState = DONE;
        mISRefreshable = false;
    }

    private void measureView(View child) {
        android.view.ViewGroup.LayoutParams params = child.getLayoutParams();
        System.out.println("params = " + params);
        if (params == null) {
            params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        System.out.println("lpWidth = " + params.width);
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, params.width);
        System.out.println("childWidthSpec = " + childWidthSpec);
        int lpHeight = params.height;
        System.out.println("lpHeight = " + lpHeight);
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.UNSPECIFIED);
        }
        System.out.println("childHeightSpec = " + childHeightSpec);
        child.measure(childWidthSpec, childHeightSpec);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        mFirstItemIndex = firstVisibleItem;
    }

    private void onRefresh() {
        if (mRefreshListener != null) {
            mRefreshListener.onRefresh();
        }
    }

    public void onRefreshComplete() {
        mState = DONE;
        String head_tips = "上次刷新的时间为：" + getTimeString();
        mLastUpdatedTextView.setText(head_tips);
        changeHeaderViewByState();
    }

    public void setonRefreshListener(OnRefreshListener onRefreshListener) {
        this.mRefreshListener = onRefreshListener;
        mISRefreshable = true;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent ev) {
        if (mISRefreshable) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mFirstItemIndex == 0 && !mIsRecored) {
                        mIsRecored = true;
                        mStartY = (int) ev.getY();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (mState != REFRESHING && mState != LOADING) {
                        if (mState == PULL_TO_REFRESH) {
                            mState = DONE;
                            changeHeaderViewByState();
                        }
                        if (mState == RELEASE_TO_REFRESH) {
                            mState = REFRESHING;
                            changeHeaderViewByState();
                            onRefresh();
                        }
                    }
                    mIsBack = false;
                    mIsRecored = false;
                    break;

                case MotionEvent.ACTION_MOVE:
                    int tempY = (int) ev.getY();
                    if (!mIsRecored && mFirstItemIndex == 0) {
                        mIsRecored = true;
                        mStartY = tempY;
                    }
                    if (mState != REFRESHING && mIsRecored && mState != LOADING) {
                        if (mState == RELEASE_TO_REFRESH) {
                            setSelection(0);
                            if ((tempY - mStartY) / RADIO < mHeadContentHeight && (tempY - mStartY) > 0) {
                                mState = PULL_TO_REFRESH;
                                changeHeaderViewByState();
                            } else if (tempY - mStartY <= 0) {
                                mState = DONE;
                                changeHeaderViewByState();
                            }
                        }

                        if (mState == PULL_TO_REFRESH) {
                            setSelection(0);
                            if ((tempY - mStartY) / RADIO >= mHeadContentHeight) {
                                mState = RELEASE_TO_REFRESH;
                                mIsBack = true;
                                changeHeaderViewByState();
                            }
                        } else if (tempY - mStartY <= 0) {
                            mState = DONE;
                            changeHeaderViewByState();
                        }

                        if (mState == DONE) {
                            if (tempY - mStartY > 0) {
                                mState = PULL_TO_REFRESH;
                                changeHeaderViewByState();
                            }
                        }

                        if (mState == PULL_TO_REFRESH) {
                            refreshHeadLinearLayout.setPadding(0, -1 * mHeadContentHeight + (tempY - mStartY) / RADIO, 0, 0);
                        }

                        if (mState == RELEASE_TO_REFRESH) {
                            refreshHeadLinearLayout.setPadding(0, (tempY - mStartY) / RADIO - mHeadContentHeight, 0, 0);
                        }
                    }
                    break;

                default:
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    private void changeHeaderViewByState() {
        String head_tips;
        switch (mState) {
            case PULL_TO_REFRESH:
                mProgressBar.setVisibility(GONE);
                head_tips_TextView.setVisibility(VISIBLE);
                mLastUpdatedTextView.setVisibility(VISIBLE);
                mArrowImageView.clearAnimation();
                mArrowImageView.setVisibility(VISIBLE);
                if (mIsBack) {
                    mIsBack = false;
                    mArrowImageView.clearAnimation();
                    mArrowImageView.startAnimation(mReverseAnimation);
                    head_tips = "正在刷新中...";
                } else {
                    head_tips = "请下拉刷新";
                }
                head_tips_TextView.setText(head_tips);
                break;

            case DONE:
                refreshHeadLinearLayout.setPadding(0, -1 * mHeadContentHeight, 0, 0);
                mProgressBar.setVisibility(GONE);
                mArrowImageView.clearAnimation();
                mArrowImageView.setImageResource(R.drawable.refresh_image);
                head_tips = "加载完成";
                head_tips_TextView.setText(head_tips);
                mLastUpdatedTextView.setVisibility(VISIBLE);
                break;

            case REFRESHING:
                refreshHeadLinearLayout.setPadding(0, 0, 0, 0);
                mProgressBar.setVisibility(VISIBLE);
                mArrowImageView.clearAnimation();
                mArrowImageView.setVisibility(GONE);
                head_tips = "正在刷新中……";
                head_tips_TextView.setText(head_tips);
                break;

            case RELEASE_TO_REFRESH:
                mArrowImageView.setVisibility(VISIBLE);
                mProgressBar.setVisibility(GONE);
                head_tips_TextView.setVisibility(VISIBLE);
                mLastUpdatedTextView.setVisibility(VISIBLE);
                mArrowImageView.clearAnimation();
                mArrowImageView.startAnimation(mAnimation);
                head_tips = "请释放后刷新";
                head_tips_TextView.setText(head_tips);
                break;
            default:
                break;
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        String head_tips = "上次刷新的时间为：" + getTimeString();
        mLastUpdatedTextView.setText(head_tips);
        super.setAdapter(adapter);
    }

    @SuppressLint("SimpleDateFormat")
    private String getTimeString() {
        return (new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()));
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

}
