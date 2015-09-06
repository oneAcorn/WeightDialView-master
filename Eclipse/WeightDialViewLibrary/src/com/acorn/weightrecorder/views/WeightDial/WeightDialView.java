package com.acorn.weightrecorder.views.WeightDial;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MotionEventCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import com.acorn.weightdialviewlibrary.R;
import com.acorn.weightrecorder.utils.LogUtil;

/**
 * Created by Acorn on 2015/8/26.
 */
public class WeightDialView extends View {
    private float density;
    //默认大小dp
    private static final int DEFAULT_SIZE = 250;
    /**
     * 默认指示器thumb触摸面积
     */
    private static final int DEFAULT_THUMB_TOUCH_AREA = 50;
    private static final int DEFAULT_TEXT_COLOR = 0xff000000;
    //sp
    private static final int DEFAULT_TEXT_SIZE = 32;
    private OnScaleChangeListener onScaleChangeListener;
    private WeightScaleDrawable weightScaleDrawable;
    private ThumbDrawable thumbDrawable;
    /**
     * 圆心
     */
    private int cX, cY;
    /**
     * 半径
     */
    private int radius;
    /**
     * 重复利用的Rect
     */
    private Rect tempRect = new Rect();
    /**
     * 局部刷新区域rect
     */
    private Rect mInvalidateRect = new Rect();
    /**
     * 为thumbDrawable增加触摸面积
     */
    private int mAddedTouchBounds;
    private boolean mIsDragging;
    private ValueAnimator thumbResetAnim;
    /**
     * 滑动速度检测类
     */
    private VelocityTracker mVelocityTracker;
    /**
     * 最小滑动速率
     */
    private int minFlingVelocity;
    /**
     * 最大滑动速率
     */
    private int maxFlingVelocity;
    private float lastAngle, newAngle;
    /**
     * 是否顺时针转动
     */
    private boolean isClockwise;
    /**
     * 惯性动画
     */
    private ValueAnimator thumbInertiaAnim;
    private Rect textRect;
    private TextPaint textPaint;
    /**
     * 总刻度
     */
    private int totalScale;
    private CharSequence text;
    private int curScale = 0;
    /**
     * 正在复位动画中
     */
    private boolean isResetting;
    private int lastScale = 0;
    /**
     * 圈数
     */
    private int circle;
    /**
     * 按下事件
     */
    private boolean isFirstDown;

    public WeightDialView(Context context) {
        this(context, null);
    }

    public WeightDialView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WeightDialView);
        totalScale = ta.getInt(R.styleable.WeightDialView_total_scale, 100);
        ta.recycle();

        density = context.getResources().getDisplayMetrics().density;
        weightScaleDrawable = new WeightScaleDrawable(totalScale);
        weightScaleDrawable.setCallback(this);
        thumbDrawable = new ThumbDrawable();
        thumbDrawable.setCallback(this);
        thumbDrawable.setBounds(0, 0, thumbDrawable.getIntrinsicWidth(), thumbDrawable.getIntrinsicHeight());
        /**增加thumb触摸面积到32dp*/
        int touchBounds = (int) (density * DEFAULT_THUMB_TOUCH_AREA);
        mAddedTouchBounds = (touchBounds - thumbDrawable.getIntrinsicWidth()) / 2;

        //初始化最小和最大滑动速率
        ViewConfiguration vc = ViewConfiguration.get(context);
        minFlingVelocity = vc.getScaledMinimumFlingVelocity() * 8;
//        maxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        maxFlingVelocity = 3600;
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(DEFAULT_TEXT_COLOR);
        textPaint.setTextSize(sp2px(context, DEFAULT_TEXT_SIZE));
        // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
        textPaint.setTextAlign(Paint.Align.CENTER);
        LogUtil.i("minFlingVelocity:" + minFlingVelocity + ",maxFlingVelocity:" + maxFlingVelocity);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        weightScaleDrawable.draw(canvas);
        thumbDrawable.draw(canvas);
        if (text != null)
            drawText(text, canvas);

//        StaticLayout staticLayout=new StaticLayout("50.58",textPaint,textRect.right-textRect.left,
//                Layout.Alignment.ALIGN_CENTER,1f,0f,true);
//        canvas.save();
//        canvas.translate(textRect.left, textRect.top);
//        staticLayout.draw(canvas);
//        canvas.restore();
    }

    private void drawText(CharSequence txt, Canvas canvas) {
        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
        // 转载请注明出处：http://blog.csdn.net/hursing
        int baseline = textRect.top + (textRect.bottom - textRect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        //保存canvas状态
        canvas.save();
        //限制最大绘制范围
        canvas.clipRect(textRect);
        canvas.drawText(txt, 0, txt.length(), textRect.centerX(), baseline, textPaint);
        //恢复之前保存的canvas状态
        canvas.restore();
    }

    /**
     * 设置总刻度
     *
     * @param scale
     */
    public void setTotalScale(int scale) {
        if (weightScaleDrawable != null) {
            weightScaleDrawable.setScale(scale);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int mWidth = 0, mHeight = 0;
        if (widthSpecMode == MeasureSpec.EXACTLY) { //match_parent
            mWidth = widthSpecSize;
        } else {
            mWidth = (int) (DEFAULT_SIZE * density);
            if (widthSpecMode == MeasureSpec.AT_MOST) { //wrap_content
                mWidth = Math.min(widthSpecSize, mWidth);
            }
        }
        if (heightSpecMode == MeasureSpec.EXACTLY) { //match_parent
            mHeight = heightSpecSize;
        } else {
            mHeight = (int) (DEFAULT_SIZE * density);
            if (heightSpecMode == MeasureSpec.AT_MOST) { //wrap_content
                mHeight = Math.min(heightSpecSize, mHeight);
            }
        }
        int size = Math.min(mWidth, mHeight);
        LogUtil.i("onMeasure()" + size);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogUtil.i("onSizeChanged(w,h):" + w + "," + h);
        computeSize(w, h);
        setScale(curScale);
    }

    private void computeSize(int w, int h) {
        //必须设置Drawable的绘制范围
        weightScaleDrawable.setBounds(0, 0, w, h);
        cX = weightScaleDrawable.getcX();
        cY = weightScaleDrawable.getcY();
        radius = weightScaleDrawable.getRadius();
        textRect = new Rect();
        weightScaleDrawable.copyBounds(textRect);
        int textInsetWidth = radius * 3 / 5;
        LogUtil.i("textInsetWidth:" + textInsetWidth);
        textRect.inset(textInsetWidth, textInsetWidth);
    }

    /**
     * 设置刻度
     *
     * @param scale
     */
    public void setScale(int scale) {
        if (scale < 0)
            scale = 0;
        else if (scale > totalScale)
            scale = totalScale;
        curScale = scale;
        updateThumbByScale(scale);
    }

    /**
     * 设置表盘背景
     * @param bitmap
     */
    public void setCircleBackground(Bitmap bitmap){
        weightScaleDrawable.setBackground(bitmap);
    }

    /**
     * 设置刻度线的颜色
     * @param color
     */
    public void setScaleLineColor(int color){
        weightScaleDrawable.setScaleLineColor(color);
    }

    public void setCircle(int circle) {
        this.circle = circle;
    }

    public int getScale() {
        return curScale;
    }
    
    public int getCircle() {
        return circle;
    }

    public float getValue() {
        return Float.valueOf(circle + "." + (curScale > 9 ? curScale : "0" + curScale));
    }

    public int getTotalScale() {
        return totalScale;
    }

    public void setText(CharSequence txt) {
        this.text = txt;
        if (textRect != null) {
            invalidate(textRect);
        }
    }

    public void setTextColor(int color) {
        textPaint.setColor(color);
    }

    /**
     * @param size sp
     */
    public void setTextSize(float size) {
        textPaint.setTextSize(sp2px(getContext(), size));
    }

    private void updateThumbByScale(int scale) {
        updateThumbByAngle(weightScaleDrawable.scaleToAngle(scale));
    }

    private void updateThumbByAngle(float angle) {
        angle = angle % 360f; //取余运算保证angle范围在0~360
        //刻度盘半径
        int weightScaleRadius = radius;
        //指示器宽度的一半
        int thumbHalfWidth = thumbDrawable.getIntrinsicWidth() / 2;
        int distance = weightScaleRadius - weightScaleRadius / 5 - thumbHalfWidth - (int) (3 * density);
        //计算出thumbDrawable圆心位置
        PointF pointF = CircleUtil.getPositionByAngle(angle, distance, cX, cY);
        //根据圆心计算ltrb
        int left = (int) pointF.x - thumbHalfWidth;
        int top = (int) pointF.y - thumbHalfWidth;
        int right = left + thumbDrawable.getIntrinsicWidth();
        int bottom = top + thumbDrawable.getIntrinsicHeight();
        //thumb没改变位置时的bounds
        thumbDrawable.copyBounds(mInvalidateRect);
        thumbDrawable.setBounds(left, top, right, bottom);
        //thumb改变位置后的bounds
        final Rect finalRect = tempRect;
        thumbDrawable.copyBounds(finalRect);
        //加上增加的触摸区域
//        mInvalidateRect.inset(-mAddedTouchBounds, -mAddedTouchBounds);
//        finalRect.inset(-mAddedTouchBounds, -mAddedTouchBounds);
        //合并2个rect,获取受影响区域
        mInvalidateRect.union(finalRect);
        //根据受影响区域刷新画布
        invalidate(mInvalidateRect);
        updateStatus(angle);
    }

    private void updateStatus(float angle) {
        newAngle = angle;
        if (isResetting) //复位时不计算顺时针
            return;
//        LogUtil.i("updateStatus"+isResetting+","+angle);
        if (Math.abs(newAngle) - Math.abs(lastAngle) > 300) { //有可能从0度逆时针转到360度那边,此时逆时针
            LogUtil.i("false newAngle:" + newAngle + ",lastAngle:" + lastAngle);
            isClockwise = false;
        } else if (Math.abs(lastAngle) - Math.abs(newAngle) > 300) {//有可能从360度转到0度那边,此时顺时针
            LogUtil.i("true newAngle:" + newAngle + ",lastAngle:" + lastAngle);
            isClockwise = true;
        } else {
            isClockwise = newAngle > lastAngle;
        }
//        LogUtil.i("newAngle:"+newAngle+",lastAngle:"+lastAngle+",isClockwise:"+isClockwise);
        notifyScale(newAngle, isClockwise, false);
        lastAngle = angle;
    }

    private void notifyScale(float newAngle, boolean isClockwise, boolean isResetNotify) {
        if (isResetting)
            return;
        //将angle取正
        if (newAngle < 0)
            newAngle = 360f + newAngle;
        int newScale = weightScaleDrawable.angleToScaleWithoutTotalScale(newAngle);
        curScale = newScale;
        LogUtil.i("notifyScale(),isClockwise:" + isClockwise + ",newScale:" + newScale + ",lastScale:" + lastScale + "," + isResetNotify);
        if (!isFirstDown) { //因为第一次按下时会有些许偏移导致角度改变且无法准确测量时针方向,所以不判断圈数
            if (isClockwise && newScale < lastScale) {
                circle++;
            } else if (!isClockwise && newScale > lastScale) {
                circle--;
            }
        } else
            isFirstDown = false;
        lastScale = newScale;
        if (onScaleChangeListener != null) {
            onScaleChangeListener.onScaleChange(newScale, isClockwise, circle);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled())
            return false;
        //兼容
        int actionMasked = MotionEventCompat.getActionMasked(event);
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                startDragging(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onDragging(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopDragging(event);
                break;
        }
        return true;
    }

    private void startDragging(MotionEvent event) {
        final Rect bounds = tempRect;
        //把thumbDrawable的bounds复制到bounds中去
        thumbDrawable.copyBounds(bounds);
        bounds.inset(-mAddedTouchBounds, -mAddedTouchBounds);
        if (bounds.contains((int) event.getX(), (int) event.getY())) {
            mIsDragging = true;
            isFirstDown = true;
            thumbDrawable.press();
            //加入速度检测
            mVelocityTracker = VelocityTracker.obtain();
            mVelocityTracker.addMovement(event);
            LogUtil.i("startDragging");
        }
    }

    private void onDragging(MotionEvent event) {
        if (!mIsDragging)
            return;
        //触摸点指向的角度
        float thumbAngle = CircleUtil.getAngleByPosition(event.getX(), event.getY(), cX, cY, 0);
        LogUtil.i("onDragging()thumbAngle:" + thumbAngle + "," + isFirstDown + ",isResetting:" + isResetting);
        updateThumbByAngle(thumbAngle);
        mVelocityTracker.addMovement(event);
    }

    private void stopDragging(MotionEvent event) {
        if (mIsDragging) {
            thumbDrawable.unPress();
            //通过滑动的距离计算出X,Y方向的速度
            mVelocityTracker.computeCurrentVelocity(1000);
            float velocityX = Math.abs(mVelocityTracker.getXVelocity());
            float velocityY = Math.abs(mVelocityTracker.getYVelocity());
//            LogUtil.i("velocity:" + velocityX + "," + velocityY);
            computeThumbInertiaAnimator(velocityX, velocityY, event);
        }
        mIsDragging = false;
        isFirstDown = false;
        if (mVelocityTracker != null) { //移除速度检测
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 计算thumb惯性动画
     */
    private void computeThumbInertiaAnimator(float velocityX, float velocityY, MotionEvent event) {
        float velocity = Math.min(Math.max(velocityX, velocityY) / 4, maxFlingVelocity);
        if (velocity > minFlingVelocity) {
            if (thumbInertiaAnim == null)
                initInertiaAnimator();
            float moveToAngle;
            if (isClockwise) {
                moveToAngle = newAngle + velocity / 10;
                if (moveToAngle > 360f) {
                    thumbInertiaAnim.setFloatValues(newAngle, 360f, moveToAngle);
                } else {
                    thumbInertiaAnim.setFloatValues(newAngle, moveToAngle);
                }
            } else {
                moveToAngle = newAngle - velocity / 10;
                if (moveToAngle < 0) {
                    thumbInertiaAnim.setFloatValues(newAngle, 0, moveToAngle);
                } else {
                    thumbInertiaAnim.setFloatValues(newAngle, moveToAngle);
                }
            }
            thumbInertiaAnim.start();
        } else {
            computeThumbResetAnimator();
        }
    }

    /**
     * 计算thumb复位动画
     */
    private void computeThumbResetAnimator() {
        //触摸点指向的角度
        float thumbAngle;
        if (newAngle < 0)
            thumbAngle = 360f + newAngle;
        else
            thumbAngle = newAngle;
        //根据thumb当前位置计算最接近的刻度
        int approximateScale = weightScaleDrawable.angleToScale(thumbAngle);
        //计算出相应最近似刻度的角度
        float angleOfScale = weightScaleDrawable.scaleToAngle(approximateScale);
        //间隔角度
        float diffAngle = Math.abs(angleOfScale - thumbAngle);
        LogUtil.i("newAngle:" + thumbAngle + ",isClockwise:" + isClockwise + ",apprScale:" + approximateScale);
        if (diffAngle > 5) { //超过一定角度用动画复位
            if (thumbResetAnim == null) {
                initResetAnimator();
            }
            long duration = (int) (diffAngle * 10);
            thumbResetAnim.setFloatValues(thumbAngle, angleOfScale);
            thumbResetAnim.setDuration(duration);
            isResetting = true;
            thumbResetAnim.start();
        } else {
            isResetting = true;
            updateThumbByScale(approximateScale);
            isResetting = false;
            notifyScale(newAngle, isClockwise, true);
        }
    }

    ValueAnimator.AnimatorUpdateListener thumbResetUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float angle = ((Number) animation.getAnimatedValue()).floatValue();
            updateThumbByAngle(angle);
        }
    };

    private void initResetAnimator() {
        thumbResetAnim = ValueAnimator.ofFloat(0f, 0f);
        thumbResetAnim.setInterpolator(new DecelerateInterpolator());
        thumbResetAnim.addUpdateListener(thumbResetUpdateListener);
        thumbResetAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                LogUtil.i("onAnimationStart()");
//                isResetting=true;  //写在这里有延迟,并不靠谱,应该卸载调用动画.start()上面一行.
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                LogUtil.i("onAnimationEnd():" + newAngle + ",isClockwise:" + isClockwise);
                isResetting = false;
                notifyScale(newAngle, isClockwise, true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isResetting = false;
                notifyScale(newAngle, isClockwise, true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void initInertiaAnimator() {
        thumbInertiaAnim = ValueAnimator.ofFloat(0f, 0f);
        thumbInertiaAnim.setInterpolator(new DecelerateInterpolator());
        thumbInertiaAnim.addUpdateListener(thumbResetUpdateListener);
        thumbInertiaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                LogUtil.i("Inertia anim end" + newAngle);
                computeThumbResetAnimator();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
//        return super.verifyDrawable(who);
        //允许drawable刷新自己以执行动画
        return who == thumbDrawable || who==weightScaleDrawable|| super.verifyDrawable(who);
    }

    public void setOnScaleChangeListener(OnScaleChangeListener onScaleChangeListener) {
        this.onScaleChangeListener = onScaleChangeListener;
    }

    /**
     * sp转px
     *
     * @param context
     * @param spVal
     * @return
     */
    public static int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }

    public interface OnScaleChangeListener {
        void onScaleChange(int newScale, boolean isClockwise, int circles);
    }
}
