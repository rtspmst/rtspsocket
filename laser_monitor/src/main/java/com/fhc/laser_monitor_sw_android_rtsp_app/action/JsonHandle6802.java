package com.fhc.laser_monitor_sw_android_rtsp_app.action;

import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.ByteConvertUtil.addBytes;
import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.ByteConvertUtil.intToBytes;
import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.HideyBarUtils.getTime;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;
import com.fhc.laser_monitor_sw_android_rtsp_app.SocketDataCallback;
import com.fhc.laser_monitor_sw_android_rtsp_app.SocketStateCallback;
import com.fhc.laser_monitor_sw_android_rtsp_app.client.JsonClient6802;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.CV;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.Language;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.MyToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 通信连接断开 准星
 * 6802
 * Cam1CenterXY 	{"Cam1CenterXY":"379,256"}	左屏小市场		正常模式晃动模式切换	DeMode	Normal/Wobble
 * Cam2CenterXY	    {"Cam2CenterXY":"365,268"}	右屏		    功率切换	AmpPwr	low/high
 * Cam3CenterXY 	{"Cam3CenterXY":"384,222"}	左屏大视场		设置音量	SpkPlaybackVol	String类型 0到500 一次25
 * 影像停止	ImgPro	off
 * 影像询问	ImgPro	query（连接成功时调用）
 */

public class JsonHandle6802 implements SocketDataCallback, SocketStateCallback {
    private final String TAG = "JsonHandle";
    private final boolean DEBUG = false;
    public static volatile boolean socketIsBroken = true;
    private final int port = 6802;
    private LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue();
    private JsonClient6802 mJsonClient6802;
    private Worker mWorker;
    private Handler mHandler = new Handler(Looper.getMainLooper());


    public JsonHandle6802() {

        mJsonClient6802 = new JsonClient6802(CV.IP, port, this, this);
    }

    @Override
    public void onSocketState(byte state) {

        //连接状态
        switch (state) {
            case 0x01:
                socketIsBroken = false;
                start();

                sendCmd(CV.IMG_PROCESS_QUERY, null);

                //模式切换
//                sendCmd(CV.DEMODE_VALUE, "Normal");

                //设置音量
//                sendCmd(CV.TX2SPK_VOLUME, String.valueOf(new SharedPreferencesUtil().getTx2spkVolume()));


                for (int i = 0; i < 3; i++) {

                    sendCmd(CV.GET_CAMERA1_CNETER, null);
                    sendCmd(CV.GET_CAMERA2_CNETER, null);
                    sendCmd(CV.GET_CAMERA3_CNETER, null);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                Log.e(TAG, " 套接字连接成功 查询准星坐标位置线程退出");

                break;
            case 0x02:
                socketIsBroken = true;

                stop();

                //提示 通信连接失败，请检查设备连接!!
                MyToastUtils.showToast(CV.TOAST_TAG2, Language.COMMUNICATION_FAILED);

                if (DEBUG) {
                    Log.e(TAG, "run:银临 通信连接断开   " + getTime());
                }
                break;
            default:
                break;
        }
    }

    public interface rectanglePointCallback {
        //返回左右两个准星的坐标
        void onRectanglePoint(int[] points);
    }

    public interface CamCenterXYCallback {
        void onCamCenterXY(String[] data);

        void onCamCenterXY1(String[] data);

        void onCamCenterXY2(String[] data);
    }

    public interface imageProcessStatusCallback {
        void onImageProcessStatus(boolean status);
    }

    public interface currentViewCallback {
        void onCurrentView(String currentView);
    }


    public void start() {
        mWorker = new Worker();
        mWorker.setRunning(true);
        mWorker.start();
    }

    public void stop() {
        if (mWorker != null) {
            mWorker.interrupt();
            mWorker.setRunning(false);
            mWorker = null;
        }
    }

    public void sendCmd(byte cmd, final String data) {

        LogUtils.e("     cmd == " + cmd + "     传值 == " + data);

        if (socketIsBroken) {
            Log.e(TAG, "sendCmd: 连接坏了！");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject();

            switch (cmd) {
                case CV.IMG_PROCESS_ON:
                    jsonObject.put("ImgPro", "on");
                    break;

                case CV.IMG_PROCESS_QUERY:
                    jsonObject.put("ImgPro", "query");
                    break;

                case CV.DEMODE_NONE:

                    //"AmpPwr":"low/high"  放大器功率切换
                    jsonObject.put("AmpPwr", data);

                    break;

                case CV.SET_DENOISE_METHOD://降噪方法标志
                    jsonObject.put("DeNoise", data);
                    break;

                case CV.SET_BEVIS_GRADE:
                    jsonObject.put("BevisGrade", data);
                    break;

                case CV.SET_WEBRTC_GRADE://降噪三下的降噪等级选择
                    jsonObject.put("WebRtcGrade", data);
                    break;

                case CV.IMG_PROCESS_OFF:
                    jsonObject.put("ImgPro", "off");
                    break;

                case CV.ADJUST_VOLUME:
                    jsonObject.put("DGain", data);
                    break;

                case CV.START_UPDATE_UI:
                    jsonObject.put("FlushUI", "on");
                    break;

                case CV.SET_CAMERA1_CNETER:
                    jsonObject.put("Cam1CenterXY", data);
                    break;

                case CV.SET_CAMERA2_CNETER:
                    jsonObject.put("Cam2CenterXY", data);
                    break;

                case CV.SET_CAMERA3_CNETER:
                    jsonObject.put("Cam3CenterXY", data);
                    break;

                case CV.GET_CAMERA1_CNETER:
                    jsonObject.put("Cam1CenterXY", "query");
                    break;

                case CV.GET_CAMERA2_CNETER:
                    jsonObject.put("Cam2CenterXY", "query");
                    break;

                case CV.GET_CAMERA3_CNETER:
                    jsonObject.put("Cam3CenterXY", "query");
                    break;

                case CV.SET_NOISE_VIEW://降噪等级选择
                    jsonObject.put("StrongMode", data);
                    break;

                case CV.SWITCH_VIEW:
                    jsonObject.put("MainCamSw", data);
                    break;
                case CV.STRONG_MODE:
                    jsonObject.put("StrongMode", "query");
                    break;
                case CV.DEMODE:
                    jsonObject.put("DeMode", "query");
                    break;
                case CV.DEMODE_VALUE:
                    jsonObject.put("DeMode", data);
                    break;
                case CV.TX2SPK_VOLUME:
                    //设置音量
                    jsonObject.put("SpkPlaybackVol", data);
                    break;
                case CV.DEMODE_SBDSRC://声音质量选择
                    jsonObject.put("SndSrcQuality", data);

                    break;
                default:
                    break;
            }

            final byte[] bytes = jsonObject.toString().getBytes();

            Log.e(TAG, Arrays.toString(bytes));

            MyApplication.getSingleThreadExecutor().execute(new Runnable() {

                @Override
                public void run() {

                    mJsonClient6802.sendBytes(addBytes(intToBytes(bytes.length), bytes));
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class Worker extends Thread {
        volatile boolean isRunning;
        private byte[] frame;

        void setRunning(boolean running) {
            isRunning = running;
        }

        @Override
        public void run() {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    frame = queue.take();

                    String jsonString = new String(frame);

                    Log.e(TAG, "准星坐标 得到数据  ===  " + jsonString);

                    JSONObject jsonObject = new JSONObject(jsonString);

                    //准星坐标
                    if (jsonString.contains("ImgMatched")) {
                        String rectangle = jsonObject.getString("ImgMatched");
                        String[] strings = rectangle.split(",");//分割

                        //接收准星坐标
                        final int[] pointArray = new int[strings.length];
                        int i = 0;
                        for (String sting : strings) {
                            try {
                                pointArray[i++] = Integer.parseInt(sting);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //左右两个准星的坐标
                                Log.e(TAG, "run: ==========左右两个准星的坐标=========== " + Arrays.toString(pointArray));
                                mRectanglePointCallback.onRectanglePoint(pointArray);
                            }
                        });
                    } else if (jsonString.contains("Cam1CenterXY")) {
                        String rectangle = jsonObject.getString("Cam1CenterXY");
                        if (!TextUtils.isEmpty(rectangle)) {
                            String s = "(\\[.*])";
                            rectangle = rectangle.replaceAll(s, "");
                        }
                        String[] strings = rectangle.replaceAll("\\\"\\\"", "").split(",");

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCamCenterXYCallback.onCamCenterXY(strings);
                            }
                        });
                    } else if (jsonString.contains("Cam2CenterXY")) {
                        String rectangle = jsonObject.getString("Cam2CenterXY");
                        if (!TextUtils.isEmpty(rectangle)) {
                            String s = "(\\[.*])";
                            rectangle = rectangle.replaceAll(s, "");
                        }
                        String[] strings = rectangle.replaceAll("\\\"\\\"", "").split(",");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                mCamCenterXYCallback.onCamCenterXY1(strings);
                            }
                        });
                    } else if (jsonString.contains("Cam3CenterXY")) {
                        String rectangle = jsonObject.getString("Cam3CenterXY");
                        if (!TextUtils.isEmpty(rectangle)) {
                            String s = "(\\[.*])";
                            rectangle = rectangle.replaceAll(s, "");
                        }
                        String[] strings = rectangle.replaceAll("\\\"\\\"", "").split(",");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCamCenterXYCallback.onCamCenterXY2(strings);
                            }
                        });
                    } else if (jsonString.contains("ImgPro")) {
                        String status = jsonObject.getString("ImgPro");
                        if ("on".equals(status)) {
                            mImageProcessStatusCallback.onImageProcessStatus(true);
                        } else if ("off".equals(status)) {
                            mImageProcessStatusCallback.onImageProcessStatus(false);
                        }
                    } else if (jsonString.contains("MainCamSw")) {
                        String currentView = jsonObject.getString("MainCamSw");
                        mCurrentViewCallback.onCurrentView(currentView);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // after work thread is stopped, stop the client
            if (DEBUG) {
                Log.e(TAG, "工作线程退出  work thread quit");
            }
            mJsonClient6802.socketStop();
        }
    }


    @Override
    public void onReceiveData(byte[] data) {
        //接收到的数据
        try {
            queue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private rectanglePointCallback mRectanglePointCallback;

    public void setmRectanglePointCallback(rectanglePointCallback mRectanglePointCallback) {
        this.mRectanglePointCallback = mRectanglePointCallback;
    }

    private CamCenterXYCallback mCamCenterXYCallback;

    public void setmCamCenterXYCallback(CamCenterXYCallback mCamCenterXYCallback) {
        this.mCamCenterXYCallback = mCamCenterXYCallback;
    }

    private imageProcessStatusCallback mImageProcessStatusCallback;

    public void setmImageProcessStatusCallback(imageProcessStatusCallback mImageProcessStatusCallback) {
        this.mImageProcessStatusCallback = mImageProcessStatusCallback;
    }

    private currentViewCallback mCurrentViewCallback;

    public void setmCurrentViewCallback(currentViewCallback mCurrentViewCallback) {
        this.mCurrentViewCallback = mCurrentViewCallback;
    }
}
