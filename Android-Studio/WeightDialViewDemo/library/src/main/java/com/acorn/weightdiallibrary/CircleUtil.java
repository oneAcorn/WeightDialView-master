package com.acorn.weightdiallibrary;

import android.graphics.PointF;


/**
 * 有关圆形的一些数学计算工具
 * Created by Acorn on 2015/8/27.
 */
public class CircleUtil {
    /**
     * 根据角度angle获取在此角与圆心连线中距离圆心为distanceToCenter远的点
     *
     * @param angle            以圆心为起点垂直向上作边a,与此边a按顺时针方向的夹角
     * @param distanceToCenter 距离圆心的距离
     * @param cX               圆心
     * @param cY               圆心
     */
    public static PointF getPositionByAngle(float angle, int distanceToCenter, int cX, int cY) {
        //因为默认角度是相对于以圆心起点水平向右的边的夹角,
        //而我们要的是相对于垂直向上的边的夹角,所以往回转90度
        angle = angle - 90;
        PointF res = new PointF();
        double x1, y1;
        //角度转成弧度
        double radians = angle2radians(angle);
        x1 = cX + distanceToCenter * Math.cos(radians);
        y1 = cY + distanceToCenter * Math.sin(radians);
        res.set((float) x1, (float) y1);
        return res;
    }

    /**
     * 根据坐标计算点相对圆心的角度,与从圆心垂直向上作的边的夹角
     *
     * @param x1     坐标
     * @param y1     坐标
     * @param cX     圆心
     * @param cY     圆心
     * @param radius 圆的半径,用于判断点是否超出圆的范围,超出时返回-1.传值为0时不判断是否超出范围
     * @return 角度, 若radius不为0且坐标超出圆的范围时返回-1.
     */
    public static float getAngleByPosition(float x1, float y1, int cX, int cY, int radius) {
        // 对边
        float oppositeSide = y1 - cY;
        // 邻边
        float adjacentSide = x1 - cX;
        if (radius > 0) { //半径大于0时,判断坐标点是否超出圆形范围
            // 点超出圆形范围
            if (Math.abs(oppositeSide) > radius || Math.abs(adjacentSide) > radius) {
                return -1f;
            }
            // 点到圆心距离超过了半径
            if (Math.sqrt(oppositeSide * oppositeSide + adjacentSide * adjacentSide) > radius) {
                return -1f;
            }
        }
        //弧度
        double tanRadians = Math.atan(oppositeSide / adjacentSide);
        double angle;
        if (x1 < cX) {
            angle = 270d + radians2angle(tanRadians);
        } else {
            angle=90d+radians2angle(tanRadians);
        }
        return (float) angle;
    }

    /**
     * 获取扇形中心点坐标
     * @param sAngle 扇形起始角度
     * @param tAngle 扇形结束角度
     * @param cX 圆心
     * @param cY 圆心
     * @param radius 半径
     * @return 扇形中心点坐标
     */
    public static PointF getSectorCenterPosition(float sAngle, float tAngle, int cX, int cY, int radius) {
        // 计算中心角度
        // 如:一个扇形,起始角度为10,结束角度为40,则arc=10+(40-10)/2
        float angle = sAngle + (tAngle - sAngle) / 2;
        return getPositionByAngle(angle, radius, cX, cY);
    }

    /**
     * 角度转弧度
     *
     * @param angle
     * @return
     */
    public static double angle2radians(float angle) {
        return angle / 180f * Math.PI;
    }

    /**
     * 弧度转角度
     *
     * @param radians
     * @return
     */
    public static double radians2angle(double radians) {
        return 180f * radians / Math.PI;
    }
}
