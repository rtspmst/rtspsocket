//package com.fhc.laser_monitor_sw_android_rtsp_app.views;
//
///*
// * fixedView的作用保证准心可见
// *
// * 准星交互规则如下：
// * 视频预览缩放时：根据视频预览缩放比例实时调节准星位置
// * 准心校准逻辑：视频预览放大后，才可以校准准心
// * 准心坐标存放于SharedPreferences
// * TODO: 特别注意：直接将准星的坐标（左上角）按照比例缩放，并不能是准星始终落在同一个目标上，这是因为虽然坐标系在缩放，但是准星图片的大小却是不变的
// * TODO: 解决办法：始终以准星的中心坐标计算
// */
//
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.os.Handler;
//import android.os.Message;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//
//import com.fhc.laser_monitor_sw_android_rtsp_app.R;
//import com.fhc.laser_monitor_sw_android_rtsp_app.utils.CV;
//
//public class SightView extends RelativeLayout {
//
//    private static final String TAG = "SightView";
//    private boolean DEBUG = true;
//
//    private ImageView sightImageView;
//    private LayoutParams sightViewLayoutParams;
//
//
//    private float cx = 50f;
//    private float cy = 50f;
//
////    private int makeUpValue = 15;
////    public void setMakeUpValue(int makeUpValue) {
////        this.makeUpValue = makeUpValue;
////    }
//
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//
//
//    }
//
//    public void setCenterX(float cx) {
//        this.centerX = cx;
//    }
//
//    public void setCenterY(float cy) {
//        this.centerY = cy;
//    }
//
//    // init the sight view is untouchable
//    private boolean touchable = false;
//    private byte longClickCnt = 0;
//
//    //准星调节回调
//    private SightViewListener listener;
//
//    public interface SightViewListener {
//        void onPositionChanged(float x, float y, char flag);
//    }
//
//    public void setListener(SightViewListener listener){
//        this.listener = listener;
//    }
//
//    // TODO: for communication
//    private boolean IS_BIG_PREVIEW = false;
//    private Handler mHandler;
//
//
//    // TODO: for who am I
//    private byte WHO_AM_I = 0;
//
//
//    private float originCx = -1;
//    private float originCy = -1;
//
//    private float centerX = 50f;
//    private float centerY = 50f;
//
//    private float left = 50f;
//    private float top = 50f;
//
//
//    public void setWHO_AM_I(byte WHO_AM_I) {
//        this.WHO_AM_I = WHO_AM_I;
//    }
//
//    public void setmHandler(Handler mHandler) {
//        this.mHandler = mHandler;
//    }
//
//    public void setIS_BIG_PREVIEW(boolean IS_BIG_PREVIEW) {
//        this.IS_BIG_PREVIEW = IS_BIG_PREVIEW;
//    }
//
//    public SightView(Context context) {
//        super(context);
//        initLayout();
//    }
//
//    public SightView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        initLayout();
//    }
//
//    public SightView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        initLayout();
//    }
//
//    //初始化当前准星View
//    private void initLayout(){
//
//        if(sightImageView == null){
//            sightImageView = new ImageView(this.getContext());
//            sightImageView.setBackgroundResource(R.drawable.cross);
//            sightViewLayoutParams = new LayoutParams(
//                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//            addView(sightImageView,sightViewLayoutParams);
//        }
//        sightImageView.setX(cx);
//        sightImageView.setY(cy);
//
//        Log.e("sightView Width: ",this.getWidth()+"");
//        Log.e("sightView Height: ",this.getHeight()+"");
//
//
//        ImageView fixedView = new ImageView(this.getContext());
//        fixedView.setBackgroundResource(R.drawable.sight_view_fixed);
//        LayoutParams fixedLayoutParams = new LayoutParams(
//                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        addView(fixedView,fixedLayoutParams);
//        fixedView.setX(50);
//        fixedView.setY(10);
//
//
//        sightImageView.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//
//                // if isn't big view, return
//                if(!IS_BIG_PREVIEW){
//                    return false;
//                }
//
//                // long click 3 times
//                longClickCnt++;
//                if(longClickCnt >=3){
//                    longClickCnt = 0;
//                    Message msg = new Message();
//                    mHandler.obtainMessage();
//                    msg.what = CV.LONG_PRESS;
//                    msg.arg1 = WHO_AM_I;
//                    mHandler.sendMessage(msg);
//                }
//                Log.e("SIGHT VIEW","Long Click");
//                return false;
//            }
//        });
//    }
//
//    public void setTouchEnable(boolean touchable) {
//        this.touchable = touchable;
//    }
//
//    /*
//     * return true - consumer this touch event
//     */
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if(!touchable){
//            return super.onTouchEvent(event);
//        }
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                cx = event.getX();
//                cy = event.getY();
//                Log.e("ACTION_DOWN X: ",cx+"");
//                Log.e("ACTION_DOWN Y: ",cy+"");
//                break;
//            case MotionEvent.ACTION_MOVE:
//                cx = event.getX();
//                cy = event.getY();
//                Log.e("ACTION_MOVE X: ",cx+"");
//                Log.e("ACTION_MOVE Y: ",cy+"");
//                break;
//            case MotionEvent.ACTION_UP:
//                cx = event.getX();
//                cy = event.getY();
//                Log.e("ACTION_UP X: ",cx+"");
//                Log.e("ACTION_UP Y: ",cy+"");
//
//                break;
//        }
//
//        // draw image
//        left = cx+60;
//        top = cy+60;
//
//        // save and refresh
//        centerX = left+32;
//        centerY = top+32;
//
//        sightImageView.setX(left);
//        sightImageView.setY(top);
//
//        // notify save
//        if (this.listener != null) {
//            listener.onPositionChanged(centerX, centerY,'a');
//        }
//
//        // return true - consumer this touch event
//        return true;
//    }
//
//
//    public void setSightPositionX(float x){
//        // left = x-32
//        sightImageView.setX(x-32);
//
//        // save centerX
//        listener.onPositionChanged(x, (float) 12.3,'x');
//    }
//
//    public void setSightPositionY(float y){
//        // top = y-32
//        sightImageView.setY(y-32);
//
//        // save centerY
//        listener.onPositionChanged((float) 12.3, y,'y');
//    }
//
//    /*
//     * 1. LEFT from normal to big, so RIGHT from normal to small
//     * 2. LEFT from big to normal, so RIGHT from small to normal
//     *
//     * NORMAL_TO_BIG    -- zoom in
//     * BIG_TO_NORMAL    -- zoom out
//     *
//     * NORMAL_TO_SMALL  -- zoom out
//     * SMALL_TO_NORMAL  -- zoom in
//     *
//     */
//    public void setCoordinate (float ratio,byte zoomMode) {
//
//        float left = 50;
//        float top = 50;
//
//        switch (zoomMode){
//            case CV.NORMAL_TO_BIG:
//
//                // TODO: X和Y坐标补偿，补偿值为图片尺寸的一半
//                centerX = centerX*ratio;
//                centerY = centerY*ratio;
//
////                Log.e("NORMAL_TO_BIG centerX",centerX+"");
////                Log.e("NORMAL_TO_BIG centerY",centerY+"");
////
////                Log.e("NORMAL_TO_BIG left",left+"");
////                Log.e("NORMAL_TO_BIG top",top+"");
//
//                break;
//
//            case CV.BIG_TO_NORMAL:
//                centerX = centerX/ratio;
//                centerY = centerY/ratio;
//
////                Log.e("BIG_TO_NORMAL centerX",centerX+"");
////                Log.e("BIG_TO_NORMAL centerY",centerY+"");
//
////                Log.e("BIG_TO_NORMAL left",left+"");
////                Log.e("BIG_TO_NORMAL top",top+"");
//                break;
//            case CV.SMALL_TO_NORMAL:
//                centerX = centerX*ratio;
//                centerY = centerY*ratio;
//                break;
//
//            case CV.NORMAL_TO_SMALL:
//                centerX = centerX/ratio;
//                centerY = centerY/ratio;
//                break;
//        }
//
//        left = centerX-32;
//        top = centerY-32;
//
//        sightImageView.setX(left);
//        sightImageView.setY(top);
//
//        listener.onPositionChanged(centerX, centerY,'a');
//    }
//
//}
