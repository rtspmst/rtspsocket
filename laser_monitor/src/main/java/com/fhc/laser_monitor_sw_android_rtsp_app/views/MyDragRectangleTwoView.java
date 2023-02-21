package com.fhc.laser_monitor_sw_android_rtsp_app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.fhc.laser_monitor_sw_android_rtsp_app.R;

/**
 * 可拖拽矩形
 * 第二种
 * 点击屏幕开始绘制矩形
 * 起始点固定 其他三个点可调整
 * 再次点击屏幕 重新绘制新的图形
 */
public class MyDragRectangleTwoView extends View {

    private Context mContext;

    private Paint mRectPaint;
    private Paint mCornerPaint;
    private Paint mTextPaint;
    private Paint mMappingLinePaint;

    private int width;
    private int height;
    private float currentX;
    private float currentY;

    /*矩形操作状态  0-不动 1-拖动 2-边角缩放 3-边框缩放*/
    private int mOperatingStatus = 0;

    private int mCornerStatus;//记录点击的哪个角
    private float eventX;
    private float eventY;

    public MyDragRectangleTwoView(Context context) {
        super(context);
        this.mContext = context;
        initPaint();
    }

    public MyDragRectangleTwoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*绘制十字线*/
        canvas.drawLine(startX, startY + (endY - startY) / 2, endX, startY + (endY - startY) / 2, mMappingLinePaint);
        canvas.drawLine(startX + (endX - startX) / 2, startY, startX + (endX - startX) / 2, startY + (endY - startY), mMappingLinePaint);

        /*绘制边框*/
        canvas.drawRect(startX, startY, endX, endY, mRectPaint);

        canvas.drawText("X 轴起点：" + (int) startX + "        Y 轴起点：" + (int) startY,
                startX,
                startY - 20,
                mTextPaint);
        canvas.drawText("X 轴终点：" + (int) endX + "        Y 轴终点：" + (int) endY,
                startX,
                endY + 35,
                mTextPaint);
    }

    private float startX;//X轴起点 starting point
    private float endX;//X轴终点 end
    private float startY;//Y轴起点
    private float endY;//Y轴终点
    private float temp_move_start_x;//临时移动点X
    private float temp_move_end_x;//临时移动点X
    private float temp_move_start_y;//临时移动点X
    private float temp_move_end_y;//临时移动点X
    private boolean isSlide_start_X;//标记是否可滑动
    private boolean isSlide_end_X;//标记是否可滑动
    private boolean isSlide_start_Y;//标记是否可滑动
    private boolean isSlide_end_Y;//标记是否可滑动
    private boolean outOfBounds;//标记是否越界

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        /*获取控件宽高*/
        width = getWidth();
        height = getHeight();
//        startX = width / 4;
//        endX = width / 4 * 3;
//        startY = height / 4;
//        endY = height / 4 * 3;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                currentX = event.getX();
                currentY = event.getY();

                if (toolPointIsInRect(event.getX(), event.getY())) {
                    //点击位置在矩形内
                    mOperatingStatus = 1;

                } else {
                    //点击位置在矩形外
                    mOperatingStatus = 2;
                    startX = event.getX();
                    startY = event.getY();

//                    toolPointIsInBorderline(event.getX(), event.getY());
                }

                break;
            case MotionEvent.ACTION_MOVE:

                eventX = event.getX();
                eventY = event.getY();

                switch (mOperatingStatus) {
                    case 1://点击位置在矩形内

                        temp_move_start_x = startX + eventX - currentX;
                        if (temp_move_start_x > 5) {
                            isSlide_start_X = true;
                        } else {
                            isSlide_start_X = false;
                        }

                        temp_move_end_x = endX + eventX - currentX;
                        if (temp_move_end_x < width - 5) {
                            isSlide_end_X = true;
                        } else {
                            isSlide_end_X = false;
                        }

                        temp_move_start_y = startY + eventY - currentY;
                        if (temp_move_start_y > 5) {
                            isSlide_start_Y = true;
                        } else {
                            isSlide_start_Y = false;
                        }

                        temp_move_end_y = endY + eventY - currentY;
                        if (temp_move_end_y < height - 5) {
                            isSlide_end_Y = true;
                        } else {
                            isSlide_end_Y = false;
                        }

                        if (isSlide_start_X && isSlide_end_X && isSlide_start_Y && isSlide_end_Y) {
                            startX = temp_move_start_x;
                            endX = temp_move_end_x;

                            startY = temp_move_start_y;
                            endY = temp_move_end_y;
                            /**重绘*/invalidate();
                        }

                        break;
                    case 2://点击位置在矩形外

                        endX = event.getX();
                        endY = event.getY();

                        /**重绘*/invalidate();
                        break;
                    default:
                        break;
                }

                currentX = eventX;
                currentY = eventY;

                break;
            case MotionEvent.ACTION_UP:
                mOperatingStatus = 0;

                myInvalidate(false);

                break;
            default:
                break;
        }
        return true;
    }

    public void myInvalidate(boolean is_invalidate) {

        if (is_invalidate) {

            //如果是从外部发来的指令
            redraw();

        } else {

            //如果是从内部点击了屏幕
            if (startX == currentX && startY == currentY) {

                redraw();
            }
        }
    }

    private void redraw() {
        startX = 0;
        endX = 0;
        startY = 0;
        endY = 0;
        /**重绘*/invalidate();
    }

    /**
     * 获取坐标点 数组
     *
     * @return
     */
    public int[] getCoordinate() {
        if (startX > endX) {
            float temp = startX;
            startX = endX;
            endX = temp;
        }

        if (startY > endY) {
            float temp = startY;
            startY = endY;
            endY = temp;
        }
        int[] arrayCoordinate = {(int) startX, (int) endX, (int) startY, (int) endY};
        return arrayCoordinate;
    }

    /**
     * 判断按下的点是否在矩形内
     */
    private boolean toolPointIsInRect(float x, float y) {

        if (startX > endX) {
            float temp = startX;
            startX = endX;
            endX = temp;
        }

        if (startY > endY) {
            float temp = startY;
            startY = endY;
            endY = temp;
        }

        if (x > startX && x < endX && y > startY && y < endY) {
            return true;
        }
        return false;
    }

    /**
     * 判断按下的点是否在边框线范围内
     */
    private boolean toolPointIsInBorderline(float x, float y) {
        if (x < startX + (endX - startX) / 2) {
            //点击的矩形左侧

            if (y < startY + (endY - startY) / 2) {
                //左上角
                mCornerStatus = 0;
            } else {
                //左下角
                mCornerStatus = 1;
            }

        } else {
            //点击的是矩形右侧
            if (y < startY + (endY - startY) / 2) {
                //右上角
                mCornerStatus = 2;
            } else {
                //右下角
                mCornerStatus = 3;
            }
        }
        return false;
    }

    private void initPaint() {
        /*边框画笔*/
        /**初始化*/mRectPaint = new Paint();
        /**设置画笔颜色*/mRectPaint.setColor(ContextCompat.getColor(mContext, R.color.logo_red));
        /**设置画笔样式*/mRectPaint.setStyle(Paint.Style.STROKE);
        /**设置画笔粗细*/mRectPaint.setStrokeWidth(6);
        /**使用抗锯齿*/mRectPaint.setAntiAlias(true);
        /**使用防抖动*/mRectPaint.setDither(true);
        /**设置笔触样式-圆*/mRectPaint.setStrokeCap(Paint.Cap.ROUND);
        /**设置结合处为圆弧*/mRectPaint.setStrokeJoin(Paint.Join.ROUND);

        /*边角画笔*/
        /**初始化*/mCornerPaint = new Paint();
        /**设置画笔颜色*/mCornerPaint.setColor(ContextCompat.getColor(mContext, R.color.white));
        /**设置画笔样式*/mCornerPaint.setStyle(Paint.Style.FILL);
        /**设置画笔粗细*/mCornerPaint.setStrokeWidth(60);
        /**使用抗锯齿*/mCornerPaint.setAntiAlias(true);
        /**使用防抖动*/mCornerPaint.setDither(true);
        /**设置笔触样式-圆*/mRectPaint.setStrokeCap(Paint.Cap.ROUND);
        /**设置结合处为圆弧*/mRectPaint.setStrokeJoin(Paint.Join.ROUND);

        /*文字画笔*/
        /**初始化*/mTextPaint = new Paint();
        /**设置画笔颜色*/
        mTextPaint.setColor(ContextCompat.getColor(mContext, R.color.signal_status_view_bar_green));
        /**设置画笔样式*/mTextPaint.setStyle(Paint.Style.FILL);
        /**设置画笔粗细*/mTextPaint.setStrokeWidth(50);
        /**使用抗锯齿*/mTextPaint.setAntiAlias(true);
        /**使用防抖动*/mTextPaint.setDither(true);
        /**字体大小*/mTextPaint.setTextSize(25);
        /**设置笔触样式-圆*/mRectPaint.setStrokeCap(Paint.Cap.ROUND);
        /**设置结合处为圆弧*/mRectPaint.setStrokeJoin(Paint.Join.ROUND);

        /*测绘线画笔*/
        /**初始化*/mMappingLinePaint = new Paint();
        /**设置画笔颜色*/
        mMappingLinePaint.setColor(ContextCompat.getColor(mContext, R.color.plus_minus_button_color));
        /**设置画笔样式*/mMappingLinePaint.setStyle(Paint.Style.FILL);
        /**设置画笔粗细*/mMappingLinePaint.setStrokeWidth(2);
        /**使用抗锯齿*/mMappingLinePaint.setAntiAlias(true);
        /**使用防抖动*/mMappingLinePaint.setDither(true);
        /**设置笔触样式-圆*/mMappingLinePaint.setStrokeCap(Paint.Cap.ROUND);
        /**设置结合处为圆弧*/mMappingLinePaint.setStrokeJoin(Paint.Join.ROUND);
    }
}
