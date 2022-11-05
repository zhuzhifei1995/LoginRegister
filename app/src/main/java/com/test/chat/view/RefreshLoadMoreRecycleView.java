package com.test.chat.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class RefreshLoadMoreRecycleView extends RecyclerView implements RecyclerView.OnTouchListener {

    private Boolean isLoadMore;
    private Boolean isLoadEnd;
    private Boolean isLoadStart;
    private Boolean isRefresh;
    private RecycleViewOnScrollListener recycleViewOnScrollListener;
    private float moveLocation;

    public RefreshLoadMoreRecycleView(Context context) {
        super(context);
        init(context);
    }

    public RefreshLoadMoreRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RefreshLoadMoreRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void init(Context context) {
        isLoadEnd = false;
        isLoadStart = true;

        this.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    loadData();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    isLoadEnd = !recyclerView.canScrollVertically(1);
                } else if (dy < 0) {
                    isLoadStart = !recyclerView.canScrollVertically(-1);

                }
            }
        });
        this.setOnTouchListener(this);
    }

    private void loadData() {
        if (isLoadEnd) {
            if (isLoadMore) {
                if (getRecycleViewOnScrollListener() != null) {
                    getRecycleViewOnScrollListener().onLoadMore();
                }
            } else {
                if (getRecycleViewOnScrollListener() != null) {
                    getRecycleViewOnScrollListener().onLoaded();
                }
            }
            isLoadEnd = false;
        } else if (isLoadStart) {
            if (isRefresh) {
                if (getRecycleViewOnScrollListener() != null) {
                    getRecycleViewOnScrollListener().onRefresh();
                }
                isLoadStart = false;
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (moveLocation == -1) {
            moveLocation = motionEvent.getRawY();
        }
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_MOVE:
                final float deltaY = motionEvent.getRawY() - moveLocation;
                moveLocation = motionEvent.getRawY();
                if (deltaY < 0) {
                    isLoadEnd = !this.canScrollVertically(1);
                } else if (deltaY > 0) {
                    isLoadStart = !this.canScrollVertically(-1);
                }
                break;
            case MotionEvent.ACTION_DOWN:
                moveLocation = motionEvent.getRawY();
                break;
            default:
                moveLocation = -1;
                break;
        }

        return false;
    }

    public RecycleViewOnScrollListener getRecycleViewOnScrollListener() {
        return recycleViewOnScrollListener;
    }

    public void setRecycleViewOnScrollListener(RecycleViewOnScrollListener recycleViewOnScrollListener) {
        this.recycleViewOnScrollListener = recycleViewOnScrollListener;
    }

    public Boolean getLoadMore() {
        return isLoadMore;
    }

    public void setLoadMoreEnable(Boolean loadMore) {
        isLoadMore = loadMore;
    }

    public Boolean getRefresh() {
        return isRefresh;
    }

    public void setRefreshEnable(Boolean refresh) {
        isRefresh = refresh;
    }

    public interface RecycleViewOnScrollListener {
        void onRefresh();

        void onLoadMore();

        void onLoaded();
    }
}
