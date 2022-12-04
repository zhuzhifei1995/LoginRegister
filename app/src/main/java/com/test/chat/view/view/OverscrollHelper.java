package com.test.chat.view.view;

import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.test.chat.util.ActivityUtil;

@RequiresApi(api = Build.VERSION_CODES.M)
public final class OverscrollHelper {

    static final String TAG = ActivityUtil.TAG;
    static final float DEFAULT_OVERSCROLL_SCALE = 1f;

    public static void overScrollBy(final PullToRefreshBase<?> view, final int deltaX, final int scrollX,
                                    final int deltaY, final int scrollY, final boolean isTouchEvent) {
        overScrollBy(view, deltaX, scrollX, deltaY, scrollY, 0, isTouchEvent);
    }

    public static void overScrollBy(final PullToRefreshBase<?> view, final int deltaX, final int scrollX,
                                    final int deltaY, final int scrollY, final int scrollRange, final boolean isTouchEvent) {
        overScrollBy(view, deltaX, scrollX, deltaY, scrollY, scrollRange, 0, DEFAULT_OVERSCROLL_SCALE, isTouchEvent);
    }

    public static void overScrollBy(final PullToRefreshBase<?> view, final int deltaX, final int scrollX,
                                    final int deltaY, final int scrollY, final int scrollRange, final int fuzzyThreshold,
                                    final float scaleFactor, final boolean isTouchEvent) {

        final int deltaValue, currentScrollValue, scrollValue;
        switch (view.getPullToRefreshScrollDirection()) {
            case HORIZONTAL:
                deltaValue = deltaX;
                scrollValue = scrollX;
                currentScrollValue = view.getScrollX();
                break;
            case VERTICAL:
            default:
                deltaValue = deltaY;
                scrollValue = scrollY;
                currentScrollValue = view.getScrollY();
                break;
        }

        if (view.isPullToRefreshOverScrollEnabled() && !view.isRefreshing()) {
            final PullToRefreshBase.Mode mode = view.getMode();

            if (mode.permitsPullToRefresh() && !isTouchEvent && deltaValue != 0) {
                final int newScrollValue = (deltaValue + scrollValue);

                if (PullToRefreshBase.DEBUG) {
                    Log.e(TAG, "OverScroll. DeltaX: " + deltaX + ", ScrollX: " + scrollX + ", DeltaY: " + deltaY
                            + ", ScrollY: " + scrollY + ", NewY: " + newScrollValue + ", ScrollRange: " + scrollRange
                            + ", CurrentScroll: " + currentScrollValue);
                }

                if (newScrollValue < (-fuzzyThreshold)) {
                    if (mode.showHeaderLoadingLayout()) {
                        if (currentScrollValue == 0) {
                            view.setState(PullToRefreshBase.State.OVER_SCROLLING);
                        }

                        view.setHeaderScroll((int) (scaleFactor * (currentScrollValue + newScrollValue)));
                    }
                } else if (newScrollValue > (scrollRange + fuzzyThreshold)) {
                    if (mode.showFooterLoadingLayout()) {
                        if (currentScrollValue == 0) {
                            view.setState(PullToRefreshBase.State.OVER_SCROLLING);
                        }

                        view.setHeaderScroll((int) (scaleFactor * (currentScrollValue + newScrollValue - scrollRange)));
                    }
                } else if (Math.abs(newScrollValue) <= fuzzyThreshold
                        || Math.abs(newScrollValue - scrollRange) <= fuzzyThreshold) {
                    view.setState(PullToRefreshBase.State.RESET);
                }
            } else if (isTouchEvent && PullToRefreshBase.State.OVER_SCROLLING == view.getState()) {
                view.setState(PullToRefreshBase.State.RESET);
            }
        }
    }

    static boolean isAndroidOverScrollEnabled(View view) {
        return view.getOverScrollMode() != View.OVER_SCROLL_NEVER;
    }
}
