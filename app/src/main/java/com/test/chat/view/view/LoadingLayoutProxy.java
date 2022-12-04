package com.test.chat.view.view;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashSet;

@RequiresApi(api = Build.VERSION_CODES.M)

public class LoadingLayoutProxy implements ILoadingLayout {

    private final HashSet<LoadingLayout> mLoadingLayouts;

    LoadingLayoutProxy() {
        mLoadingLayouts = new HashSet<>();
    }

    public void addLayout(LoadingLayout layout) {
        if (null != layout) {
            mLoadingLayouts.add(layout);
        }
    }

    @Override
    public void setLastUpdatedLabel(CharSequence label) {
        for (LoadingLayout layout : mLoadingLayouts) {
            layout.setLastUpdatedLabel(label);
        }
    }

    @Override
    public void setLoadingDrawable(Drawable drawable) {
        for (LoadingLayout layout : mLoadingLayouts) {
            layout.setLoadingDrawable(drawable);
        }
    }

    @Override
    public void setRefreshingLabel(CharSequence refreshingLabel) {
        for (LoadingLayout layout : mLoadingLayouts) {
            layout.setRefreshingLabel(refreshingLabel);
        }
    }

    @Override
    public void setPullLabel(CharSequence label) {
        for (LoadingLayout layout : mLoadingLayouts) {
            layout.setPullLabel(label);
        }
    }

    @Override
    public void setReleaseLabel(CharSequence label) {
        for (LoadingLayout layout : mLoadingLayouts) {
            layout.setReleaseLabel(label);
        }
    }

    public void setTextTypeface(Typeface tf) {
        for (LoadingLayout layout : mLoadingLayouts) {
            layout.setTextTypeface(tf);
        }
    }
}
