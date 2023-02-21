package com.fhc.laser_monitor_sw_android_rtsp_app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;

import static com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication.getAppContext;

/**
 * 检测网络连接状态
 */
public class NetUtil {
    public static final int NET_NOCONNECT = 0;//无连接
    public static final int NET_ETHERNET = 1;//以太网
    public static final int NET_WIFI = 2;//WiFi
    public static final int NET_MOBILE = 3;//手机

    public static int isNetworkAvailable() {
        ConnectivityManager connectMgr = (ConnectivityManager) getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ethNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (ethNetInfo != null && ethNetInfo.isConnected()) {
            return NET_ETHERNET;
        } else if (wifiNetInfo != null && wifiNetInfo.isConnected()) {
            return NET_WIFI;
        } else if (mobileNetInfo != null && mobileNetInfo.isConnected()) {
            return NET_MOBILE;
        } else {
            return NET_NOCONNECT;
        }
    }


    /**
     * 检测网络是否可用
     *
     * @return
     */
    public static boolean isNetworkConnected() {
        return isNetworkAvailable() != NET_NOCONNECT;
    }

    // 定义WifiManager对象
    private static final WifiManager mWifiManager;

    static {
        // 取得WifiManager对象
        mWifiManager = (WifiManager) MyApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

    }

    // 检查当前WIFI状态
    public static int checkState() {
        return mWifiManager.getWifiState();
    }


}
