package com.test.chat.view.view;

import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.M)
public interface EmptyViewMethodAccessor {

    void setEmptyViewInternal(View emptyView);

    void setEmptyView(View emptyView);

}
