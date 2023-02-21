package com.fhc.laser_monitor_sw_android_rtsp_app.views;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.activity.MainActivity;

/**
 * 可拖拽矩形
 * 第一种
 * 矩形始终展示 修改的同一个
 * 四点可调整
 */
public class MyDragRectangleOneView extends View {

    private Context mContext;

    private Paint mRectPaint;
    private Paint mCornerPaint;
    private Paint mTextPaint;
    private Paint mTextCancelPaint;
    private Paint mlinePaint;
    private Paint mMappingLinePaint;

    private int width;
    private int height;
    private float currentX;
    private float currentY;

    private int minimum = 50;//最小间距

    /*矩形操作状态  0-不动 1-拖动 2-边角缩放 3-边框缩放*/
    private int mOperatingStatus = 0;

    private int mCornerStatus;//记录点击的哪个角
    private float eventX;
    private float eventY;
    private float down_X;
    private float down_Y;
    private float centerX;
    private float centerY;

    public MyDragRectangleOneView(Context context) {
        super(context);
        this.mContext = context;
        initPaint();
    }

    public MyDragRectangleOneView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initPaint();
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


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*绘制十字线*/
        canvas.drawLine(startX, startY + (endY - startY) / 2, endX, startY + (endY - startY) / 2, mMappingLinePaint);
        canvas.drawLine(startX + (endX - startX) / 2, startY, startX + (endX - startX) / 2, startY + (endY - startY), mMappingLinePaint);

        /*绘制边框*/
        canvas.drawRect(startX, startY, endX, endY, mRectPaint);

        canvas.drawLine(startX, startY, startX + minimum / 2, startY, mlinePaint);
        canvas.drawLine(startX, startY, startX, startY + minimum / 2, mlinePaint);

        canvas.drawLine(startX, endY, startX + minimum / 2, endY, mlinePaint);
        canvas.drawLine(startX, endY - minimum / 2, startX, endY, mlinePaint);

        canvas.drawLine(endX - minimum / 2, startY, endX, startY, mlinePaint);
        canvas.drawLine(endX, startY, endX, startY + minimum / 2, mlinePaint);

        canvas.drawLine(endX - minimum / 2, endY, endX, endY, mlinePaint);
        canvas.drawLine(endX, endY, endX, endY - minimum / 2, mlinePaint);

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

        SharedPreferences sharedPreferences = mContext.getSharedPreferences("CALIBRATE",
                Activity.MODE_PRIVATE);
        centerX = sharedPreferences.getFloat("coordinateX0", 360f);
        centerY = sharedPreferences.getFloat("coordinateY0", 240f);

        startX = centerX * height / MainActivity.IMAGE_HEIGHT - 80;
        endX = centerX * height / MainActivity.IMAGE_HEIGHT + 80;
        startY = centerY * height / MainActivity.IMAGE_HEIGHT - 80;
        endY = centerY * height / MainActivity.IMAGE_HEIGHT + 80;

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

                    down_X = event.getX();
                    down_Y = event.getY();

                    toolPointIsInBorderline(event.getX(), event.getY());
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

                        if (startX > endX || startY > endY) {
                            outOfBounds = true;
                        } else {
                            outOfBounds = false;
                        }

                        switch (mCornerStatus) {
                            case 0://左上角
                                temp_move_start_x = startX + eventX - currentX;
                                if (temp_move_start_x > 5 && temp_move_start_x < endX - minimum) {
                                    isSlide_start_X = true;
                                } else {
                                    isSlide_start_X = false;
                                }

                                if (isSlide_start_X) {
                                    startX = temp_move_start_x;
                                }

                                temp_move_start_y = startY + eventY - currentY;
                                if (temp_move_start_y > 5 && temp_move_start_y < endY - minimum) {
                                    isSlide_start_Y = true;
                                } else {
                                    isSlide_start_Y = false;
                                }

                                if (isSlide_start_Y) {
                                    startY = temp_move_start_y;
                                }

                                break;
                            case 1://左下角
                                temp_move_start_x = startX + eventX - currentX;
                                if (temp_move_start_x > 5 && temp_move_start_x < endX - minimum) {
                                    isSlide_start_X = true;
                                } else {
                                    isSlide_start_X = false;
                                }

                                if (isSlide_start_X) {
                                    startX = temp_move_start_x;
                                }


                                temp_move_end_y = endY + eventY - currentY;
                                if (temp_move_end_y < height - 5 && temp_move_end_y > startY + minimum) {
                                    isSlide_end_Y = true;
                                } else {
                                    isSlide_end_Y = false;
                                }

                                if (isSlide_end_Y) {
                                    endY = temp_move_end_y;
                                }

                                break;
                            case 2://右上角

                                temp_move_end_x = endX + eventX - currentX;
                                if (temp_move_end_x < width - 5 && temp_move_end_x > startX + minimum) {
                                    isSlide_end_X = true;
                                } else {
                                    isSlide_end_X = false;
                                }

                                if (isSlide_end_X) {
                                    endX = temp_move_end_x;
                                }

                                temp_move_start_y = startY + eventY - currentY;
                                if (temp_move_start_y > 5 && temp_move_start_y < endY - minimum) {
                                    isSlide_start_Y = true;
                                } else {
                                    isSlide_start_Y = false;
                                }

                                if (isSlide_start_Y) {
                                    startY = temp_move_start_y;
                                }

                                break;
                            case 3://右下角
                                temp_move_end_x = endX + eventX - currentX;
                                if (temp_move_end_x < width - 5 && temp_move_end_x > startX + minimum) {
                                    isSlide_end_X = true;
                                } else {
                                    isSlide_end_X = false;
                                }

                                if (isSlide_end_X) {
                                    endX = temp_move_end_x;
                                }

                                temp_move_end_y = endY + eventY - currentY;
                                if (temp_move_end_y < height - 5 && temp_move_end_y > startY + minimum) {
                                    isSlide_end_Y = true;
                                } else {
                                    isSlide_end_Y = false;
                                }

                                if (isSlide_end_Y) {
                                    endY = temp_move_end_y;
                                }
                                break;
                            default:
                                break;
                        }

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
            if (down_X == currentX && down_Y == currentY) {
                redraw();
            }
        }

    }

    private void redraw() {
        startX = centerX * height / MainActivity.IMAGE_HEIGHT - 80;
        endX = centerX * height / MainActivity.IMAGE_HEIGHT + 80;
        startY = centerY * height / MainActivity.IMAGE_HEIGHT - 80;
        endY = centerY * height / MainActivity.IMAGE_HEIGHT + 80;
        /**重绘*/invalidate();
    }

    /**
     * 判断按下的点是否在矩形内
     */
    private boolean toolPointIsInRect(float x, float y) {
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
        /**设置画笔粗细*/mTextPaint.setStrokeWidth(minimum);
        /**使用抗锯齿*/mTextPaint.setAntiAlias(true);
        /**使用防抖动*/mTextPaint.setDither(true);
        /**字体大小*/mTextPaint.setTextSize(25);
        /**设置笔触样式-圆*/mRectPaint.setStrokeCap(Paint.Cap.ROUND);
        /**设置结合处为圆弧*/mRectPaint.setStrokeJoin(Paint.Join.ROUND);

        /*取消文字画笔*/
        /**初始化*/mTextCancelPaint = new Paint();
        /**设置画笔颜色*/
        mTextCancelPaint.setColor(ContextCompat.getColor(mContext, R.color.signal_status_view_bar_warning));
        /**设置画笔样式*/mTextCancelPaint.setStyle(Paint.Style.FILL);
        /**设置画笔粗细*/mTextCancelPaint.setStrokeWidth(minimum);
        /**使用抗锯齿*/mTextCancelPaint.setAntiAlias(true);
        /**使用防抖动*/mTextCancelPaint.setDither(true);
        /**字体大小*/mTextCancelPaint.setTextSize(50);
        /**设置笔触样式-圆*/mTextCancelPaint.setStrokeCap(Paint.Cap.ROUND);
        /**设置结合处为圆弧*/mTextCancelPaint.setStrokeJoin(Paint.Join.ROUND);

        /**初始化*/mlinePaint = new Paint();
        /**设置 线 画笔颜色*/
        mlinePaint.setColor(ContextCompat.getColor(mContext, R.color.plus_minus_button_color));
        /**设置画笔样式*/mlinePaint.setStyle(Paint.Style.FILL);
        /**设置画笔粗细*/mlinePaint.setStrokeWidth(6);
        /**使用抗锯齿*/mlinePaint.setAntiAlias(true);
        /**使用防抖动*/mlinePaint.setDither(true);
        /**设置笔触样式-圆*/mlinePaint.setStrokeCap(Paint.Cap.ROUND);
        /**设置结合处为圆弧*/mlinePaint.setStrokeJoin(Paint.Join.ROUND);

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
