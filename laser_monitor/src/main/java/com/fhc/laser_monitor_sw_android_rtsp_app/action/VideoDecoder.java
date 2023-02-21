//package com.fhc.laser_monitor_sw_android_rtsp_app.action;
//
//import android.media.MediaCodec;
//import android.media.MediaFormat;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.view.Surface;
//import android.widget.Toast;
//
//import com.fhc.laser_monitor_sw_android_rtsp_app.SocketDataCallback;
//import com.fhc.laser_monitor_sw_android_rtsp_app.SocketStateCallback;
//import com.fhc.laser_monitor_sw_android_rtsp_app.client.Client;
//import com.fhc.laser_monitor_sw_android_rtsp_app.muxer.AVDataCallback;
//import com.fhc.laser_monitor_sw_android_rtsp_app.utils.CV;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.concurrent.LinkedBlockingQueue;
//
//import static com.fhc.laser_monitor_sw_android_rtsp_app.activity.MainActivity.mContext;
//import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.HideyBarUtils.getTime;
//
//public class VideoDecoder implements SocketDataCallback, SocketStateCallback {
//
//    private static final String TAG = "LeftVideo";
//    private static final boolean DEBUG = false;
//
//    private Surface mSurface = null;
//    private final int TIMEOUT_US = 10000;
//    private static Client mClient;
//    private RenderVideoThread mRenderVideoThread;
//    private WriteH264FileThread mWriteH264FileThread;
//    private Handler mHandler = new Handler(Looper.getMainLooper());
//
//
//    private volatile boolean RECORD_FLAG = false;//是否开始录像 标志
//
//    private LinkedBlockingQueue<byte[]> h264Queue = new LinkedBlockingQueue<>();
//    private LinkedBlockingQueue<byte[]> h264QueueForMp4 = new LinkedBlockingQueue<>();//如果RECORD_FLAG为true 开始在里面存放视频
//
//    private final byte[] sps = {0x0, 0x0, 0x0, 0x1, 0x27, 0x64, 0x0, 0x28, (byte) 0xac, 0x1a, (byte) 0xe0, 0x5a, 0x1e, (byte) 0xd0, (byte) 0x80, 0x0, 0x0, 0x3, 0x0, (byte) 0x80, 0x0, 0x0, 0x5, 0x42};
//    private final byte[] pps = {0x0, 0x0, 0x0, 0x1, 0x28, (byte) 0xee, 0x3c, (byte) 0xb0};
//
////    private final static byte[] sps = {0x00,0x00,0x00,0x01,0x67,0x42 , (byte) 0xc0,0x16 , (byte) 0xda,0x02, (byte) 0x80, (byte) 0xf6, (byte) 0x84,0x00 ,0x00 ,0x03 ,0x00 ,0x04 ,0x00 ,0x00 ,0x03 ,0x00,0x7a ,0x3c ,0x58 , (byte) 0xba, (byte) 0x80};
////    private final static byte[] pps = {0x00 ,0x00 ,0x00 ,0x01 ,0x68 , (byte) 0xce,0x0f , (byte) 0xc8};
//
//    public void setAvDataCallback(AVDataCallback avDataCallback) {
//        this.avDataCallback = avDataCallback;
//    }
//
//    private AVDataCallback avDataCallback;
//
//
//    //类加载时就初始化
//    private static final VideoDecoder instance = new VideoDecoder();
//
//    public VideoDecoder() {
//        if (mClient == null) {
//            mClient = new Client(CV.IP, 6803, this, this);
//        }
//    }
//
//    public static VideoDecoder getInstance() {
//        return instance;
//    }
//
//
//    public void setSurface(Surface mSurface) {
//        this.mSurface = mSurface;
//    }
//
//    public void setPause(byte pause) {
//        isPause = pause;
//    }
//
//    private static byte isPause = 0x02;
////    private static byte isPlayback = 0x02;
//
//    @Override
//    public void onSocketState(byte state) {
//        switch (state) {
//            case CV.SOCKET_CONNECT_SUCCESS:
//
//                start();
//
//                if (DEBUG) Log.d(TAG, "socket connect success!");
//
//                break;
//            case CV.SOCKET_CONNECT_BROKEN:
//
//                stop();
//
//                if (DEBUG) Log.e(TAG, "socket connect is broken!");
//
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        Log.e(TAG, "run:银临 左视频断开" + getTime());
//                        Toast.makeText(mContext, "左视频连接断开，请检查设备连接!", Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//                break;
//        }
//    }
//
//    public void start() {
//        if (mRenderVideoThread == null) {
//            mRenderVideoThread = new RenderVideoThread();
//            mRenderVideoThread.setRunning(true);
//            mRenderVideoThread.start();
//        }
//    }
//
//    // active stop
//    public void stop() {
//        if (mRenderVideoThread != null) {
//            mRenderVideoThread.interrupt();
//            mRenderVideoThread.setRunning(false);
//            mRenderVideoThread = null;
//        }
//    }
//
//    //开始录制视频
//    public void startRecord() {
//        RECORD_FLAG = true;
//
//        if (DEBUG) Log.d(TAG, "start record ");
//
//        //TODO: start a new thread for write h264 file
//        if (mWriteH264FileThread == null) {
//            mWriteH264FileThread = new WriteH264FileThread();
//            mWriteH264FileThread.setRunning(true);
//            mWriteH264FileThread.start();
//        }
//    }
//
//    //停止录制视频
//    public void stopRecord() {
//        if (DEBUG) Log.d(TAG, "stop record ");
//
//        RECORD_FLAG = false;
//
//        // stop the thread of send h264 data to Muxer
//        if (mWriteH264FileThread != null) {
//            mWriteH264FileThread.interrupt();
//            mWriteH264FileThread.setRunning(false);
//
//            // 等待当前线程写入h264文件完成，然后停止Muxer    wait current thread write h264 file finish, then stop the Muxer
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//
//                    try {
//
//                        //thread.join的含义是当前线程需要等待previousThread线程终止之后才从thread.join返回。
//                        // 简单来说，就是线程没有执行完之前，会一直阻塞在join方法处。
//
//                        mWriteH264FileThread.join();
//
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    mWriteH264FileThread = null;
//
//                    if (avDataCallback != null) {
//
//                        avDataCallback.onStopMux();
//
//                    }
//
//                }
//            }).start();
//
//        }
//    }
//
//    //开始录制视频线程
//    private class WriteH264FileThread extends Thread {
//
//        private boolean isRunning = false; //通过setRunning 设置是否循环
//
//        private byte[] h264_data;
//
//        public void setRunning(boolean running) {
//            isRunning = running;
//        }
//
//        @Override
//        public void run() {
//
//            while (isRunning && !Thread.currentThread().isInterrupted()) {
//
//                try {
//
//                    h264_data = h264QueueForMp4.take();
//
//                    if (avDataCallback != null) {
//
//                        avDataCallback.onFrame(h264_data);
//                    }
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            Log.d(TAG, "write h264 file thread quit");
//        }
//    }
//
//
//    //渲染视频线程
//    private class RenderVideoThread extends Thread {
//
//        private volatile boolean isRunning;
//
//        //MediaCodec是Android提供的用于对音视频进行编解码的类，它通过访问底层的codec来实现编解码的功能
//        private MediaCodec decoder;
//
//        private MediaCodec.BufferInfo mBufferInfo;
//
//        private int inIndex;
//
//        public void setRunning(boolean running) {
//            isRunning = running;
//        }
//
//        @Override
//        public void run() {
//
//            if (!prepare()) {
//
//                if (DEBUG) Log.w(TAG, "video decoder init fail!");
//                isRunning = false;
//            }
//
//            while (isRunning && !Thread.currentThread().isInterrupted()) {
//
//                decode();
//
//            }
//
//            // release
//            release();
//        }
//
//
//       public boolean prepare() {
//
////            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);
//
//            mBufferInfo = new MediaCodec.BufferInfo();//媒体编解码器缓冲区信息
//            //媒体格式
//            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 720, 480);
//
////            try {
////                byte[] sps = h264Queue.take();
//
//            //ByteBuffer.wrap(sps) sps 这个缓冲区的数据会存放在byte数组中，bytes数组或buff缓冲区任何一方中数据的改动都会影响另一方。其实ByteBuffer底层本来就有一个bytes数组负责来保存buffer缓冲区中的数据，通过allocate方法系统会帮你构造一个byte数组
//            format.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
//
////                byte[] pps = h264Queue.take();
//            format.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
////
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
//
//            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
//
//            try {
//
//                //初始化解码器操作，具体要设置解码类型，高度，宽度，还有一个用于显示视频的surface。
//                decoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            decoder.configure(format, mSurface, null, 0);
//            decoder.start();
//
//            if (DEBUG) Log.d(TAG, " 解码器启动 decoder start");
//
//            return true;
//        }
//
//
//        private void decode() {
//
//            boolean isEOS = false;
//            while (!isEOS) {// 判断是否是流的结尾
//
////                if(isPause==0x02){
////                    continue;
////                }
//
//                try {
//                    inIndex = decoder.dequeueInputBuffer(TIMEOUT_US);
//                } catch (IllegalStateException e) {
//                    if (DEBUG) Log.d(TAG, "dequeueInputBuffer fail!");
//                }
//
//
//                if (inIndex >= 0) {
//
//                    byte[] videoData = null;
//
//
//                    try {
//                        videoData = h264Queue.take();
////                        videoData = h264Queue.poll(3000,TimeUnit.MILLISECONDS);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        isEOS = true;
//                        return;
//                    }
//
////                    if(videoData==null){
////                        continue;
////                    }
//
//                    ByteBuffer buffer = null;
//
//                    try {
//                        buffer = decoder.getInputBuffer(inIndex);
//                    } catch (IllegalStateException e) {
//                        if (DEBUG) Log.d(TAG, "get input buffer fail!");
//                    }
//
//                    if (buffer == null) {
//                        if (DEBUG) Log.w(TAG, "buffer=null");
//                        return;
//                    }
//                    buffer.clear();
//                    if (videoData == null) {
//                        if (DEBUG) Log.w(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
//                        decoder.queueInputBuffer(inIndex, 0, 0, 0,
//                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//                        isEOS = true;
//                        isRunning = false;
//                    } else {
//                        buffer.put(videoData, 0, videoData.length);
//                        buffer.clear();
//                        buffer.limit(videoData.length);
//                        decoder.queueInputBuffer(inIndex, 0, videoData.length, 0,
//                                MediaCodec.BUFFER_FLAG_KEY_FRAME);
//                    }
//                } else {
//                    isEOS = true;
//                    if (DEBUG) Log.w(TAG, "get inputBuffer fail!");
//                    continue;
//                }
//
//                //TODO:
//                int outIndex = decoder.dequeueOutputBuffer(mBufferInfo,
//                        TIMEOUT_US);
//
//                if (outIndex >= 0 && mSurface != null) {
//
//                    //Render the buffer with the default timestamp
//                    decoder.releaseOutputBuffer(outIndex, true);
//
//                } else {
//                    isEOS = true;
//                    if (DEBUG) Log.w(TAG, "获取OutputBuffer失败！");
//                }
//            }
//
//        }
//
//        //释放
//        private void release() {
//            if (decoder != null) {
//
//                try {
//                    decoder.stop();
//                    decoder.release();
//                } catch (IllegalStateException e) {
//                    if (DEBUG) Log.d(TAG, "解码器释放失败！");
//                }
//
//
//                // clear the queue
//                h264Queue.clear();
//                h264QueueForMp4.clear();
//            }
////			mClient.stop();
//            if (DEBUG) Log.d(TAG, "视频渲染线程退出");
//        }
//    }
//
//
//    @Override
//    public void onReceiveData(byte[] data) {
//        try {
//
//            if (isPause == 0x01) {
//
//                h264Queue.put(data);
//
//            }
//
//            if (RECORD_FLAG) {
//
//                h264QueueForMp4.put(data);
//
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}