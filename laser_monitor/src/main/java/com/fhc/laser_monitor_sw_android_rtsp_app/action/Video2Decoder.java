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
//import com.fhc.laser_monitor_sw_android_rtsp_app.activity.MainActivity;
//import com.fhc.laser_monitor_sw_android_rtsp_app.SocketDataCallback;
//import com.fhc.laser_monitor_sw_android_rtsp_app.SocketStateCallback;
//import com.fhc.laser_monitor_sw_android_rtsp_app.client.Client;
//import com.fhc.laser_monitor_sw_android_rtsp_app.utils.CV;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.concurrent.LinkedBlockingQueue;
//
//public class Video2Decoder implements SocketDataCallback,SocketStateCallback{
//    private static final String TAG = "RightVideo";
//    private static final boolean DEBUG = true;
//	private Surface mSurface=null;
//	private static Client mClient;
//	private Worker mWorker;
//	private Handler mHandler = new Handler(Looper.getMainLooper());
//
//	private LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
//
//	private final int preview_height = 480;
//    private final int preview_width = 720;
//
//	private final byte[] sps={0x0,0x0,0x0,0x1,0x27,0x64,0x0,0x28, (byte) 0xac,0x1a, (byte) 0xe0,0x5a,0x1e, (byte) 0xd0, (byte) 0x80,0x0,0x0,0x3,0x0, (byte) 0x80,0x0,0x0,0x5,0x42};
//	private final byte[] pps={0x0,0x0,0x0,0x1,0x28, (byte) 0xee,0x3c, (byte) 0xb0,0x0,0x0,0x0,0x1,0x6};
//
//    //类加载时就初始化
//    private static final Video2Decoder instance = new Video2Decoder();
//
//    public Video2Decoder() {
//
//        if(mClient==null){
//            mClient = new Client(CV.IP,6805,this,this);
//        }
//    }
//    public static Video2Decoder getInstance(){
//        return instance;
//    }
//
//    public void setmSurface(Surface mSurface) {
//        this.mSurface = mSurface;
//    }
//
//    public void setPause(byte pause) {
//        isPause = pause;
//    }
//
//    private static byte isPause = 0x02;
//	public void start() {
//		if (mWorker == null) {
//			mWorker = new Worker();
//			mWorker.setRunning(true);
//			mWorker.start();
//		}
//	}
//
//	public void stop() {
//		if (mWorker != null) {
//			mWorker.interrupt();
//			mWorker.setRunning(false);
//			mWorker = null;
//		}
//	}
//
//	@Override
//	public void onReceiveData(byte[] data) {
//		try {
//		    if(isPause==0x01){
//                queue.put(data);
//            }
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
//
//	@Override
//	public void onSocketState(byte state) {
//		switch (state){
//			case CV.SOCKET_CONNECT_SUCCESS:
//				start();
//				if(DEBUG)Log.d(TAG,"socket connect success!");
//				break;
//			case CV.SOCKET_CONNECT_BROKEN:
//				stop();
//				if(DEBUG)Log.e(TAG,"socket connect is broken!");
//				mHandler.post(new Runnable() {
//					@Override
//					public void run() {
//						Toast.makeText(MainActivity.mContext,"右视频连接断开，请检查设备连接!",Toast.LENGTH_SHORT).show();
//					}
//				});
//				break;
//		}
//	}
//
//	private class Worker extends Thread {
//		private volatile boolean isRunning;
//		private MediaCodec decoder;
//		private MediaCodec.BufferInfo mBufferInfo;
//		private int index;
//
//		boolean prepare() {
//
////            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);
//
//            Log.d(TAG,"prepare");
//			mBufferInfo = new MediaCodec.BufferInfo();
//
//			MediaFormat format = MediaFormat.createVideoFormat(
//					MediaFormat.MIMETYPE_VIDEO_AVC, preview_width, preview_height);
//
////			try {
////				byte[] sps = queue.take();
//				format.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
//
////				byte[] pps = queue.take();
//				format.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
//
////			} catch (InterruptedException e) {
////				e.printStackTrace();
////			}
//
//			try {
//				decoder = MediaCodec
//						.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
//
//			} catch (IOException e) {
//				e.printStackTrace();
//				return false;
//			}
//			decoder.configure(format, mSurface, null, 0);
//			decoder.start();
//
//			if(DEBUG)Log.d(TAG,"decoder start");
//			return true;
//		}
//
//		void setRunning(boolean running) {
//			isRunning = running;
//		}
//
//		@Override
//		public void run() {
//			if (!prepare()) {
//				if(DEBUG)Log.w(TAG, "视频解码器初始化失败");
//				isRunning = false;
//			}
//			while (isRunning && !Thread.currentThread().isInterrupted()) {
//				decode();
//			}
//			release();
//
//		}
//
//		private void decode() {
//			boolean isEOS = false;
//			while (!isEOS) {// 判断是否是流的结尾
////				if(isPause==0x02){
////					continue;
////				}
//
//                try {
//                    index = decoder.dequeueInputBuffer(10000);
//                }catch (IllegalStateException e){
//                    if(DEBUG)Log.d(TAG,"dequeueInputBuffer fail!");
//                }
//
//
//				if (index >= 0) {
//
//					byte[] frame = null;
//
//                    try {
//                        frame = queue.take();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        isEOS=true;
//                        return;
//                    }
//
////                    if(frame==null){
////                        continue;
////                    }
//
//					ByteBuffer buffer = null;
//
//					try {
//						buffer = decoder.getInputBuffer(index);
//					}catch (IllegalStateException e){
//						Log.d(TAG,"get input buffer fail!");
//					}
//
//					if (buffer == null) {
//						if(DEBUG)Log.w(TAG, "buffer=null");
//						return;
//					}
//					buffer.clear();
//					if (frame == null) {
//						if(DEBUG)Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
//						decoder.queueInputBuffer(index, 0, 0, 0,
//								MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//						isEOS = true;
//						isRunning = false;
//					} else{
//						buffer.put(frame, 0, frame.length);
//						buffer.clear();
//						buffer.limit(frame.length);
//						decoder.queueInputBuffer(index, 0, frame.length, 0,
//								MediaCodec.BUFFER_FLAG_KEY_FRAME);
//					}
//				} else {
//					isEOS = true;
//					if(DEBUG)Log.w(TAG,"get inputBuffer fail!");
//					continue;
//				}
//
//				//TODO:
//				index = decoder.dequeueOutputBuffer(mBufferInfo,
//						10000);
//				if (index >= 0 && mSurface!=null) {
//                    //Render the buffer with the default timestamp
//                    decoder.releaseOutputBuffer(index, true);
//				}else {
//                    isEOS = true;
//					if(DEBUG)Log.w(TAG,"get OutputBuffer fail!");
//                }
//			}
//
//		}
//
//		/**
//		 * 释放资源
//		 */
//		private void release() {
//			if (decoder != null) {
//
//				try{
//					decoder.stop();
//					decoder.release();
//				}catch (IllegalStateException e){
//					Log.d(TAG,"decoder release fail!");
//				}
//
//				// clear the queue
//				queue.clear();
//			}
////			mClient.stop();
//			Log.d(TAG,"video render thread quit");
//		}
//	}
//}