package com.acorn.weightdiallibrary;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateInterpolator;


/**
 * 指示器Drawable
 * Created by Acorn on 2015/8/26.
 */
public class ThumbDrawable extends Drawable {
    private static final int DEFAULT_COLOR = 0xffff4044;
    private static final int DEFAULT_SIZE = 20;
    /**
     * 动画时缩放比率
     */
    private static final float ANIMATOR_SCALE_RATE = 1.4f;
    private int color;
    private int size;
    private Paint paint;
    private float tempRadius;
    private ValueAnimator pressAnim;

    public ThumbDrawable() {
        this(DEFAULT_COLOR, DEFAULT_SIZE);
    }

    public ThumbDrawable(int color) {
        this(color, DEFAULT_SIZE);
    }

    public ThumbDrawable(int color, int size) {
        this.color = color;
        this.size = size;
        paint = new Paint();
        paint.setAntiAlias(true);  //抗锯齿
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        tempRadius = size / 2;
        createPressAnimator();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), tempRadius, paint);
//        LogUtil.i("radius:" + tempRadius);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public int getRadius() {
        return size / 2;
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) (size * ANIMATOR_SCALE_RATE);
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) (size * ANIMATOR_SCALE_RATE);
    }

    public void press() {
        if (!pressAnim.isStarted())
            pressAnim.start();
    }

    public void unPress() {
        if (pressAnim.isRunning())
            pressAnim.cancel();
        pressAnim.reverse();
    }

    private void createPressAnimator() {
        ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = ((Number) animation.getAnimatedValue()).floatValue();
                tempRadius = scale * size / 2;
                invalidateSelf();
            }
        };
        pressAnim = ValueAnimator.ofFloat(1.0f, ANIMATOR_SCALE_RATE);
        pressAnim.setInterpolator(new AccelerateInterpolator());
        pressAnim.addUpdateListener(updateListener);
        pressAnim.setDuration(300);
    }
}
