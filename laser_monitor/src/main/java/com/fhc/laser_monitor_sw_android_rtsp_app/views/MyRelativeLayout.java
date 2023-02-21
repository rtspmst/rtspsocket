package com.fhc.laser_monitor_sw_android_rtsp_app.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;

import com.fhc.laser_monitor_sw_android_rtsp_app.R;

/**
 * 设置位置 布局内添加气泡
 */
public class MyRelativeLayout extends RelativeLayout {

    //气泡爆炸的bitmap数组
    private Bitmap[] mBurstBitmapsArray;

    //当前气泡爆炸图片index
    private int mCurDrawableIndex = 0;

    //可动气泡的圆心
    private PointF mBubMovableCenter;

    //爆炸绘制区域
    private Rect mBurstRect;

    //气泡颜色
    private int mBubbleColor;

    //气泡半径
    private float mBubbleRadius = -100;

    //气泡的画笔
    private Paint mBubblePaint;

    //可动气泡的半径 调节准星图片大小
    private float mBubMovableRadius = 65f;

    //设置是否响应点击事件
    public static boolean isAllowSetting = false;

    //动画是否结束 未结束直接消费点击事件
    public static boolean animationIsOver = false;

    //设置是否响应点击事件
    public void setAllowSetting(boolean setValue) {
        isAllowSetting = setValue;
    }

    public MyRelativeLayout(Context context) {
        this(context, null);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBurstRect = new Rect();
        mBurstBitmapsArray = new Bitmap[mBurstDrawablesArray.length];
        for (int i = 0; i < mBurstDrawablesArray.length; i++) {
            //将气泡爆炸的drawable转为bitmap
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mBurstDrawablesArray[i]);
            mBurstBitmapsArray[i] = bitmap;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.dragBubbleView, defStyleAttr, 0);
        mBubbleColor = array.getColor(R.styleable.dragBubbleView_bubble_color, Color.RED);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBubblePaint.setColor(mBubbleColor);
        mBubblePaint.setStyle(Paint.Style.FILL);

        //可动气泡圆心 默认那个
        if (mBubMovableCenter == null) {
            mBubMovableCenter = new PointF(w - mBubbleRadius, h - mBubbleRadius);
        } else {
            mBubMovableCenter.set(w - mBubbleRadius, h - mBubbleRadius);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //抗锯齿
        mBurstRect.set(
                (int) (mBubMovableCenter.x - mBubMovableRadius),
                (int) (mBubMovableCenter.y - mBubMovableRadius),
                (int) (mBubMovableCenter.x + mBubMovableRadius),
                (int) (mBubMovableCenter.y + mBubMovableRadius));

        if (mCurDrawableIndex < mBurstBitmapsArray.length) {
            canvas.drawBitmap(mBurstBitmapsArray[mCurDrawableIndex], null, mBurstRect, mBubblePaint);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isAllowSetting) {

            if (!animationIsOver) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        animationIsOver = true;
                        mBubMovableCenter.x = event.getX();
                        mBubMovableCenter.y = event.getY();

                        if (myListener != null && event != null) {
                            myListener.event_X_Y(event.getX(), event.getY());

                            //爆炸效果
                            startBubbleBurstAnim();
                        }

                        Log.e("TAG", "onTouchEvent: " + event.getX() + " ---- " + event.getY());

                        break;
                    default:
                        break;
                }
            } else {
                return true;
            }
        }
        return isAllowSetting;
    }

    //开始泡泡爆破动画
    private void startBubbleBurstAnim() {
        ValueAnimator anim = ValueAnimator.ofInt(0, mBurstDrawablesArray.length);
        anim.setDuration(8000);
        anim.setInterpolator(new LinearInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurDrawableIndex = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animationIsOver = false;
            }
        });
        anim.start();
    }

    //气泡爆炸的图片id数组
    private int[] mBurstDrawablesArray = {
            R.drawable.ic_dingwei6,
            R.drawable.ic_dingwei7,
            R.drawable.ic_dingwei8,
            R.drawable.ic_dingwei9,
            R.drawable.ic_dingwei10,
            R.drawable.ic_dingwei11,
            R.drawable.ic_dingwei6,
            R.drawable.ic_dingwei7,
            R.drawable.ic_dingwei8,
            R.drawable.ic_dingwei9,
            R.drawable.ic_dingwei10,
            R.drawable.ic_dingwei11};

    public interface CoordinateCallback {
        //接收数据
        void event_X_Y(float dataX, float dataY);
    }

    private CoordinateCallback myListener;

    public void setOnTouchEventListener(CoordinateCallback listener) {
        myListener = listener;
    }
}
