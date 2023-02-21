package com.fhc.laser_monitor_sw_android_rtsp_app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fhc.laser_monitor_sw_android_rtsp_app.activity.MainActivity;
import com.fhc.laser_monitor_sw_android_rtsp_app.client.UartClient6804;

import org.easydarwin.video.EasyPlayerClient;

/**
 * @ClassName: OtgWorkReceiver
 * 注册广播 监听USB插拔
 */
public class OtgWorkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        switch (NetUtil.isNetworkAvailable()) {

            case NetUtil.NET_NOCONNECT://无连接

                EasyPlayerClient.VIDEO_STATUS = false;
                UartClient6804.TRANSPARENT_MESSAGE = -22;

                MainActivity activity = (MainActivity) context;

                if (activity != null) {

                    activity.setButtonState(true);

                    //停止录像
                    activity.codecToggle(false);

                }


                break;

            case NetUtil.NET_ETHERNET://以太网

//                Log.e("TAG", "onReceive: ===========33333333333===============" );

                break;

            case NetUtil.NET_WIFI://WiFi

//                Log.e("TAG", "onReceive: ===========22222222222===============" );

                break;

            case NetUtil.NET_MOBILE://手机

//                Log.e("TAG", "onReceive: ==========1111111111================" );

                break;
            default:
                break;
        }
    }
}
