package com.fhc.laser_monitor_sw_android_rtsp_app.activity;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fhc.laser_monitor_sw_android_rtsp_app.utils.AdbUtil;

public class BaseActivity extends AppCompatActivity {

    public AdbUtil mAdbUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdbUtil = new AdbUtil();

//        横屏时禁止屏幕翻转 ==  android:screenOrientation="landscape">
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //如果版本大于等于29 把页面延伸到凹口区显示
        full_screen_display();
    }

    //如果版本大于等于29 把页面延伸到凹口区显示
    private void full_screen_display() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            //设置页面全屏显示
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

            //设置页面延伸到凹口区显示
            getWindow().setAttributes(lp);
            getWindow().getDecorView()
                    .findViewById(android.R.id.content)
                    .getRootView()
                    .setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                        @Override
                        public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                            DisplayCutout cutout = windowInsets.getDisplayCutout();
                            if (cutout == null) {
                                //通过cutout是否为null判断是否凹口手机
                                Log.e("TAG", "cutout==null, is not notch screen");

                            } else {

                            }
                            return windowInsets;
                        }
                    });
        }
    }
}
