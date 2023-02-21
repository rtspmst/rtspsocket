//package com.fhc.laser_monitor_sw_android_rtsp_app.muxer;
//
//import android.media.MediaCodec;
//import android.media.MediaCodecInfo;
//import android.media.MediaFormat;
//import android.util.Log;
//
//import com.fhc.laser_monitor_sw_android_rtsp_app.action.AudioDecoder;
//import com.fhc.laser_monitor_sw_android_rtsp_app.action.VideoDecoder;
//import com.fhc.laser_monitor_sw_android_rtsp_app.utils.CV;
//
//import java.nio.ByteBuffer;
//
//public class MuxerMp4 {
//
//    private final String TAG = "Muxer";
//    private final boolean DEBUG = true;
//    private VideoDecoder videoDecoder;
//    private AudioDecoder audioDecoder;
//    private BaseMuxer mBaseMuxer;//使用MediaMuxer对音视频进行混合封装
//    private int videoTimeStamp;
//    private int audioTimeStamp;
//    private boolean videoStop = false;
//    private boolean audioStop = false;
//
//
//    private final static byte[] sps = {0x0, 0x0, 0x0, 0x1, 0x27, 0x64, 0x0, 0x28, (byte) 0xac, 0x1a, (byte) 0xe0, 0x5a, 0x1e, (byte) 0xd0, (byte) 0x80, 0x0, 0x0, 0x3, 0x0, (byte) 0x80, 0x0, 0x0, 0x5, 0x42};
//    private final static byte[] pps = {0x0, 0x0, 0x0, 0x1, 0x28, (byte) 0xee, 0x3c, (byte) 0xb0};
//
////    private final static byte[] sps = {0x00,0x00,0x00,0x01,0x67,0x42 , (byte) 0xc0,0x16 , (byte) 0xda,0x02, (byte) 0x80, (byte) 0xf6, (byte) 0x84,0x00 ,0x00 ,0x03 ,0x00 ,0x04 ,0x00 ,0x00 ,0x03 ,0x00,0x7a ,0x3c ,0x58 , (byte) 0xba, (byte) 0x80};
////    private final static byte[] pps = {0x00 ,0x00 ,0x00 ,0x01 ,0x68 , (byte) 0xce,0x0f , (byte) 0xc8};
//
//    public MuxerMp4(VideoDecoder videoDecoder, AudioDecoder audioDecoder, String name) {
//
//        this.videoDecoder = videoDecoder;
//        this.audioDecoder = audioDecoder;
//
//        //初始化媒体混合器
//        initMediaMuxer(name);
//    }
//
//
//    private void initMediaMuxer(String name) {
//
//        mBaseMuxer = new BaseMuxer(name);//使用MediaMuxer对音视频进行混合封装
//
//        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, CV.PICTURE_WIDTH, CV.PICTURE_HEIGHT);
//
//        format.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
//
//        format.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
//
//        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
//
//        mBaseMuxer.addVideoTrack(format);
//
//        // sample rate and channel count must same as AudioDecoder's setting
//        MediaFormat audioFormat = MediaFormat.createAudioFormat(
//                MediaFormat.MIMETYPE_AUDIO_AAC, 48000, 2);
//
//        audioFormat.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
//        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64000);
//        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
//
//        //must set CSD, aac only csd_0
//        byte[] data = new byte[]{(byte) 0x11, (byte) 0x90};
//        ByteBuffer cds_0 = ByteBuffer.wrap(data);
//        audioFormat.setByteBuffer("csd-0", cds_0);
//
//        mBaseMuxer.addAudioTrack(audioFormat);
//
//        mBaseMuxer.startMuxer();
//
//        //开始写数据 把视频流写入文件
//        startWriteData();
//    }
//
//    private boolean haveGetSpsInfo;//获取Sps信息
//
//    //开始写数据 把视频流写入文件
//    private void startWriteData() {
//
//        //从VideoDecoder接收到的将要录制的视频信息
//        videoDecoder.setAvDataCallback(new AVDataCallback() {
//            @Override
//            public void onFrame(byte[] frame) {
//
//                //frame 视频流
//
//                if (haveGetSpsInfo) {
//
//                    if (DEBUG) Log.d(TAG, "onFrameData: -->datas[4]:" + frame[4]);
//
//                    addMuxerVideoData(frame);
//
//                    return;
//                }
//
//                //找sps和pps
//                if ((frame[4] & 0x1f) == 7) {//sps
//
//                    addMuxerVideoData(frame);
//
//                    if (DEBUG) Log.d(TAG, "SPS");
//
//                } else if ((frame[4] & 0x1f) == 8) {//pps
//
//                    addMuxerVideoData(frame);
//
//                    if (DEBUG) Log.d(TAG, "PPS");
//
//                } else if ((frame[4] & 0x1f) == 5) {
//
//                    //第一帧为I帧
//                    haveGetSpsInfo = true;
//
//                    addMuxerVideoData(frame);
//
//                    if (DEBUG) Log.d(TAG, "IFrame");
//                }
//            }
//
//            @Override
//            public void onStopMux() {
//                videoStop = true;
//                if (videoStop && audioStop) {
//                    //释放
//                    mBaseMuxer.release();
//                }
//            }
//        });
//
//        audioDecoder.setAvDataCallback(new AVDataCallback() {
//            @Override
//            public void onFrame(byte[] frame) {
//                addMuxerAudioData(frame);
//            }
//
//            @Override
//            public void onStopMux() {
//                audioStop = true;
//                if (videoStop && audioStop) {
//                    //释放
//                    mBaseMuxer.release();
//                }
//            }
//        });
//    }
//
//
//    //视频
//    MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
//
//    private void addMuxerVideoData(byte[] datas) {
//
//        if (mBaseMuxer == null){
//            return;
//        }
//
//        videoBufferInfo.offset = 0;
//
//        videoBufferInfo.size = datas.length;
//
//        if ((datas[4] & 0x1f) == 5) {
//
//            if (DEBUG) Log.d(TAG, "onDecodeVideoFrame: -->I帧");
//            videoBufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
//
//        } else if ((datas[4] & 0x1f) == 7 || (datas[4] & 0x1f) == 8) {
//
//            if (DEBUG) Log.d(TAG, "addMuxerVideoData: -->sps or pps");
//            videoBufferInfo.flags = MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
//
//        } else {
//
//            videoBufferInfo.flags = 0;
//        }
//
//        ByteBuffer buffer = ByteBuffer.wrap(datas, videoBufferInfo.offset, videoBufferInfo.size);
//        videoBufferInfo.presentationTimeUs = videoTimeStamp++ * (1000 * 1000 / 30);
//        mBaseMuxer.writeSampleData(buffer, videoBufferInfo, true);
//    }
//
//
//    //音频
//    MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
//    private void addMuxerAudioData(byte[] datas) {
//
//        if (mBaseMuxer == null){
//            return;
//        }
//        Log.d("addMuxerAudioData", "addMuxerAudioData");
//
//
//        audioBufferInfo.offset = 0;
//        audioBufferInfo.size = datas.length;
//        audioBufferInfo.flags = 0;
//
//        Log.d("audioBufferInfo.offset", audioBufferInfo.offset + "");
//        Log.d("audioBufferInfo.size", audioBufferInfo.size + "");
//
//        ByteBuffer buffer = ByteBuffer.wrap(datas, audioBufferInfo.offset, audioBufferInfo.size);
////        audioBufferInfo.presentationTimeUs = audioTimeStamp++ * (1024 * 1000 * 1000 /48000);
//        audioBufferInfo.presentationTimeUs = audioTimeStamp++ * (960L * 1000L * 1000L / 48000L);
//
//        Log.d("presentationTimeUs", audioBufferInfo.presentationTimeUs + "");
//
//        mBaseMuxer.writeSampleData(buffer, audioBufferInfo, false);
//    }
//
//
//}
