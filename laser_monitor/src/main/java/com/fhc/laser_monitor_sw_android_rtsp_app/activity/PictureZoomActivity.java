package com.fhc.laser_monitor_sw_android_rtsp_app.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.fhc.laser_monitor_sw_android_rtsp_app.R;

import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.HideyBarUtils.getRealHeight;
import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.HideyBarUtils.getRealWidth;

/*图片缩放页面*/
public class PictureZoomActivity extends AppCompatActivity implements View.OnTouchListener {

    private ImageView img_test;
    // 縮放控制
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    // 不同状态的表示：
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    // 定义第一个按下的点，两只接触点的重点，以及出事的两指按下的距离：
    private PointF startPoint = new PointF();
    private PointF midPoint = new PointF();
    private float oriDis = 1f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_zoom);
        img_test = (ImageView) findViewById(R.id.main_imgZooming);

        String path = getIntent().getStringExtra("path");
        Bitmap bitmap = getNewBitmap(fileToBitmap(path));
        img_test.setImageBitmap(bitmap);

        img_test.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // 单指
            case MotionEvent.ACTION_DOWN:
                matrix.set(view.getImageMatrix());
                savedMatrix.set(matrix);
                startPoint.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            // 双指
            case MotionEvent.ACTION_POINTER_DOWN:
                oriDis = distance(event);
                if (oriDis > 10f) {
                    savedMatrix.set(matrix);
                    midPoint = middle(event);
                    mode = ZOOM;
                }
                break;
            // 手指放开
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            // 单指滑动事件
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    // 是一个手指拖动
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - startPoint.x, event.getY() - startPoint.y);
                } else if (mode == ZOOM) {
                    // 两个手指滑动
                    float newDist = distance(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oriDis;
                        matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                    }
                }
                break;
            default:
                break;
        }
        // 设置ImageView的Matrix
        view.setImageMatrix(matrix);
        return true;
    }

    // 计算两个触摸点之间的距离
    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Float.valueOf(String.valueOf(Math.sqrt(x * x + y * y)));
    }

    // 计算两个触摸点的中点
    private PointF middle(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new PointF(x / 2, y / 2);
    }

    //文件转Bitmap
    public Bitmap fileToBitmap(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        /**
         *压缩长宽各为一半避免图片过大装载不了
         */
        options.inPurgeable = true;
        options.inSampleSize = 1;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public Bitmap getNewBitmap(Bitmap bitmap) {
        // 获得图片的宽高.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //设置图片居于屏幕中央
        float aaa = (getRealWidth(this) - width) / 2;
        float bbb = (getRealHeight(this) - height) / 2;
        matrix.postTranslate(aaa, bbb);
        img_test.setImageMatrix(matrix);

        // 计算缩放比例.
//        float scaleHeight = width / height;
//        float scaleWidth = scaleHeight;
//
//        // 取得想要缩放的matrix参数.
//        Matrix matrix = new Matrix();
//        matrix.postScale(scaleWidth, scaleHeight);

        // 得到新的图片.
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newBitmap;
    }
}