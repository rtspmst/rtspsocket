package com.fhc.laser_monitor_sw_android_rtsp_app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;

import com.fhc.laser_monitor_sw_android_rtsp_app.activity.MainActivity;

public class MyDrawerLayout extends DrawerLayout {

    public MyDrawerLayout(@NonNull Context context) {
        super(context);
    }

    public MyDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (MainActivity.isCloseDrawerLayout) {
            MainActivity.isCloseDrawerLayout = false;
            closeDrawers();
        }
        return false;
    }
}
