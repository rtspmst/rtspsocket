package com.fhc.laser_monitor_sw_android_rtsp_app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.fhc.laser_monitor_sw_android_rtsp_app.bean.MessageEventBean;

import org.greenrobot.eventbus.EventBus;

//home键监听
public class HomeKeyEventReceiver extends BroadcastReceiver {

    String SYSTEM_REASON = "reason";

    String SYSTEM_HOME_KEY = "homekey";

    String SYSTEM_HOME_KEY_LONG = "recentapps";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {

            String reason = intent.getStringExtra(SYSTEM_REASON);

            if (TextUtils.equals(reason, SYSTEM_HOME_KEY) || TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)) {

                EventBus.getDefault().post(new MessageEventBean("HOME"));

            }
        }
    }

}
