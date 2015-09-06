package com.acorn.weightrecorder.utils;

import android.util.Log;

/**
 * 日志工具类
 * 
 * AUTHOR:Skyin_wd
 * 
 * TIME:2014-7-30下午16:25:52
 */

public class LogUtil {
    private static boolean bLog = true;
    private static final String TAG = "sf_log";
    public static final String TAG_REQUEST = "sf_request";
    public static final String TAG_TEST = "sf_test";
    public static final String TAG_PUSH = "sf_push";
    public static final String TAG_ADD_SHOPCAR = "sf_add_shopcar";
    public static final String TAG_ORDERLIST = "sf_order_list";
    public static final String TAG_AUTO_LOGIN = "sf_auto_login";
    public static final String TAG_CUT_DOWN = "sf_cut_down";
    public static final String TAG_WEIXIN_LOGIN = "weixin_login";

    public static void d(String msg) {

        if (bLog) {

            Log.d(TAG, msg);

        }

    }

    public static void d(String Tag, String msg) {

        if (bLog) {

            Log.d(Tag, msg);

        }

    }

    public static void e(String Tag, String msg) {

        if (bLog) {

            Log.d(Tag, msg);

        }

    }

    public static void i(String msg) {

        if (bLog) {

            Log.i(TAG, msg);

        }

    }

    public static void e(String msg) {

        if (bLog) {

            Log.e(TAG, msg);

        }

    }

    public static void v(String msg) {

        if (bLog) {

            Log.v(TAG, msg);

        }

    }

    public static void w(String msg) {

        if (bLog) {

            Log.w(TAG, msg);

        }

    }

    public static void logRequestParam(Object object){
        try {
            Class<? extends Object> cls = object.getClass();
            java.lang.reflect.Field[] flds = cls.getFields();
            if ( flds != null )  
            {  
                for ( int i = 0; i < flds.length; i++ )  
                {  
                    d(TAG_REQUEST, flds[i].getName() + " = " + flds[i].get(object));  
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

}
