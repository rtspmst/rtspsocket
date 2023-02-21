package com.fhc.laser_monitor_sw_android_rtsp_app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

//准星控制View
public class DrawView extends View {

    private float left = 0f;
    private float top = 0f;
    private float right = 0f;
    private float bottom = 0f;

    private float centerX = 0f;
    private float centerY = 0f;
    // create paint
    private Paint p = new Paint();
    //十字架长度
    private final float lineLength = 20;
    //外边框长度一半
    private final float halfRectangleLength = 100;

    public DrawView(Context context, int drawViewColor) {
        super(context);
        textColor = drawViewColor;
    }

    public void drawRec(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    //绘制用于校准的记录 拖动设置位置
    public void drawRecForCalibration(float centerX, float centerY, float scale) {
        this.centerX = centerX;
        this.centerY = centerY;

        float rectangleScaleLength = halfRectangleLength * scale;

        left = this.centerX - rectangleScaleLength;
        top = this.centerY - rectangleScaleLength;
        right = this.centerX + rectangleScaleLength;
        bottom = this.centerY + rectangleScaleLength;
    }

    public void drawRec(float centerX, float centerY, float scale) {

        this.centerX = centerX * scale;
        this.centerY = centerY * scale;

        float rectangleScaleLength = halfRectangleLength * scale;

        left = this.centerX - rectangleScaleLength;
        top = this.centerY - rectangleScaleLength;
        right = this.centerX + rectangleScaleLength;
        bottom = this.centerY + rectangleScaleLength;
    }

    int textColor = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //画一个矩形
        p.setColor(Color.RED);// set color is red
        p.setStrokeWidth(3f);
        p.setStyle(Paint.Style.STROKE);//set style is stoke

        //绘制矩形
        canvas.drawRect(left, top, right, bottom, p);// square

        //画十字
        //横线
        canvas.drawLine(centerX - lineLength, centerY, centerX + lineLength, centerY, p);
        //竖线
        canvas.drawLine(centerX, centerY - lineLength, centerX, centerY + lineLength, p);
    }
}
