//package com.fhc.laser_monitor_sw_android_rtsp_app.muxer;
//
//import android.media.MediaCodec;
//import android.media.MediaFormat;
//import android.media.MediaMuxer;
//import android.os.Environment;
//import android.util.Log;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.security.InvalidParameterException;
//import java.text.SimpleDateFormat;
//import java.util.GregorianCalendar;
//import java.util.Locale;
//
//
//public class BaseMuxer {
//    public static final boolean DEBUG = true;
//    private String TAG = getClass().getSimpleName();
//
//    private MediaMuxer mMuxer;//使用MediaMuxer对音视频进行混合封装
//
//    private int mVideoTrackIndex, mAudioTrackIndex;
//    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
//    public BaseMuxer(String name) {
//        try {
//            mMuxer = new MediaMuxer(Environment.getExternalStorageDirectory()
//                    + File.separator+"mux"
//                    +"/"+mDateTimeFormat.format(new GregorianCalendar().getTime())
//                    + "-"+name
//                    +".mp4",
//                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        mVideoTrackIndex = -1;
//        mAudioTrackIndex = -1;
//    }
//
//    public synchronized void addVideoTrack(MediaFormat mediaFormat) {
//        if (mVideoTrackIndex != -1)
//            throw new RuntimeException("already add video tracks");
//        mVideoTrackIndex = mMuxer.addTrack(mediaFormat);
//    }
//
//    public synchronized void addAudioTrack(MediaFormat mediaFormat) {
//        if (mAudioTrackIndex != -1)
//            throw new RuntimeException("already add audio tracks");
//        mAudioTrackIndex = mMuxer.addTrack(mediaFormat);
//    }
//
//    public synchronized void startMuxer() {
//        mMuxer.start();
//    }
//
//    public synchronized void writeSampleData(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo, boolean isVideo) {
//
//        if (mVideoTrackIndex==-1 || mAudioTrackIndex == -1) {
//            if(DEBUG)Log.i(TAG, String.format("pumpStream [%s] but muxer is not start.ignore..", isVideo ? "video" : "audio"));
//            return;
//        }
//
//        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//            // The codec config data was pulled out and fed to the muxer when we got
//            // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
//
//        } else if (bufferInfo.size != 0) {
//
//
//            if ((isVideo && mVideoTrackIndex == -1)|| (!isVideo && mAudioTrackIndex == -1)) {
//                throw new InvalidParameterException("muxer hasn't started");
//            }
//
//            // adjust the ByteBuffer values to match BufferInfo (not needed?)
//            outputBuffer.position(bufferInfo.offset);
//            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
//
////            if (DEBUG) Log.d(TAG, String.format("sent %s [" + bufferInfo.size + "] with timestamp:[%d] to muxer", isVideo ? "video" : "audio", bufferInfo.presentationTimeUs ));
//            mMuxer.writeSampleData(isVideo ? mVideoTrackIndex : mAudioTrackIndex, outputBuffer, bufferInfo);
//
//        }
//
//        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//            if (DEBUG) Log.i(TAG, "BUFFER_FLAG_END_OF_STREAM received");
//        }
//    }
//
//    //释放
//    public synchronized void release() {
//        if (mMuxer != null) {
//            if ( mAudioTrackIndex != -1) {//mAudioTrackIndex != -1 &&
//                if (DEBUG) Log.i(TAG, String.format("muxer is started. now it will be stoped."));
//                try {
//                    mMuxer.stop();
//                    mMuxer.release();
//                } catch (IllegalStateException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
//    }
//}
