package com.fhc.laser_monitor_sw_android_rtsp_app.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.DensityUtil;

import java.lang.ref.WeakReference;

/**
 * @ClassName: SignalStatusView
 * @Description:光回波信号状态条
 * @Author: Lix
 * @CreateDate: 2020/5/27
 * @Version: 1.0
 */
public class SignalStatusView extends View {
    private static final String TAG = "SignalStatusView";
    public static final int RUN = 0x01;
    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 是否开启动画，默认开启
     */
    private boolean isRunning = true;
    /**
     * 这里定义了最大值和最小值
     * 当最大的时候颜色是绿色
     * 当最小或者在中等以下的时候颜色是红色
     */
    private int mProcessMax;
    private int mProcessMin;
    /**
     * 控件宽和高，用于计算能容纳多少个
     */
    private float mWidth;
    private float mHeight;
    /**
     * 每条的高度
     */
    private float mProcessHeight;
    /**
     * 每条的宽度
     */
    private float mProcessWidth;
    /**
     * 每条之间的间距
     */
    private float mProcessMargin;
    /**
     * 画笔
     */
    private Paint mPaint;
    private Paint mDefaultPaint;
    /**
     * 背景颜色
     */
    private int mBackGroundColor;
    /**
     * 进度条颜色
     */
    private int mProcessBarColor;
    private int mProcessBarWarningColor;
    /**
     * 默认进度条颜色
     */
    private int mDefaultProcessBarColor;
    private int mDefaultProcessBarWarningColor;
    /**
     * 进度
     */
    private float mProcess = 0.0f;
    /**
     * 时长
     */
    private long mShowProgressTime;

    private MyHandler mHandler;
    private int directionInt;

    public SignalStatusView(Context context) {
        this(context, null);
    }

    public SignalStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(attrs);
        init();
        mHandler = new MyHandler(this);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.SignalStatusView);
        mProcessHeight = typedArray.getFloat(R.styleable.SignalStatusView_mProcessHeight, DensityUtil.dip2px(mContext, 3f));
        mProcessWidth = typedArray.getFloat(R.styleable.SignalStatusView_mProcessWidth, DensityUtil.dip2px(mContext, 3f));
        mProcessMargin = typedArray.getFloat(R.styleable.SignalStatusView_mProcessMargin, DensityUtil.dip2px(mContext, 2f));
        mShowProgressTime = typedArray.getInteger(R.styleable.SignalStatusView_mShowProgressTime, 100);
        mBackGroundColor = typedArray.getColor(R.styleable.SignalStatusView_mBackGroundColor, ContextCompat.getColor(mContext, R.color.transparent));
        directionInt = typedArray.getInteger(R.styleable.SignalStatusView_mProcessDirection, 0);
        mProcessBarColor = typedArray.getColor(R.styleable.SignalStatusView_mProcessBarColor, ContextCompat.getColor(mContext, R.color.signal_status_view_bar_green));
        mProcessBarWarningColor = typedArray.getColor(R.styleable.SignalStatusView_mProcessBarColor, ContextCompat.getColor(mContext, R.color.signal_status_view_bar_warning));
        mDefaultProcessBarColor = typedArray.getColor(R.styleable.SignalStatusView_mProcessBarColor, ContextCompat.getColor(mContext, R.color.signal_status_view_default_bar_color));
        mDefaultProcessBarWarningColor = typedArray.getColor(R.styleable.SignalStatusView_mProcessBarColor, ContextCompat.getColor(mContext, R.color.signal_status_view_default_bar_warning_color));
        typedArray.recycle();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth((float) 3.0);
        mPaint.setColor(mProcessBarColor);
        mPaint.setStyle(Paint.Style.FILL);
        mDefaultPaint = new Paint();
        mDefaultPaint.setAntiAlias(true);
        mDefaultPaint.setColor(mDefaultProcessBarColor);
        mDefaultPaint.setStrokeWidth((float) 3.0);
        mDefaultPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(mBackGroundColor);
        if (directionInt == direction.vertical.directionInt) {
            //方向垂直
            if (mProcess <= 0) {
                mPaint.setColor(mProcessBarWarningColor);
                mDefaultPaint.setColor(mDefaultProcessBarWarningColor);
            } else {
                if (onRangeValue(mProcess)) {
                    mPaint.setColor(mProcessBarWarningColor);
                    mDefaultPaint.setColor(mDefaultProcessBarWarningColor);
                } else {
                    mPaint.setColor(mProcessBarColor);
                    mDefaultPaint.setColor(mDefaultProcessBarColor);
                }
            }
            //过渡效果
//            for (int i = 0; i < mProcessMax; i++) {
//                int height = (int) (mHeight - mProcessHeight * i);
//                int bottom = (int) (height + mProcessHeight - mProcessMargin);
//                onDrawDefaultVerticalProcess(canvas, height, bottom);
//            }
            onDrawVerticalProcess(canvas);
        } else if (directionInt == direction.horizonta.directionInt) {
            //方向水平
            if (mProcess <= 0) {
                mPaint.setColor(mProcessBarWarningColor);
                mDefaultPaint.setColor(mDefaultProcessBarWarningColor);
            } else {
                if (onRangeValue(mProcess)) {
                    mPaint.setColor(mProcessBarWarningColor);
                    mDefaultPaint.setColor(mDefaultProcessBarWarningColor);
                } else {
                    mPaint.setColor(mProcessBarColor);
                    mDefaultPaint.setColor(mDefaultProcessBarColor);
                }
            }
            //过渡效果
//            for (int i = 0; i < mProcessMax; i++) {
//                int left = (int) (mWidth - mProcessWidth * i);
//                int right = (int) (left + mProcessWidth - mProcessMargin);
//                onDrawDefaultHorizontaProcess(canvas, left, right);
//            }
            onDrawHorizontaProcess(canvas);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getProperSize(widthMeasureSpec);
        mHeight = getProperSize(heightMeasureSpec);
        setMeasuredDimension((int) mWidth, (int) mHeight);
        if (directionInt == direction.vertical.directionInt) {
            mProcessMax = (int) (mHeight / mProcessHeight);
        } else if (directionInt == direction.horizonta.directionInt) {
            mProcessMax = (int) (mWidth / mProcessWidth);
        }
        mProcessMin = mProcessMax / 4;
    }

    /**
     * 计算默认大小
     *
     * @param measureSpec
     * @return
     */
    private int getProperSize(int measureSpec) {
        int properSize = 0;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        /**
         * 默认大小
         */
        int defaultSize = 100;
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
                properSize = defaultSize;
                break;
            case MeasureSpec.EXACTLY:
            case MeasureSpec.AT_MOST:
                properSize = size;
                break;
            default:
                break;
        }
        return properSize;
    }

    /**
     * 绘制默认水平
     *
     * @param canvas
     * @param left
     * @param right
     */
    private void onDrawDefaultHorizontaProcess(Canvas canvas, float left, float right) {
        float top = 10;
        float bottom = mHeight - 10;
        RectF rect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rect, 2, 2, mDefaultPaint);
    }

    /**
     * 绘制默认 垂直
     *
     * @param canvas
     * @param top
     * @param bottom
     */
    private void onDrawDefaultVerticalProcess(Canvas canvas, float top, float bottom) {
        float left = 10;
        float right = mWidth - 10;
        RectF rect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rect, 2, 2, mDefaultPaint);
    }

    private void onDrawHorizontaProcess(Canvas canvas) {
        float top = 10;
        float bottom = mHeight - 10;
        float process = mProcess;
        for (int i = 0; i < mProcessMax; i++) {
            if (process > mWidth) {
                process = mWidth;
                int left = (int) (process - mProcessWidth * i);
                int right = (int) (left + mProcessWidth - mProcessMargin);
                RectF rect = new RectF(left, top, right, bottom);
                canvas.drawRoundRect(rect, 2, 2, mPaint);
            } else if (process < 0 || process == 0) {
                process = mWidth;
                int left = (int) (process - mProcessWidth);
                int right = (int) (left + mProcessWidth - mProcessMargin);
                RectF rect = new RectF(left, top, right, bottom);
                canvas.drawRoundRect(rect, 2, 2, mPaint);
            } else {
                int left = (int) (mWidth - mProcessWidth * i);
                int right = (int) (left + mProcessWidth - mProcessMargin);
                if (left >= (mWidth - process)) {
                    RectF rect = new RectF(left, top, right, bottom);
                    canvas.drawRoundRect(rect, 2, 2, mPaint);
                }
            }
        }
    }

    private void onDrawVerticalProcess(Canvas canvas) {
        float right = mWidth - 10;
        float left = 10;
        float process = mProcess;
        for (int i = 0; i < mProcessMax; i++) {
            if (process > mHeight) {
                process = mHeight;
                int height = (int) (process - mProcessHeight * i);
                int bottom = (int) (height + mProcessHeight - mProcessMargin);
                RectF rect = new RectF(left, height, right, bottom);
                canvas.drawRoundRect(rect, 2, 2, mPaint);
            } else if (process < 0 || process == 0) {
                process = mHeight;
                int height = (int) (process - mProcessHeight);
                int bottom = (int) (height + mProcessHeight - mProcessMargin);
                RectF rect = new RectF(left, height, right, bottom);
                canvas.drawRoundRect(rect, 2, 2, mPaint);
            } else {
                int height = (int) (mHeight - mProcessHeight * i);
                int bottom = (int) (height + mProcessHeight - mProcessMargin);
                if (height >= (mHeight - process)) {
                    RectF rect = new RectF(left, height, right, bottom);
                    canvas.drawRoundRect(rect, 2, 2, mPaint);
                }
            }
        }
    }

    /**
     * 计算数据量大小
     */
    private boolean onRangeValue(float process) {
        float min = mProcessMin;
        float minRange = directionInt == direction.vertical.directionInt ? (int) (process / mProcessHeight) : (int) (process / mProcessWidth);
        boolean bool = false;
        switch (Float.compare(min, minRange)) {
            case -1:
            case 0:
                bool = false;
                break;
            case 1:
                bool = true;
                break;
            default:
                break;
        }
        return bool;
    }

    /**
     * 动画增长
     */
//    Runnable runnable = new Runnable() {
//
//        @Override
//        public void run() {
//            while (isRunning) {
//                try {
//                    Message msg = new Message();
//                    msg.what = RUN;
//                    msg.obj = mProcess;
//                    myHandler.sendMessage(msg);
//                    Thread.sleep(mShowProgressTime);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    };

    public static class MyHandler extends Handler {

        //WeakReference垃圾回收机制
        private WeakReference<SignalStatusView> weakRef;

        public MyHandler(SignalStatusView view) {
            weakRef = new WeakReference(view);
        }

        @Override
        public void handleMessage(Message msg) {
            final SignalStatusView signalStatusView = weakRef.get();
            if (signalStatusView != null) {
                if (msg.what == RUN) {
//                    signalStatusView.setmProcess((Float) msg.obj);
                    signalStatusView.postInvalidate();
                }
            }
            super.handleMessage(msg);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public void setmProcessHeight(float mProcessHeight) {
        if (directionInt == direction.vertical.directionInt) {
            this.mProcessHeight = DensityUtil.dip2px(mContext, mProcessHeight);
        } else if (directionInt == direction.horizonta.directionInt) {
            this.mProcessWidth = DensityUtil.dip2px(mContext, mProcessHeight);
        }
    }

    public void setmBackGroundColor(int mBackGroundColor) {
        this.mBackGroundColor = mBackGroundColor;
    }

    public void setmProcessBarColor(int mProcessBarColor) {
        this.mProcessBarColor = mProcessBarColor;
    }

    public float getmProcess() {
        return mProcess;
    }

    public void setmProcess(float mProcess) {
        float scale = 0f;
        if (directionInt == direction.vertical.directionInt) {
            scale = mHeight / 100;
        } else if (directionInt == direction.horizonta.directionInt) {
            scale = mWidth / 100;
        }

        this.mProcess = mProcess * scale;
        Message msg;
        if (mHandler != null) {
            msg = mHandler.obtainMessage(RUN);
        } else {
            msg = new Message();
            msg.what = RUN;
        }
        msg.sendToTarget();
    }

    public void startAnimation(float mProcess) {
        this.mProcess = mProcess;
        Message msg;
        if (mHandler != null) {
            msg = mHandler.obtainMessage(RUN);
        } else {
            msg = new Message();
            msg.what = RUN;
        }
        msg.sendToTarget();
    }

    public float getmHeight() {
        return mHeight;
    }

    public void start() {
        isRunning = true;
    }

    public void stop() {
        isRunning = false;
    }

    public void setmShowProgressTime(long mShowProgressTime) {
        this.mShowProgressTime = mShowProgressTime;
    }

    /**
     * 方向
     */
    private enum direction {
        vertical(0),
        horizonta(1);

        direction(int directionInt) {
            this.directionInt = directionInt;
        }

        int directionInt;
    }

}