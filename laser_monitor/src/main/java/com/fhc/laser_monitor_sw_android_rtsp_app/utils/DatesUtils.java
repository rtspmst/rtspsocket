package com.fhc.laser_monitor_sw_android_rtsp_app.utils;

import android.util.Log;

import java.text.SimpleDateFormat;

public class DatesUtils {

    public static String cccccccc(String from) {

        //获取当前时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
        String format = simpleDateFormat.format(System.currentTimeMillis());
        Log.e("TAG", "cccccccc == " + from + " -- " + format + " -- " + Thread.currentThread().getName());
        return format + "  --  ";
    }
}
