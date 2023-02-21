package com.fhc.laser_monitor_sw_android_rtsp_app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BaseInterpolator;
import android.widget.FrameLayout;


/**
 * 旋转效果
 * 全局搜索这句话 你就会看到一共三个这样的话
 */
public class CubeLayout extends FrameLayout{

    private BaseInterpolator mInterpolator = new AccelerateDecelerateInterpolator();

    public CubeLayout(Context context) {
        this(context, null);
    }

    public CubeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CubeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View foregroundView = getChildAt(0);
        View backgroundView = getChildAt(1);

        CubeLeftOutAnimation cubeLeftOutAnimation = new CubeLeftOutAnimation();
        cubeLeftOutAnimation.setDuration(800);
        cubeLeftOutAnimation.setFillAfter(true);

        CubeRightInAnimation cubeRightInAnimation = new CubeRightInAnimation();
        cubeRightInAnimation.setDuration(800);
        cubeRightInAnimation.setFillAfter(true);

        foregroundView.startAnimation(cubeLeftOutAnimation);
        backgroundView.startAnimation(cubeRightInAnimation);
    }
}

