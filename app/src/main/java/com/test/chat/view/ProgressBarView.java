package com.test.chat.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.test.chat.R;

public class ProgressBarView extends View {

    private Paint mPaintBack;
    private Paint mPaint;
    private Paint mPaintText;
    private float process;
    private int textSize = 10;
    private long duration = 3000;
    private int strokeWidth = 15;

    public ProgressBarView(Context context) {
        super(context);
        init();
    }

    public ProgressBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaintBack = new Paint();
        mPaintBack.setColor(getResources().getColor(R.color.gray));
        mPaintBack.setStyle(Paint.Style.STROKE);
        mPaintBack.setAntiAlias(true);
        mPaintBack.setStrokeCap(Paint.Cap.ROUND);
        mPaintBack.setStrokeWidth(strokeWidth);
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.text_bottom));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(strokeWidth);
        mPaintText = new Paint();
        mPaintText.setAntiAlias(true);
        mPaintText.setStyle(Paint.Style.STROKE);
        mPaintText.setColor(Color.BLACK);
        mPaintBack.setStrokeCap(Paint.Cap.ROUND);
        mPaintText.setTextSize(sp2px(textSize));
    }

    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        @SuppressLint("DrawAllocation")
        RectF rectF = new RectF(strokeWidth, 15, getWidth() - strokeWidth, getHeight() - strokeWidth);
        canvas.drawArc(rectF, 0, 360, false, mPaintBack);
        canvas.drawArc(rectF, 0, process, false, mPaint);
        int percent = (int) (process / 360 * 100);
        Paint.FontMetrics fm = mPaintText.getFontMetrics();
        String text = "完成";
        String defaultText = "0%";
        int mTxtWidth = (int) mPaintText.measureText(text, 0, defaultText.length());
        int mTxtHeight = (int) Math.ceil(fm.descent - fm.ascent);
        int x = getWidth() / 2 - mTxtWidth / 2;
        int y = getHeight() / 2 + mTxtHeight / 4;
        if (percent < 100) {
            canvas.drawText(percent + "%", x, y, mPaintText);
        } else {
            canvas.drawText(text, x, y, mPaintText);
        }
    }

    public void start() {
        float startDegree = 0;
        float endDegree = 360;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(startDegree, endDegree);
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            process = (float) animation.getAnimatedValue();
            invalidate();
        });
        valueAnimator.start();
    }

    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }
}
