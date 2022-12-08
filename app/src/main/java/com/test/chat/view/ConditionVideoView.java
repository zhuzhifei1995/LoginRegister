package com.test.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class ConditionVideoView extends VideoView {

    public ConditionVideoView(Context context) {
        super(context);
    }

    public ConditionVideoView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ConditionVideoView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
