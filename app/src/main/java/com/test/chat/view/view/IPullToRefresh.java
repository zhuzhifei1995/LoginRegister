package com.test.chat.view.view;

import android.os.Build;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.M)

public interface IPullToRefresh<T extends View> {
    boolean demo();

    PullToRefreshBase.Mode getCurrentMode();

    boolean getFilterTouchEvents();

    void setFilterTouchEvents(boolean filterEvents);

    ILoadingLayout getLoadingLayoutProxy();

    ILoadingLayout getLoadingLayoutProxy(boolean includeStart, boolean includeEnd);

    PullToRefreshBase.Mode getMode();

    void setMode(PullToRefreshBase.Mode mode);

    T getRefreshableView();

    boolean getShowViewWhileRefreshing();

    void setShowViewWhileRefreshing(boolean showView);

    PullToRefreshBase.State getState();

    boolean isPullToRefreshEnabled();

    boolean isPullToRefreshOverScrollEnabled();

    void setPullToRefreshOverScrollEnabled(boolean enabled);

    boolean isRefreshing();

    void setRefreshing(boolean doScroll);

    boolean isScrollingWhileRefreshingEnabled();

    void setScrollingWhileRefreshingEnabled(boolean scrollingWhileRefreshingEnabled);

    void onRefreshComplete();

    void setOnPullEventListener(PullToRefreshBase.OnPullEventListener<T> listener);

    void setOnRefreshListener(PullToRefreshBase.OnRefreshListener<T> listener);

    void setOnRefreshListener(PullToRefreshBase.OnRefreshListener2<T> listener);

    void setRefreshing();

    void setScrollAnimationInterpolator(Interpolator interpolator);

}