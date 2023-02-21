package com.fhc.laser_monitor_sw_android_rtsp_app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.activity.MainActivity;

import static com.blankj.utilcode.util.ThreadUtils.runOnUiThread;

//检测有线连接弹窗
public class DialogWiredNETUtil {

    public DialogWiredNETUtil(Context context) {
        this.context = context;
        green = ContextCompat.getColor(context, R.color.signal_status_view_bar_green);
        logo_red = ContextCompat.getColor(context, R.color.logo_red);
        gray = ContextCompat.getColor(context, R.color.gray);
    }

    public AlertDialog dialog;

    private Context context;
    private TextView tvWired;
    private TextView tvWiFi;
    private TextView tvLightEcho;

    private View view;
    private int green;
    private int logo_red;
    private int gray;

    //开始自检
    public void startSelfTest() {

        if (context != null) {

            dialog = new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setView(getView())
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            closeAlertDialog();
                        }
                    }).show();
            dialog.getWindow().setLayout(DensityUtil.dip2px(context, 600), LinearLayout.LayoutParams.WRAP_CONTENT);

        }
    }

    private View getView() {

        view = LayoutInflater.from(context).inflate(R.layout.view_net_layout, null);
        //等待转接卡连接
        tvWired = view.findViewById(R.id.tvWired);
        //请关闭WiFi
        tvWiFi = view.findViewById(R.id.tvWiFi);
        //请关闭移动网络
        tvLightEcho = view.findViewById(R.id.tvLightEcho);

        //循环检查
        checkLoop();

        view.findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeAlertDialog();
            }
        });

        Button ivRebootApp = view.findViewById(R.id.ivRebootApp);
        ivRebootApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context != null) {
                    restartApplication(context);
                }
            }
        });

        if (MainActivity.IS_ENGLISH) {
            ivRebootApp.setText("Reboot");

        } else {
            ivRebootApp.setText("重启");
        }

        return view;
    }

    //重启app
    public void restartApplication(Context context) {
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        System.exit(0);
    }

    //循环检查
    public void checkLoop() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                networkDetectionLoop();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void networkDetectionLoop() {

        tvLightEcho.setTextColor(gray);

        if (MainActivity.IS_ENGLISH) {
            tvLightEcho.setText("Please turn off Wi Fi");
            tvWired.setText("Waiting for the adapter to link");
        } else {
            tvLightEcho.setText("请关闭WiFi");
            tvWired.setText("等待转接卡链接");
        }

        tvWired.setTextColor(logo_red);

        //WiFi状态 3为连接 1为断开
        switch (NetUtil.checkState()) {
            case 3:
                if (MainActivity.IS_ENGLISH) {
                    tvWiFi.setText("Please turn off Wi Fi");
                } else {
                    tvWiFi.setText("请关闭WiFi");
                }

                tvWiFi.setTextColor(logo_red);
                break;
            case 1:
                if (MainActivity.IS_ENGLISH) {
                    tvWiFi.setText("Wi Fi is off");
                    tvLightEcho.setText("Mobile network is off");
                } else {
                    tvWiFi.setText("WiFi已关闭");
                    tvLightEcho.setText("移动网络已关闭");
                }

                tvWiFi.setTextColor(green);

                tvLightEcho.setTextColor(green);

                int networkAvailable = NetUtil.isNetworkAvailable();
                switch (networkAvailable) {
                    case NetUtil.NET_ETHERNET:
                        //手机正处于USB连接1
                        if (MainActivity.IS_ENGLISH) {
                            tvWired.setText("Riser card is linked");
                        } else {
                            tvWired.setText("转接卡已链接");
                        }

                        tvWired.setTextColor(green);
                        break;

                    case NetUtil.NET_MOBILE:
                        if (MainActivity.IS_ENGLISH) {
                            tvLightEcho.setText("Please turn off mobile network");
                        } else {
                            tvLightEcho.setText("请关闭移动网络");
                        }
                        tvLightEcho.setTextColor(logo_red);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    public void closeAlertDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
