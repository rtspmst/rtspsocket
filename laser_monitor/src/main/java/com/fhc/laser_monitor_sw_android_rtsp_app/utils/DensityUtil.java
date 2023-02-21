package com.fhc.laser_monitor_sw_android_rtsp_app.utils;

import android.content.Context;

/**
 * Created by zane on 15-6-24.
 */
public class DensityUtil {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static String replaceAction(String userPSD) {

        StringBuilder str = new StringBuilder();

        for (int i = 0; i < userPSD.length(); i++) {
            str.append("*");
        }

        return str.toString();

//        String Number=mobile.substring(0,3)+”**“+mobile.substring(7,mobile.length( ));
//        return userPSD.replaceAll("(?<=\\d{0})\\d(?=\\d{0})", "*");
    }
}
