package com.test.chat.view.view;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.M)

public interface ILoadingLayout {
    void setLastUpdatedLabel(CharSequence label);

    void setLoadingDrawable(Drawable drawable);

    void setPullLabel(CharSequence pullLabel);

    void setRefreshingLabel(CharSequence refreshingLabel);

    void setReleaseLabel(CharSequence releaseLabel);

    void setTextTypeface(Typeface tf);

}
