package com.test.chat.util;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.M)
public class KeyboardStateObserver {

    private static final String TAG = ActivityUtil.TAG;
    private View mChildOfContent;
    private int usableHeightPrevious;
    private OnKeyboardVisibilityListener onKeyboardVisibilityListener;

    private KeyboardStateObserver(Activity activity) {
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                possiblyResizeChildOfContent();
            }
        });
    }

    public static KeyboardStateObserver getKeyboardStateObserver(Activity activity) {
        return new KeyboardStateObserver(activity);
    }

    public void setKeyboardVisibilityListener(OnKeyboardVisibilityListener onKeyboardVisibilityListener) {
        this.onKeyboardVisibilityListener = onKeyboardVisibilityListener;
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                if (onKeyboardVisibilityListener != null) {
                    onKeyboardVisibilityListener.onKeyboardShow();
                }
            } else {
                if (onKeyboardVisibilityListener != null) {
                    onKeyboardVisibilityListener.onKeyboardHide();
                }
            }
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect rect = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(rect);
        return (rect.bottom - rect.top);
    }

    public interface OnKeyboardVisibilityListener {
        void onKeyboardShow();

        void onKeyboardHide();
    }
}
