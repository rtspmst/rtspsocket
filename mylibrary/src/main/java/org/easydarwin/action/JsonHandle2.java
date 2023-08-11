package org.easydarwin.action;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.easydarwin.ByteConvertUtil;
import org.easydarwin.CV;
import org.easydarwin.LanguageTr;
import org.easydarwin.MyToastUtils;
import org.easydarwin.SingletonInternalClass;
import org.easydarwin.SocketDataCallback;
import org.easydarwin.SocketStateCallback;
import org.easydarwin.client.JsonClient2;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Communication connection  ，Quasi star
 * 6802
 * Cam1CenterXY 	{"Cam1CenterXY":"379,256"}
 * Cam2CenterXY	    {"Cam2CenterXY":"365,268"}
 * Cam3CenterXY 	{"Cam3CenterXY":"384,222"}
 * ImgPro	off
 * ImgPro	query
 */

public class JsonHandle2 implements SocketDataCallback, SocketStateCallback {
    private final String TAG = "JsonHandle";
    private final boolean DEBUG = false;
    public static volatile boolean socketIsBroken = true;
    private final int port = 6802;
    private LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue();
    private JsonClient2 mJsonClient2;
    private Worker mWorker;
    private Handler mHandler = new Handler(Looper.getMainLooper());


    public JsonHandle2() {

        mJsonClient2 = new JsonClient2(this, this);
    }

    @Override
    public void onSocketState(byte state) {

        switch (state) {
            case 0x01:
                socketIsBroken = false;
                start();

                sendCmd(CV.IMG_PROCESS_QUERY, null);

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

                Log.e(TAG, " Socket connection successful ");

                break;
            case 0x02:
                socketIsBroken = true;

                stop();

                //prompt
                MyToastUtils.showToast(CV.TOAST_TAG2, LanguageTr.COMMUNICATION_FAILED);

                break;
            default:
                break;
        }
    }

    public interface rectanglePointCallback {
        //Returns the coordinates of the left and right collimators
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


        if (socketIsBroken) {
            Log.e(TAG, "sendCmd: The connection is broken！");
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

                case CV.SET_DENOISE_METHOD://Noise reduction method flag
                    jsonObject.put("DeNoise", data);
                    break;

                case CV.SET_BEVIS_GRADE:
                    jsonObject.put("BevisGrade", data);
                    break;

                case CV.SET_WEBRTC_GRADE://Selection of noise reduction level under noise reduction three
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

                case CV.SET_NOISE_VIEW://Selection of noise reduction level
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
                case CV.DEMODE_SBDSRC://Sound quality selection
                    jsonObject.put("SndSrcQuality", data);

                    break;
                default:
                    break;
            }

            final byte[] bytes = jsonObject.toString().getBytes();

            Log.e(TAG, Arrays.toString(bytes));

            SingletonInternalClass.getInstance().getSingleThreadExecutor().execute(new Runnable() {

                @Override
                public void run() {

                    mJsonClient2.sendBytes(ByteConvertUtil.addBytes(ByteConvertUtil.intToBytes(bytes.length), bytes));
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

                    Log.e(TAG, "Quasi stellar coordinates  ===  " + jsonString);

                    JSONObject jsonObject = new JSONObject(jsonString);

                    //准星坐标
                    if (jsonString.contains("ImgMatched")) {
                        String rectangle = jsonObject.getString("ImgMatched");
                        String[] strings = rectangle.split(",");//segmentation

                        //Receive the coordinates of the collimator
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
                                //The coordinates of the left and right collimators
                                Log.e(TAG, "run: ==========The coordinates of the left and right collimators=========== " + Arrays.toString(pointArray));
                                mRectanglePointCallback.onRectanglePoint(pointArray);
                            }
                        });
                    } else if (jsonString.contains("Cam1CenterXY")) {
                        String rectangle = jsonObject.getString("Cam1CenterXY");
                        if (!TextUtils.isEmpty(rectangle)) {
                            String s = "(\\[.*])";
                            rectangle = rectangle.replaceAll(s, "");
                        }
                        final String[] strings = rectangle.replaceAll("\\\"\\\"", "").split(",");

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
                        final String[] strings = rectangle.replaceAll("\\\"\\\"", "").split(",");
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
                        final String[] strings = rectangle.replaceAll("\\\"\\\"", "").split(",");
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

            mJsonClient2.socketStop();
        }
    }


    @Override
    public void onReceiveData(byte[] data) {
        //Received data
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
