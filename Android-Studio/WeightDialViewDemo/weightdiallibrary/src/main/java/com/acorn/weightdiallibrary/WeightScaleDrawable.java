package com.acorn.weightdiallibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import com.acorn.weightdiallibrary.utils.TransUtil;


/**
 * 圆形刻度Drawable
 * Created by Acorn on 2015/8/26.
 */
public class WeightScaleDrawable extends Drawable {
    private static final Double PI = Math.PI;
    /**
     * 默认总刻度
     */
    private static final int DEFAULT_SCALE = 100;
    private static final int DEFAULT_LIGHT_STEP1 = 5;
    private static final int DEFAULT_LIGHT_STEP2 = 10;
    /**
     * 总刻度
     */
    private int totalScale;
    /**
     * 一级突出显示的间隔
     */
    private int lightStep1;
    /**
     * 二级突出显示的间隔
     */
    private int lightStep2;
    /**
     * drawable大小
     */
    private int size;
    /**
     * 半径
     */
    private int radius;
    /**
     * 圆心
     */
    private int cX, cY;
    private Path path;
    private Paint paint;
    private PointF[] startPoints;
    private PointF[] endPoints;
    //圆形背景图片宽高
    private int circleBackgroundSize;
    private Paint circleBackgroundPaint;
    private Bitmap backgroundBitmap;

    public WeightScaleDrawable() {
        this(DEFAULT_SCALE);
    }

    public WeightScaleDrawable(int scale) {
        this(scale, DEFAULT_LIGHT_STEP1, DEFAULT_LIGHT_STEP2);
    }

    /**
     * 圆形刻度Drawable
     *
     * @param scale 总刻度
     */
    public WeightScaleDrawable(int scale, int lightStep1, int lightStep2) {
        this.totalScale = scale;
//        this.size = size;
        this.lightStep1 = lightStep1;
        this.lightStep2 = lightStep2;
//        this.radius = size / 2;
//        this.cX = this.cY = size / 2;
        paint = new Paint();
        paint.setAntiAlias(true);  //抗锯齿
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xff3182a2);
    }

    @Override
    public void draw(Canvas canvas) {
        if (!path.isEmpty()) {
            if (circleBackgroundPaint != null && backgroundBitmap != null) {
                canvas.drawCircle(cX, cY, radius, circleBackgroundPaint);
            }
            canvas.drawPath(path, paint);
        }
    }

    public void setBackground(Bitmap bitmap) {
        if (bitmap == null) {
            circleBackgroundPaint = null;
            backgroundBitmap = null;
            invalidateSelf();
            return;
        }
        backgroundBitmap = bitmap;
        if (isVisible() && size != 0) {
            computeBackground();
            invalidateSelf();
        }
    }

    private void computeBackground() {
        if (backgroundBitmap == null)
            return;
        circleBackgroundSize = Math.min(backgroundBitmap.getWidth(), backgroundBitmap.getHeight());
        BitmapShader bitmapShader = new BitmapShader(backgroundBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Matrix matrix = new Matrix();
        float bitmapScale = (float) size / (float) circleBackgroundSize;
        matrix.setScale(bitmapScale, bitmapScale);
        bitmapShader.setLocalMatrix(matrix);
        if (circleBackgroundPaint == null) {
            circleBackgroundPaint = new Paint();
            circleBackgroundPaint.setAntiAlias(true);
        }
        circleBackgroundPaint.setShader(bitmapShader);
    }

    public void setScaleLineColor(int color) {
        paint.setColor(color);
        invalidateSelf();
    }

    public void setScale(int scale) {
        if (scale <= 0)
            return;
        this.totalScale = scale;
        createDrawGraph();
        invalidateSelf();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        computeSize(bounds);
        computeBackground();
        createDrawGraph();
    }

    private void computeSize(Rect bounds) {
        size = Math.min(bounds.width(), bounds.height());
        cX = cY = radius = size / 2;
    }

    /**
     * 初始化绘制图形
     */
    private void createDrawGraph() {
        path = new Path();
        getDrawPoints();
        for (int i = 0; i < startPoints.length; i++) {
            path.moveTo(startPoints[i].x, startPoints[i].y);
            path.lineTo(endPoints[i].x, endPoints[i].y);
        }
    }

    private void getDrawPoints() {
        startPoints = new PointF[totalScale];
        endPoints = new PointF[totalScale];
        float angle = 0;
        float angelStep = 360f / totalScale;
        for (int i = 0; i < totalScale; i++) {
            //起始点
            int startDistance;
            if (i % lightStep2 == 0)
                startDistance = radius - radius / 10 * 2;
            else if (i % lightStep1 == 0)
                startDistance = radius - (int) (radius / 10 * 1.5);
            else
                startDistance = radius - radius / 10;
            PointF startPoint = getPointByAngle(angle, startDistance);
            PointF endPoint = getPointByAngle(angle, radius);
            startPoints[i] = startPoint;
            endPoints[i] = endPoint;
//            DebugLog.i("angle("+angle+"),i:"+i+",start:"+startPoint+",end"+endPoint);
            angle += angelStep;
        }
    }


    /**
     * 根据角度angle获取在此角与圆心连线中距离圆心为distanceToCenter远的点
     *
     * @param angle            以圆心为起点垂直向上作边a,与此边a按顺时针方向的夹角
     * @param distanceToCenter 距离圆心的距离
     */
    public PointF getPointByAngle(float angle, int distanceToCenter) {
        //因为默认角度是相对于以圆心起点水平向右的边的夹角,
        //而我们要的是相对于垂直向上的边的夹角,所以往回转90度
        angle = angle - 90;
        PointF res = new PointF();
        double x1, y1;
        //角度转成弧度
        double radians = TransUtil.angle2radians(angle);
        x1 = cX + distanceToCenter * Math.cos(radians);
        y1 = cY + distanceToCenter * Math.sin(radians);
        res.set((float) x1, (float) y1);
        return res;
    }

    /**
     * 根据刻度获取在此角与圆心连线中距离圆心为distanceToCenter远的点
     *
     * @param scale            刻度
     * @param distanceToCenter 距离圆心的距离
     * @return
     */
    public PointF getPointByScale(int scale, int distanceToCenter) {
        return getPointByAngle(scaleToAngle(scale), distanceToCenter);
    }

    /**
     * 刻度转角度
     *
     * @param scale
     * @return
     */
    public float scaleToAngle(int scale) {
        return (float) scale * (360f / (float) totalScale);
    }

    public int angleToScale(float angle) {
        float step = 360f / (float) totalScale;
        //刻度近似值
        float approximateScale = angle / step;
        //四舍五入
        int approximateScaleInt = (int) (approximateScale + 0.5f);
        return approximateScaleInt;
    }

    /**
     * 角度转刻度,当刻度等于总刻度时返回0
     *
     * @param angle
     * @return
     */
    public int angleToScaleWithoutTotalScale(float angle) {
        float step = 360f / (float) totalScale;
        //刻度近似值
        float approximateScale = angle / step;
        //四舍五入
        int approximateScaleInt = (int) (approximateScale + 0.5f);
        if (approximateScaleInt == totalScale)
            approximateScaleInt = 0;
        return approximateScaleInt;
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

    @Override
    public int getIntrinsicHeight() {
        return size;
    }

    @Override
    public int getIntrinsicWidth() {
        return size;
    }

    public int getcX() {
        return cX;
    }

    public int getcY() {
        return cY;
    }

    public int getRadius() {
        return radius;
    }
}
