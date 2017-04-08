package com.zjy.police.trafficassist.utils;

import android.util.Log;

/**
 * com.zjy.trafficassist.utils
 * Created by 73958 on 2017/3/22.
 */

public class LogUtil {

    private static boolean LOGV = true;

    private static boolean LOGD = true;

    private static boolean LOGI = true;

    private static boolean LOGW = true;

    private static boolean LOGE = true;

    private static String TAG_FILTER = "HDU_";

    public static void v(String mess) {
        if (LOGV) { Log.v(TAG_FILTER + getTag(), mess); }
    }
    public static void d(String mess) {
        if (LOGD) { Log.d(TAG_FILTER + getTag(), mess); }
    }
    public static void i(String mess) {
        if (LOGI) { Log.i(TAG_FILTER + getTag(), mess); }
    }
    public static void w(String mess) {
        if (LOGW) { Log.w(TAG_FILTER + getTag(), mess); }
    }
    public static void e(String mess) {
        if (LOGE) { Log.e(TAG_FILTER + getTag(), mess); }
    }

    public static void v(String tag, String mess) {
        if (LOGV) { Log.v(TAG_FILTER + getTag() + tag, mess); }
    }
    public static void d(String tag, String mess) {
        if (LOGD) { Log.d(TAG_FILTER + getTag() + tag, mess); }
    }
    public static void i(String tag, String mess) {
        if (LOGI) { Log.i(TAG_FILTER + getTag() + tag, mess); }
    }
    public static void w(String tag, String mess) {
        if (LOGW) { Log.w(TAG_FILTER + getTag() + tag, mess); }
    }
    public static void e(String tag, String mess) {
        if (LOGE) { Log.e(TAG_FILTER + getTag() + tag, mess); }
    }

    private static String getTag() {
        StackTraceElement[] trace = new Throwable().fillInStackTrace()
                .getStackTrace();
        String callingClass = "";
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(LogUtil.class)) {
                callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass
                        .lastIndexOf('.') + 1);
                break;
            }
        }
        return callingClass + " ";
    }
}
