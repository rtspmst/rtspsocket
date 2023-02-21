package com.fhc.laser_monitor_sw_android_rtsp_app.utils;

import static com.blankj.utilcode.util.ThreadUtils.runOnUiThread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;
import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.action.AudioDecoder;
import com.fhc.laser_monitor_sw_android_rtsp_app.action.JsonHandle6802;
import com.fhc.laser_monitor_sw_android_rtsp_app.activity.MainActivity;
import com.fhc.laser_monitor_sw_android_rtsp_app.client.UartClient6804;
import com.fhc.laser_monitor_sw_android_rtsp_app.fragment.PlayFragment;

/**
 * 一键自检弹窗
 */
public class DialogInspectionUtil {

    private final Context context;
    private TextView tvWired;
    private TextView tvWiFi;
    private int checkState;
    private TextView tvLightEcho;
    public AlertDialog dialog;
    private TextView tvAudio;
    private TextView tvRanging;
    private TextView tvSocket;
    private TextView tvVideo;
    private View view;
    private int green;
    private int logo_red;
    private int gray;
    private int blue_900;

    private TextView tv_wifi_name;
    private TextView tv_yibangding;
    private Button btn_BangDingWiFi;
    private final SharedPreferencesUtil spUtil;
    private WifiManager mWifiManager;
    private WifiInfo wifiInfo;
    private TextView tv_tx2;

    public DialogInspectionUtil(Context context) {

        this.context = context;

        green = ContextCompat.getColor(context, R.color.signal_status_view_bar_green);
        logo_red = ContextCompat.getColor(context, R.color.logo_red);
        gray = ContextCompat.getColor(context, R.color.gray);
        blue_900 = ContextCompat.getColor(context, R.color.grey_600);

        spUtil = new SharedPreferencesUtil();

        English_Chinese_switch();
    }

    //开始自检
    public void startSelfTest() {
        dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(getView())
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        closeAlertDialog();
                    }
                }).show();

        dialog.getWindow().setLayout(DensityUtil.dip2px(context, 500), DensityUtil.dip2px(context, 350));
    }

    public void closeAlertDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        xiaohui();
    }

    //弹窗
    private View getView() {

        view = LayoutInflater.from(context).inflate(R.layout.view_detect_layout, null);
        //有线
        tvWired = view.findViewById(R.id.tvWired);
        //WiFi
        tvWiFi = view.findViewById(R.id.tvWiFi);
        //视频
        tvVideo = view.findViewById(R.id.tvVideo);
        //Socket
        tvSocket = view.findViewById(R.id.tvSocket);
        //测距
        tvRanging = view.findViewById(R.id.tvRanging);
        //光回波
        tvLightEcho = view.findViewById(R.id.tvLightEcho);
        //音频
        tvAudio = view.findViewById(R.id.tvAudio);
        //WiFi名称
        tv_wifi_name = view.findViewById(R.id.tv_wifi_name);
        //已绑定WiFi名称
        tv_yibangding = view.findViewById(R.id.tv_yibangding);
        //绑定WiFi按钮
        btn_BangDingWiFi = view.findViewById(R.id.btn_BangDingWiFi);
        //tx2 准星坐标
        tv_tx2 = view.findViewById(R.id.tv_tx2);

        set_wifi_name();


        btn_BangDingWiFi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spUtil.saveWiFiName(wifiInfo.getSSID());

                set_wifi_name();

            }
        });

        recordBoot();

        view.findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return view;
    }

    public void set_wifi_name() {
        if (MainActivity.IS_ENGLISH) {
            tv_yibangding.setText("Please connect designated Wi-Fi : " + spUtil.getWiFiName());
        } else {
            tv_yibangding.setText("请连接已绑定设备WiFi : " + spUtil.getWiFiName());
        }
    }

    public void recordBoot() {

        MyToastUtils.isShow = false;

        // 取得WifiManager对象
        mWifiManager = (WifiManager) MyApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiInfo = mWifiManager.getConnectionInfo();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                self_test_loop();
            }
        });

    }

    public void xiaohui() {

        MyToastUtils.isShow = true;
    }


    @SuppressLint("SetTextI18n")
    private void self_test_loop() {
        //WiFi状态 3为连接 1为断开
        checkState = NetUtil.checkState();

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatusIntent = context.registerReceiver(null, ifilter);
        //如果设备正在充电，可以提取当前的充电状态和充电方式（无论是通过 USB 还是交流充电器），如下所示：

        // 我们在充电吗/ charged?
        int status = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        //充电状态判断
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
        int chargePlug = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        //USB连接状态判断
        if (isCharging) {
            if (usbCharge) {
                //手机正处于USB连接中1
                connectionUSB();
            } else if (acCharge) {
                //手机通过电源充电中
                tvWired.setText(CHARGING);
                notConnectedUSB();
            }
        } else {
            //手机未连接USB线
            tvWired.setText(USB_DISCONNECTED);
            notConnectedUSB();
        }

        //6804
        if (UartClient6804.TRANSPARENT_MESSAGE == 1) {
            //主控板连接成功
            tvSocket.setText(CONTROL_BOARD_CONNECTION_OK);
            tvSocket.setTextColor(green);
        } else {
            //主控板连接失败
            tvSocket.setText(CONTROL_BOARD_CONNECTION_FAILED);
            tvSocket.setTextColor(logo_red);
        }

        //TX2 6802
        if (JsonHandle6802.socketIsBroken) {
            //TX2连接失败
            tv_tx2.setText(TX2_CONNECTION_FAILED);
            tv_tx2.setTextColor(logo_red);
        } else {
            //TX2连接成功
            tv_tx2.setText(TX2_CONNECTION_OK);
            tv_tx2.setTextColor(green);
        }

        //6801
        if (AudioDecoder.AUDIO_CONNECT == 1) {
            tvAudio.setText(AUDIO_CONNECTION_OK);
            tvAudio.setTextColor(green);
        } else {
            tvAudio.setText(AUDIO_CONNECTION_FAILED);
            tvAudio.setTextColor(logo_red);
        }


        //视频
        if (PlayFragment.DETECT_VIDEO == 1) {
            //视频播放正常
            tvVideo.setText(VIDEO_PLAY_OK);
            tvVideo.setTextColor(green);
        } else {
            //视频播放失败
            tvVideo.setText(VIDEO_PLAY_FAILED);
            tvVideo.setTextColor(logo_red);
        }


        //光回波
        if (1.23f != MainActivity.huibo_power1) {
            //光回波正常
            tvLightEcho.setText(NORMAL_LIGHT_ECHO);
            tvLightEcho.setTextColor(green);
        } else {
            //光回波异常
            tvLightEcho.setText(ABNORMALLIGHT_ECHO);
            tvLightEcho.setTextColor(logo_red);
        }

        //测距机工作正常
        switch (MainActivity.DISTANCE_VALUE) {
            case 0:
                //测距仪连接失败
                tvRanging.setText(RANGEFINDER_CONNECTION_FAILED);
                tvRanging.setTextColor(logo_red);
                break;
            case 1:
                //测距仪已连接，无数据
                tvRanging.setText(RANGEFINDER_CONNECTED_NO_DATA);
                tvRanging.setTextColor(gray);
                break;
            case 2:
                //测距仪正常工作
                tvRanging.setText(RANGEFINDER_OPERATING_NORMALLY);
                tvRanging.setTextColor(green);
                break;
            default:
                break;
        }


    }


    //手机未连接数据线或者手机正在充电中
    private void notConnectedUSB() {

        tvWired.setTextColor(gray);

        switch (checkState) {
            case 3:

                //无线连接已打开
                tvWiFi.setText(OPENED);

                tvWiFi.setTextColor(green);
                String wiFiName = spUtil.getWiFiName();
                if (wifiInfo != null) {

                    //已连接 :
                    tv_wifi_name.setText(CONNECTED + wifiInfo.getSSID());

                    tv_wifi_name.setTextColor(blue_900);
                    tv_wifi_name.setVisibility(View.VISIBLE);

//                    if (wifiInfo.getSSID().equals(wiFiName)) {
//
//                        if (MainActivity.IS_ENGLISH) {
//                            tv_wifi_name.setText("Connected : " + wifiInfo.getSSID());
//                        } else {
//                            tv_wifi_name.setText("已连接 : " + wifiInfo.getSSID());
//                        }
//
//                        tv_wifi_name.setTextColor(green);
//                        tv_wifi_name.setVisibility(View.VISIBLE);
//                    } else {
//                        if (MainActivity.IS_ENGLISH) {
//                            tv_wifi_name.setText("Now : " + wifiInfo.getSSID() + " is not sharing the same Wi-Fi");
//                        } else {
//                            tv_wifi_name.setText("当前 : " + wifiInfo.getSSID() + "和绑定的WiFi不是同一个");
//                        }
//
//                        tv_wifi_name.setTextColor(logo_red);
//                        tv_wifi_name.setVisibility(View.VISIBLE);
//                    }
                }
                break;
            case 1:

                //请连接WiFi或者USB连接设备
                tvWiFi.setText(CONNECT_WIFI_OR_USB);
                //当前WiFi  已关闭
                tv_wifi_name.setText(CURRENT_WIFI + wifiInfo.getSSID() + CLOSED);

                tvWiFi.setTextColor(logo_red);
                tv_wifi_name.setTextColor(blue_900);
                break;
            default:
                break;
        }
    }

    //手机正处于USB连接中1
    private void connectionUSB() {

        //USB已连接
        tvWired.setText(USB_CONNECTED);

        tvWired.setTextColor(green);

        switch (checkState) {
            case 3:

                tv_wifi_name.setVisibility(View.VISIBLE);
                tv_wifi_name.setTextColor(blue_900);
                tvWiFi.setTextColor(logo_red);
                //当前WIFI
                tv_wifi_name.setText(CURRENT_WIFI + wifiInfo.getSSID());
                //已连接USB 请关闭WIFI
                tvWiFi.setText(TURN_OFF_WIFI);

                break;
            case 1:

                tv_wifi_name.setText(CURRENT_WIFI + wifiInfo.getSSID());
                tvWiFi.setText(WIFI_CONNECTION_CLOSED);

                tv_wifi_name.setVisibility(View.VISIBLE);
                tvWiFi.setTextColor(green);
                tv_wifi_name.setTextColor(blue_900);
                break;
            default:
                break;
        }
    }

    private static String CHARGING = "充电中 ...";
    private static String USB_DISCONNECTED = "USB 断开";
    //主控板连接成功
    private static String CONTROL_BOARD_CONNECTION_OK = "主控板连接成功 , 6804";
    //主控板连接失败
    private static String CONTROL_BOARD_CONNECTION_FAILED = "主控板连接失败 , 6804";
    //tx2连接成功
    private static String TX2_CONNECTION_OK = "TX2连接成功 , 6802";
    //tx2连接失败
    private static String TX2_CONNECTION_FAILED = "TX2连接失败 , 6802";

    private static String AUDIO_CONNECTION_OK = "音频连接正常 , 6801";
    private static String AUDIO_CONNECTION_FAILED = "音频连接失败 , 6801";

    private static String VIDEO_PLAY_OK = "视频播放正常";
    private static String VIDEO_PLAY_FAILED = "视频播放失败";
    private static String NORMAL_LIGHT_ECHO = "光回波正常";
    private static String ABNORMALLIGHT_ECHO = "光回波异常";
    private static String RANGEFINDER_CONNECTION_FAILED = "测距仪连接失败";
    private static String RANGEFINDER_CONNECTED_NO_DATA = "测距仪已连接，无数据";
    private static String RANGEFINDER_OPERATING_NORMALLY = "测距仪正常工作";
    private static String OPENED = "无线连接已打开";
    private static String CONNECTED = "已连接 : ";
    private static String CONNECT_WIFI_OR_USB = "请连接WiFi或者USB连接设备";
    private static String CURRENT_WIFI = "当前WiFi : ";
    private static String CLOSED = " 已关闭";
    private static String USB_CONNECTED = "USB已连接";
    private static String TURN_OFF_WIFI = "已连接USB,请关闭WiFi";
    private static String WIFI_CONNECTION_CLOSED = "已关闭WiFi连接";

    private void English_Chinese_switch() {

        switch (Language.anInt) {
            case 0:
                break;
            case 1:
                //充电中
                CHARGING = "mengisi daya ...";
                //usb断开
                USB_DISCONNECTED = "USB terputus ...";

                //主控板连接成功
                CONTROL_BOARD_CONNECTION_OK = "Koneksi papan kontrol utama berhasil,6804";
                //主控板连接失败
                CONTROL_BOARD_CONNECTION_FAILED = "Koneksi papan kontrol utama gagal,6804";

                //TX2连接成功
                TX2_CONNECTION_OK = "Koneksi TX2 berhasil,6802";
                //TX2连接失败
                TX2_CONNECTION_FAILED = "Koneksi TX2 gagal,6802";

                //音频连接正常
                AUDIO_CONNECTION_OK = "Koneksi audio berhasil,6801";
                //音频连接失败
                AUDIO_CONNECTION_FAILED = "Koneksi audio gagal,6801";

                //视频播放正常
                VIDEO_PLAY_OK = "Pemutaran video normal";
                //视频播放失败
                VIDEO_PLAY_FAILED = "Pemutaran video gagal";

                //光回波正常
                NORMAL_LIGHT_ECHO = "Gema optik normal";
                //光回波异常
                VIDEO_PLAY_FAILED = "Anomali gema optik";

                //测距仪连接失败
                RANGEFINDER_CONNECTION_FAILED = "Pengukur jarak tidak terhubung gagal";
                //测距仪已连接，无数据
                RANGEFINDER_CONNECTED_NO_DATA = "Pengukur jarak terhubung, tidak ada data";
                //测距仪正常工作
                RANGEFINDER_OPERATING_NORMALLY = "Pengukur jarak bekerja secara normal";

                //无线连接已打开
                OPENED = "Koneksi nirkabel terbuka";
                //已连接 :
                CONNECTED = "WiFi saat ini: ";
                //请连接WiFi或者USB连接设备
                CONNECT_WIFI_OR_USB = "Silakan sambungkan WiFi atau perangkat koneksi USB";
                //当前WiFi
                CURRENT_WIFI = "WiFi saat ini: ";
                //已关闭
                CLOSED = " off";
                //USB已连接
                USB_CONNECTED = "Kabel USB terhubung";
                //已连接USB,请关闭WiFi
                TURN_OFF_WIFI = "Tolong tutup WiFi";
                //已关闭WiFi连接
                WIFI_CONNECTION_CLOSED = "Koneksi WiFi ditutup";

                break;
            case 2:
                //充电中
                CHARGING = "charging ...";
                //usb断开
                USB_DISCONNECTED = "USB disconnected ...";

                //主控板连接成功
                CONTROL_BOARD_CONNECTION_OK = "Control board connection OK , 6804";
                //主控板连接失败
                CONTROL_BOARD_CONNECTION_FAILED = "Control board connection failed , 6804";

                //TX2连接成功
                TX2_CONNECTION_OK = "TX2 connection OK , 6802";
                //TX2连接失败
                TX2_CONNECTION_FAILED = "TX2 connection failed , 6802";

                //音频连接正常
                AUDIO_CONNECTION_OK = "Audio connection OK , 6801";
                //音频连接失败
                AUDIO_CONNECTION_FAILED = "Audio connection failed , 6801";

                //视频播放正常
                VIDEO_PLAY_OK = "Video play OK";
                //视频播放失败
                VIDEO_PLAY_FAILED = "Video play failed";

                //光回波正常
                NORMAL_LIGHT_ECHO = "Normal light echo";
                //光回波异常
                VIDEO_PLAY_FAILED = "Abnormallight echo";

                //测距仪连接失败
                RANGEFINDER_CONNECTION_FAILED = "Rangefinder connection failed";
                //测距仪已连接，无数据
                RANGEFINDER_CONNECTED_NO_DATA = "Rangefinder connected, no data";
                //测距仪正常工作
                RANGEFINDER_OPERATING_NORMALLY = "Rangefinder operating normally";
                //无线连接已打开
                OPENED = "Wi-Fi is on";
                //已连接 :
                CONNECTED = "Current Wi-Fi : ";
                //请连接WiFi或者USB连接设备
                CONNECT_WIFI_OR_USB = "Please connect Wi-Fi or USB device";
                //当前WiFi
                CURRENT_WIFI = "current Wi-Fi : ";
                //已关闭
                CLOSED = " off";
                //USB已连接
                USB_CONNECTED = "USB is connected";
                //已连接USB,请关闭WiFi
                TURN_OFF_WIFI = "Please turn off Wi Fi";
                //已关闭WiFi连接
                WIFI_CONNECTION_CLOSED = "Wi-Fi is off";
                break;
        }
    }

}
