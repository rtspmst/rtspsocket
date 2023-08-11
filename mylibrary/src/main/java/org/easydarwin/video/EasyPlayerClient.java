package org.easydarwin.video;

import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar;
import static org.easydarwin.video.Client.TRANSTYPE_TCP;
import static org.easydarwin.video.EasyMuxer2.VIDEO_TYPE_H264;
import static org.easydarwin.video.EasyMuxer2.VIDEO_TYPE_H265;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import org.easydarwin.audio.EasyAACMuxer;
import org.easydarwin.sw.JNIUtil;
import org.easydarwin.util.CodecSpecificDataUtil;
import org.easydarwin.util.TextureLifecycler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * EasyPlayer It is an RTSP streaming player
 */
public class EasyPlayerClient implements Client.SourceCallBack {

    private static final long LEAST_FRAME_INTERVAL = 10000L;
    public static boolean VIDEO_STATUS;

    /* video coding  */
    public static final int EASY_SDK_VIDEO_CODEC_H264 = 0x1C;        /* H264  */
    public static final int EASY_SDK_VIDEO_CODEC_H265 = 0x48323635; /*H265*/
    public static final int EASY_SDK_VIDEO_CODEC_MJPEG = 0x08;/* MJPEG */
    public static final int EASY_SDK_VIDEO_CODEC_MPEG4 = 0x0D;/* MPEG4 */

    /* Audio encoding */
    public static final int EASY_SDK_AUDIO_CODEC_AAC = 0x15002;        /* AAC */
    public static final int EASY_SDK_AUDIO_CODEC_G711U = 0x10006;        /* G711 ulaw*/
    public static final int EASY_SDK_AUDIO_CODEC_G711A = 0x10007;    /* G711 alaw*/
    public static final int EASY_SDK_AUDIO_CODEC_G726 = 0x1100B;    /* G726 */

    //Video display
    public static final int RESULT_VIDEO_DISPLAYED = 01;

    //Video decoding method
    public static final String KEY_VIDEO_DECODE_TYPE = "video-decode-type";

    //The size of the video
    public static final int RESULT_VIDEO_SIZE = 02;

    //Indicates that KEY's available playback time has been used up
    public static final int RESULT_TIMEOUT = 03;//Trial time is up

    //Indicates that KEY's available playback time has been used up
    public static final int RESULT_EVENT = 04;
    public static final int RESULT_UNSUPPORTED_VIDEO = 05;//Video format not supported
    public static final int RESULT_UNSUPPORTED_AUDIO = 06;//Audio format not supported
    public static final int RESULT_RECORD_BEGIN = 7;//Start recording
    public static final int RESULT_RECORD_END = 8;//End recording


    //Indicates that the first frame of data has been received
    public static final int RESULT_FRAME_RECVED = 9;

    private static final String TAG = EasyPlayerClient.class.getSimpleName() + "------";

    //The width of the video
    public static final String EXTRA_VIDEO_WIDTH = "extra-video-width";
    //The height of the video
    public static final String EXTRA_VIDEO_HEIGHT = "extra-video-height";


    private static final int NAL_VPS = 32;
    private static final int NAL_SPS = 33;
    private static final int NAL_PPS = 34;

    private Surface mSurface;
    private final TextureLifecycler lifecycler;//
    private volatile Thread mThread, mAudioThread;
    private final ResultReceiver mRR;
    private Client mClient;
    private boolean mAudioEnable = true;
    private volatile long mReceivedDataLength;

    private AudioTrack mAudioTrack;

    private String mRecordingPath;//Recording address
    private EasyAACMuxer mObject;
    private EasyAACMuxer muxer;
    private EasyMuxer2 muxer2;
    private Client.MediaInfo mMediaInfo;
    private short mHeight = 0;
    short mWidth = 0;
    private ByteBuffer mCSD0;
    private ByteBuffer mCSD1;
    private final I420DataCallback i420callback;
    private boolean mMuxerWaitingKeyVideo;//Muxer Waiting for key videos
    /**
     * -1 Paused，0Normal recording ，1 Restoring 。
     */
    private int mRecordingStatus;
    private long muxerPausedMillis = 0L;
    private long mMuxerCuttingMillis = 0L;

//    private RtmpClient mRTMPClient = new RtmpClient();

    //Determine if recording is in progress
    public boolean isRecording() {
        return !TextUtils.isEmpty(mRecordingPath);
    }

    //PriorityQueue
    private static class FrameInfoQueue extends PriorityQueue<Client.FrameInfo> {
        public static final int CAPACITY = 500;
        public static final int INITIAL_CAPACITY = 300;

        public FrameInfoQueue() {
            super(INITIAL_CAPACITY, new Comparator<Client.FrameInfo>() {

                @Override
                public int compare(Client.FrameInfo frameInfo, Client.FrameInfo t1) {
                    return (int) (frameInfo.stamp - t1.stamp);
                }
            });
        }

        //ReentrantLock
        final ReentrantLock lock = new ReentrantLock();
        final Condition notFull = lock.newCondition();
        final Condition notVideo = lock.newCondition();
        final Condition notAudio = lock.newCondition();

        @Override
        public int size() {
            lock.lock();
            try {
                return super.size();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void clear() {
            lock.lock();
            try {
                int size = super.size();
                super.clear();
                int k = size;
                for (; k > 0 && lock.hasWaiters(notFull); k--) {
                    notFull.signal();
                }
            } finally {
                lock.unlock();
            }
        }

        public void put(Client.FrameInfo x) throws InterruptedException {
            lock.lockInterruptibly();
            try {
                int size;
                while ((size = super.size()) == CAPACITY) {
                    Log.v(TAG, "queue full:" + CAPACITY);
                    notFull.await();
                }
                offer(x);
//                Log.d(TAG, String.format("queue size : " + size));
                {

                    if (x.audio) {
                        notAudio.signal();
                    } else {
                        notVideo.signal();
                    }
                }

            } finally {
                lock.unlock();
            }
        }

        public Client.FrameInfo takeVideoFrame() throws InterruptedException {
            lock.lockInterruptibly();
            try {
                while (true) {
                    Client.FrameInfo x = peek();
                    if (x == null) {
                        notVideo.await();
                    } else {
                        if (!x.audio) {
                            remove();
                            notFull.signal();
                            notAudio.signal();
                            return x;
                        } else {
                            notVideo.await();
                        }
                    }
                }
            } finally {

                lock.unlock();
            }
        }

        public Client.FrameInfo takeVideoFrame(long ms) throws InterruptedException {
            lock.lockInterruptibly();
            try {
                while (true) {
                    Client.FrameInfo x = peek();
                    if (x == null) {
                        if (!notVideo.await(ms, TimeUnit.MILLISECONDS)) {
                            return null;
                        }
                    } else {
                        if (!x.audio) {
                            remove();
                            notFull.signal();
                            notAudio.signal();
                            return x;
                        } else {
                            notVideo.await();
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public Client.FrameInfo takeAudioFrame() throws InterruptedException {
            lock.lockInterruptibly();
            try {
                while (true) {
                    Client.FrameInfo x = peek();
                    if (x == null) {
                        notAudio.await();
                    } else {
                        if (x.audio) {
                            remove();
                            notFull.signal();
                            notVideo.signal();
                            return x;
                        } else {
                            notAudio.await();
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private FrameInfoQueue mQueue = new FrameInfoQueue();

    private final Context mContext;
    /**
     * Latest video timestamp
     */
    private volatile long mNewestStample;
    private boolean mWaitingKeyFrame;
    private boolean mTimeout;
    private boolean mNotSupportedVideoCB, mNotSupportedAudioCB;

    public EasyPlayerClient(Context context, Surface surface, ResultReceiver receiver) {
        this(context, surface, receiver, null);
    }

    public EasyPlayerClient(Context context, Surface surface, ResultReceiver receiver, I420DataCallback callback) {
        mSurface = surface;
        mContext = context;

        mRR = receiver;
        i420callback = callback;
        lifecycler = null;
    }

    /**
     * Start playback
     *
     * @param url
     * @param type
     * @param sendOption
     * @param mediaType
     * @param user
     * @param pwd
     * @return
     */
    public int start(final String url, int type, int sendOption, int mediaType, String user, String pwd) {
        return start(url, type, sendOption, mediaType, user, pwd, null);
    }

    /**
     * Start playback
     *
     * @param url
     * @param type
     * @param sendOption
     * @param mediaType
     * @param user
     * @param pwd
     * @param recordPath
     * @return
     */
    public int start(final String url, int type, int sendOption, int mediaType, String user, String pwd, String recordPath) {
        if (url == null) {
            throw new NullPointerException("url is null");
        }
        if (type == 0) {
            type = TRANSTYPE_TCP;
        }
        mNewestStample = 0;
        mWaitingKeyFrame = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("waiting_i_frame", true);
        mWidth = mHeight = 0;
        mQueue.clear();

        //Start Codec
        startCodec();

        mTimeout = false;
        mNotSupportedVideoCB = mNotSupportedAudioCB = false;
        mReceivedDataLength = 0;
        mClient = new Client(mContext);
        int channel = mClient.registerCallback(this);
        mRecordingPath = recordPath;
        Log.e(TAG, String.format("playing url:\n%s\n", url));
        return mClient.openStream(channel, url, type, sendOption, mediaType, user, pwd);
    }

    public boolean isAudioEnable() {
        return mAudioEnable;
    }

    public void setAudioEnable(boolean enable) {
        mAudioEnable = enable;
        AudioTrack at = mAudioTrack;

        if (at != null) {
            Log.i(TAG, String.format("audio will be %s", enable ? "enabled" : "disabled"));

            synchronized (at) {
                if (!enable) {
                    at.pause();
                    at.flush();
                } else {
                    at.flush();
                    at.play();
                }
            }

        }
    }


    public void pause() {
        mQueue.clear();
        if (mClient != null) {
            mClient.pause();
        }
        mQueue.clear();
    }

    public void resume() {
        if (mClient != null) {
            mClient.resume();
        }
    }

    public static interface I420DataCallback {
        public void onI420Data(ByteBuffer buffer);
    }

    public void stop() {
        Thread t = mThread;
        mThread = null;
        if (t != null) {
            t.interrupt();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        t = mAudioThread;
        mAudioThread = null;
        if (t != null) {
            t.interrupt();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        stopRecord();

        mQueue.clear();
        if (mClient != null) {
            mClient.unrigisterCallback(this);
            mClient.closeStream();
            try {
                mClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mQueue.clear();
        mClient = null;
        mNewestStample = 0;
    }

    public long receivedDataLength() {
        return mReceivedDataLength;
    }

    private static int getXPS(byte[] data, int offset, int length, byte[] dataOut, int[] outLen, int type) {
        int i;
        int pos0;
        int pos1;
        pos0 = -1;
        length = Math.min(length, data.length);
        for (i = offset; i < length - 4; i++) {
            if ((0 == data[i]) && (0 == data[i + 1]) && (1 == data[i + 2]) && (type == (0x0F & data[i + 3]))) {
                pos0 = i;
                break;
            }
        }
        if (-1 == pos0) {
            return -1;
        }
        if (pos0 > 0 && data[pos0 - 1] == 0) { // 0 0 0 1
            pos0 = pos0 - 1;
        }
        pos1 = -1;
        for (i = pos0 + 4; i < length - 4; i++) {
            if ((0 == data[i]) && (0 == data[i + 1]) && (1 == data[i + 2])) {
                pos1 = i;
                break;
            }
        }
        if (-1 == pos1 || pos1 == 0) {
            return -2;
        }
        if (data[pos1 - 1] == 0) {
            pos1 -= 1;
        }
        if (pos1 - pos0 > outLen[0]) {
            return -3; // Input buffer too small
        }
        dataOut[0] = 0;
        System.arraycopy(data, pos0, dataOut, 0, pos1 - pos0);
        // memcpy(pXPS+1, pES+pos0, pos1-pos0);
        // *pMaxXPSLen = pos1-pos0+1;
        outLen[0] = pos1 - pos0;
        return pos1;
    }

    private static byte[] getvps_sps_pps(byte[] data, int offset, int length) {
        int i = 0;
        int vps = -1, sps = -1, pps = -1;
        length = Math.min(length, data.length);
        do {
            if (vps == -1) {
                for (i = offset; i < length - 4; i++) {
                    if ((0x00 == data[i]) && (0x00 == data[i + 1]) && (0x01 == data[i + 2])) {
                        byte nal_spec = data[i + 3];
                        int nal_type = (nal_spec >> 1) & 0x03f;
                        if (nal_type == NAL_VPS) {
                            // vps found.
                            if (data[i - 1] == 0x00) {  // start with 00 00 00 01
                                vps = i - 1;
                            } else {                      // start with 00 00 01
                                vps = i;
                            }
                            break;
                        }
                    }
                }
            }
            if (sps == -1) {
                for (i = vps; i < length - 4; i++) {
                    if ((0x00 == data[i]) && (0x00 == data[i + 1]) && (0x01 == data[i + 2])) {
                        byte nal_spec = data[i + 3];
                        int nal_type = (nal_spec >> 1) & 0x03f;
                        if (nal_type == NAL_SPS) {
                            // vps found.
                            if (data[i - 1] == 0x00) {  // start with 00 00 00 01
                                sps = i - 1;
                            } else {                      // start with 00 00 01
                                sps = i;
                            }
                            break;
                        }
                    }
                }
            }
            if (pps == -1) {
                for (i = sps; i < length - 4; i++) {
                    if ((0x00 == data[i]) && (0x00 == data[i + 1]) && (0x01 == data[i + 2])) {
                        byte nal_spec = data[i + 3];
                        int nal_type = (nal_spec >> 1) & 0x03f;
                        if (nal_type == NAL_PPS) {
                            // vps found.
                            if (data[i - 1] == 0x00) {  // start with 00 00 00 01
                                pps = i - 1;
                            } else {                    // start with 00 00 01
                                pps = i;
                            }
                            break;
                        }
                    }
                }
            }
        } while (vps == -1 || sps == -1 || pps == -1);
        if (vps == -1 || sps == -1 || pps == -1) {// Failed to obtain success。
            return null;
        }
        // Calculate the length of the CSD buffer. A section of data from the beginning of VPS to the end of PPS
        int begin = vps;
        int end = -1;
        for (i = pps + 4; i < length - 4; i++) {
            if ((0x00 == data[i]) && (0x00 == data[i + 1]) && (0x01 == data[i + 2])) {
                if (data[i - 1] == 0x00) {  // start with 00 00 00 01
                    end = i - 1;
                } else {                    // start with 00 00 01
                    end = i;
                }
                break;
            }
        }
        if (end == -1 || end < begin) {
            return null;
        }
        // Copy and return
        byte[] buf = new byte[end - begin];
        System.arraycopy(data, begin, buf, 0, buf.length);
        return buf;
    }

    private static boolean codecMatch(String mimeType, MediaCodecInfo codecInfo) {
        String[] types = codecInfo.getSupportedTypes();
        for (String type : types) {
            if (type.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }

    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    //Start Codec
    private void startCodec() {
        mThread = new Thread("VIDEO_CONSUMER") {

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                tasksThreading();
            }
        };
        mThread.start();
    }

    private void tasksThreading() {
        //Set thread priority
        Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

        MediaCodec mCodec = null;

        int mColorFormat = 0;

        VideoCodec.VideoDecoderLite mDecoder = null, displayer = null;

        try {
            boolean pushBlankBuffersOnStop = true;

            int index = 0;
            // previous
            long previousStampUs = 0L;
            long lastFrameStampUs = 0L;
            long differ = 0;
            int realWidth = mWidth;
            int realHeight = mHeight;
            int sliceHeight = realHeight;

            int frameWidth = 0;
            int frameHeight = 0;

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

            Client.FrameInfo initFrameInfo = null;
            Client.FrameInfo frameInfo = null;
            while (mThread != null) {
                if (mCodec == null && mDecoder == null) {
                    if (frameInfo == null) {
                        frameInfo = mQueue.takeVideoFrame();
                    }
                    initFrameInfo = frameInfo;
                    //SPUtil.KEY_SW_CODEC = "use-sw-codec";
                    try {
                        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("use-sw-codec", false)) {
                            //User settings software codec
                            throw new IllegalStateException("user set sw codec");
                        }
                        final String mime = frameInfo.codec == EASY_SDK_VIDEO_CODEC_H264 ? "video/avc" : "video/hevc";
                        MediaFormat format = MediaFormat.createVideoFormat(mime, mWidth, mHeight);
                        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
                        format.setInteger(MediaFormat.KEY_PUSH_BLANK_BUFFERS_ON_STOP, pushBlankBuffersOnStop ? 1 : 0);
                        if (mCSD0 != null) {
                            format.setByteBuffer("csd-0", mCSD0);
                        } else {
                            throw new InvalidParameterException("csd-0 is invalid.");
                        }
                        if (mCSD1 != null) {
                            format.setByteBuffer("csd-1", mCSD1);
                        } else {
                            if (frameInfo.codec == EASY_SDK_VIDEO_CODEC_H264) {
                                throw new InvalidParameterException("csd-1 is invalid.");
                            }
                        }
                        MediaCodecInfo ci = selectCodec(mime);
                        mColorFormat = CodecSpecificDataUtil.selectColorFormat(ci, mime);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            MediaCodecInfo.CodecCapabilities capabilities = ci.getCapabilitiesForType(mime);
                            MediaCodecInfo.VideoCapabilities videoCapabilities = capabilities.getVideoCapabilities();
                            boolean supported = videoCapabilities.isSizeSupported(mWidth, mHeight);
                            Log.e(TAG, "Media Codec  " + ci.getName() + (supported ? "support" : "not support") + mWidth + "*" + mHeight);

                            if (!supported) {
                                boolean b1 = videoCapabilities.getSupportedWidths().contains(mWidth + 0);
                                boolean b2 = videoCapabilities.getSupportedHeights().contains(mHeight + 0);
                                supported |= b1 && b2;
                                if (supported) {
                                    Log.w(TAG, ".......................................................................");
                                } else {
                                    throw new IllegalStateException("media codec " + ci.getName() + (supported ? "support" : "not support") + mWidth + "*" + mHeight);
                                }
                            }
                        }
                        Log.e(TAG, String.format("Configure Codec:%s", format));

                        MediaCodec codec = MediaCodec.createByCodecName(ci.getName());
                        codec.configure(format, i420callback != null ? null : mSurface, null, 0);
                        codec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                        codec.start();
                        mCodec = codec;
                        if (i420callback != null) {
                            final VideoCodec.VideoDecoderLite decoder = new VideoCodec.VideoDecoderLite();
                            decoder.create(mSurface, frameInfo.codec == EASY_SDK_VIDEO_CODEC_H264);
                            displayer = decoder;
                        }
                    } catch (Throwable e) {
                        if (mCodec != null) {
                            mCodec.release();
                        }
                        mCodec = null;
                        if (displayer != null) {
                            displayer.close();
                        }
                        displayer = null;
                        Log.e(TAG, String.format("Due to initialization codec error %s", e.getMessage()));
                        e.printStackTrace();
                        final VideoCodec.VideoDecoderLite decoder = new VideoCodec.VideoDecoderLite();
                        decoder.create(mSurface, frameInfo.codec == EASY_SDK_VIDEO_CODEC_H264);
                        mDecoder = decoder;
                    }

                } else {
                    frameInfo = mQueue.takeVideoFrame(5);
                }
                if (frameInfo != null) {
//                    Log.e(TAG, "video " + frameInfo.stamp + " take[" + (frameInfo.stamp - lastFrameStampUs) + "]");
                    if (frameHeight != 0 && frameWidth != 0) {
                        if (frameInfo.width != 0 && frameInfo.height != 0) {
                            if (frameInfo.width != frameWidth || frameInfo.height != frameHeight) {
                                frameHeight = frameInfo.height;
                                frameWidth = frameInfo.width;
                                stopRecord();
                                if (mCodec != null) {
                                    mCodec.release();
                                    mCodec = null;
                                    continue;
                                }
                            }
                        }
                    }
                    frameHeight = frameInfo.height;
                    frameWidth = frameInfo.width;
                    pumpVideoSample(frameInfo);
                    lastFrameStampUs = frameInfo.stamp;
                }
                do {
                    if (mDecoder != null) {
                        if (frameInfo != null) {
                            long decodeBegin = SystemClock.elapsedRealtime();
                            int[] size = new int[2];
//                                mDecoder.decodeFrame(frameInfo, size);
                            ByteBuffer buf = mDecoder.decodeFrameYUV(frameInfo, size);
                            if (i420callback != null && buf != null) {
                                i420callback.onI420Data(buf);
                            }
                            if (buf != null) {
                                mDecoder.releaseBuffer(buf);
                            }
                            long decodeSpend = SystemClock.elapsedRealtime() - decodeBegin;

                            boolean firstFrame = previousStampUs == 0L;
                            if (firstFrame) {
                                Log.e(TAG, String.format("POST The movie is already displayed!!!"));
                                ResultReceiver rr = mRR;
                                if (rr != null) {
                                    Bundle data = new Bundle();
                                    data.putInt(KEY_VIDEO_DECODE_TYPE, 0);
                                    rr.send(RESULT_VIDEO_DISPLAYED, data);
                                }
                            }

                            if (previousStampUs != 0L) {
                                long sleepTime = frameInfo.stamp - previousStampUs - decodeSpend * 1000;
                                if (sleepTime > 100000) {
                                    Log.e(TAG, "Sleep time too long:" + sleepTime);
                                    sleepTime = 100000;
                                }
                                if (sleepTime > 0) {
                                    sleepTime %= 100000;
                                    long cache = mNewestStample - frameInfo.stamp;
                                    sleepTime = fixSleepTime(sleepTime, cache, 50000);
                                    if (sleepTime > 0) {
                                        Thread.sleep(sleepTime / 1000);
                                    }
                                    Log.e(TAG, "cache:" + cache);
                                }
                            }
                            previousStampUs = frameInfo.stamp;
                        }
                    } else {
                        try {
                            do {
                                if (frameInfo != null) {
                                    byte[] pBuf = frameInfo.buffer;
                                    index = mCodec.dequeueInputBuffer(10);
                                    if (false) {
                                        throw new IllegalStateException("fake state");
                                    }
                                    if (index >= 0) {
                                        ByteBuffer buffer = mCodec.getInputBuffers()[index];
                                        buffer.clear();
                                        if (pBuf.length > buffer.remaining()) {
                                            mCodec.queueInputBuffer(index, 0, 0, frameInfo.stamp, 0);
                                        } else {
                                            buffer.put(pBuf, frameInfo.offset, frameInfo.length);
                                            mCodec.queueInputBuffer(index, 0, buffer.position(), frameInfo.stamp + differ, 0);
                                        }
                                        frameInfo = null;
                                    }
                                }
                                index = mCodec.dequeueOutputBuffer(info, 10); //
                                switch (index) {
                                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                                        Log.i(TAG, "The information output buffer has been changed");
                                        break;
                                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                        MediaFormat mf = mCodec.getOutputFormat();
                                        Log.i(TAG, "The information output format has been changed ：" + mf);
                                        int width = mf.getInteger(MediaFormat.KEY_WIDTH);
                                        if (mf.containsKey("crop-left") && mf.containsKey("crop-right")) {
                                            width = mf.getInteger("crop-right") + 1 - mf.getInteger("crop-left");
                                        }
                                        int height = mf.getInteger(MediaFormat.KEY_HEIGHT);
                                        if (mf.containsKey("crop-top") && mf.containsKey("crop-bottom")) {
                                            height = mf.getInteger("crop-bottom") + 1 - mf.getInteger("crop-top");
                                        }
                                        realWidth = width;
                                        realHeight = height;

                                        if (mf.containsKey(MediaFormat.KEY_SLICE_HEIGHT)) {
                                            sliceHeight = mf.getInteger(MediaFormat.KEY_SLICE_HEIGHT);
                                        } else {
                                            sliceHeight = realHeight;
                                        }
                                        break;
                                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                                        // Output is empty
                                        break;
                                    default:

                                        // -1 epresents the first frame of data
                                        long newSleepUs = -1;
                                        boolean firstTime = previousStampUs == 0L;
                                        if (!firstTime) {
                                            long sleepUs = (info.presentationTimeUs - previousStampUs);
                                            if (sleepUs > 100000) {

                                                Log.w(TAG, "Sleep time too long:" + sleepUs);
                                                sleepUs = 100000;
                                            } else if (sleepUs < 0) {
                                                Log.w(TAG, "Sleep time is too short:" + sleepUs);
                                                sleepUs = 0;
                                            }

                                            {
                                                long cache = mNewestStample - lastFrameStampUs;
                                                newSleepUs = fixSleepTime(sleepUs, cache, 100000);
                                                // Log.d(TAG, String.format("sleepUs:%d,newSleepUs:%d,Cache:%d", sleepUs, newSleepUs, cache));
                                            }
                                        }

                                        //previousStampUs = info.presentationTimeUs;
                                        ByteBuffer outputBuffer;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            outputBuffer = mCodec.getOutputBuffer(index);
                                        } else {
                                            outputBuffer = mCodec.getOutputBuffers()[index];
                                        }
                                        if (i420callback != null && outputBuffer != null) {
                                            if (sliceHeight != realHeight) {
                                                ByteBuffer tmp = ByteBuffer.allocateDirect(realWidth * realHeight * 3 / 2);
                                                outputBuffer.clear();
                                                outputBuffer.limit(realWidth * realHeight);
                                                tmp.put(outputBuffer);

                                                outputBuffer.clear();
                                                outputBuffer.position(realWidth * sliceHeight);
                                                outputBuffer.limit((realWidth * sliceHeight + realWidth * realHeight / 4));
                                                tmp.put(outputBuffer);

                                                outputBuffer.clear();
                                                outputBuffer.position(realWidth * sliceHeight + realWidth * realHeight / 4);
                                                outputBuffer.limit((realWidth * sliceHeight + realWidth * realHeight / 4 + realWidth * realHeight / 4));
                                                tmp.put(outputBuffer);

                                                tmp.clear();
                                                outputBuffer = tmp;
                                            }

                                            if (mColorFormat == COLOR_FormatYUV420SemiPlanar || mColorFormat == COLOR_FormatYUV420PackedSemiPlanar || mColorFormat == COLOR_TI_FormatYUV420PackedSemiPlanar) {
                                                JNIUtil.yuvConvert2(outputBuffer, realWidth, realHeight, 4);
                                            }
                                            i420callback.onI420Data(outputBuffer);
                                            displayer.decoder_decodeBuffer(outputBuffer, realWidth, realHeight);
                                        }
                                        //previewStampUs = info.presentationTimeUs;
                                        if (false && Build.VERSION.SDK_INT >= 21) {
                                            Log.d(TAG, String.format("releaseoutputbuffer:%d,stampUs:%d", index, previousStampUs));
                                            mCodec.releaseOutputBuffer(index, previousStampUs);
                                        } else {
                                            if (newSleepUs < 0) {
                                                newSleepUs = 0;
                                            }
//                                            Log.d(TAG,String.format("sleep:%d", newSleepUs/1000));
                                            Thread.sleep(newSleepUs / 1000);
                                            mCodec.releaseOutputBuffer(index, i420callback == null);
                                        }
                                        if (firstTime) {
                                            Log.i(TAG, String.format("POST The movie is already displayed!!!"));
                                            ResultReceiver rr = mRR;
                                            if (rr != null) {
                                                Bundle data = new Bundle();
                                                data.putInt(KEY_VIDEO_DECODE_TYPE, 1);
                                                rr.send(RESULT_VIDEO_DISPLAYED, data);
                                            }
                                        }
                                        previousStampUs = info.presentationTimeUs;
                                }

                            } while (frameInfo != null || index < MediaCodec.INFO_TRY_AGAIN_LATER);
                        } catch (IllegalStateException ex) {
                            // Media codec error...

                            ex.printStackTrace();

                            if (mCodec != null) {
                                mCodec.release();
                            }
                            mCodec = null;
                            if (displayer != null) {
                                displayer.close();
                            }
                            displayer = null;

                            final VideoCodec.VideoDecoderLite decoder = new VideoCodec.VideoDecoderLite();
                            decoder.create(mSurface, initFrameInfo.codec == EASY_SDK_VIDEO_CODEC_H264);
                            mDecoder = decoder;
                            continue;
                        }

                    }
                    break;
                } while (true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCodec != null) {
//                        mCodec.stop();
                mCodec.release();
            }
            if (mDecoder != null) {
                mDecoder.close();
            }
            if (displayer != null) {
                displayer.close();
            }
        }
    }

    //Repair sleep time
    private static final long fixSleepTime(long sleepTimeUs, long totalTimestampDifferUs, long delayUs) {
        if (totalTimestampDifferUs < 0L) {
            Log.e(TAG, String.format("Total timestamp is:%d, this should not be happen.", totalTimestampDifferUs));
            totalTimestampDifferUs = 0;
        }
        double dValue = ((double) (delayUs - totalTimestampDifferUs)) / 1000000d;
        double radio = Math.exp(dValue);
        double r = sleepTimeUs * radio + 0.5f;

        return (long) r;
    }

    private void pumpAACSample(Client.FrameInfo frameInfo) {
        EasyMuxer muxer = mObject;
        if (muxer == null) {
            return;
        }
        MediaCodec.BufferInfo bi = new MediaCodec.BufferInfo();
        bi.offset = frameInfo.offset;
        bi.size = frameInfo.length;
        ByteBuffer buffer = ByteBuffer.wrap(frameInfo.buffer, bi.offset, bi.size);
        bi.presentationTimeUs = frameInfo.stamp;

        try {
            if (!frameInfo.audio) {
                //The frame should be audio！
                throw new IllegalArgumentException("frame should be audio!");
            }
            if (frameInfo.codec != EASY_SDK_AUDIO_CODEC_AAC) {
                //Audio codec should be AAC
                throw new IllegalArgumentException("audio codec should be aac!");
            }
            bi.offset += 7;
            bi.size -= 7;
            muxer.pumpStream(buffer, bi, false);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

    private int video_index = 0;


    //onSourceCallBack() Get Stream Data
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onSourceCallBack(int _channelId, int _channelPtr, int _frameType, Client.FrameInfo frameInfo) {
//        long begin = SystemClock.elapsedRealtime();
        try {
            onRTSPSourceCallBack1(_channelId, _channelPtr, _frameType, frameInfo);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
//            Log.d(TAG, String.format("onRTSPSourceCallBack %d", SystemClock.elapsedRealtime() - begin));
        }
    }

    public void onRTSPSourceCallBack1(int _channelId, int _channelPtr, int _frameType, Client.FrameInfo frameInfo) {
        Thread.currentThread().setName("PRODUCER_THREAD");
        if (frameInfo != null) {
            mReceivedDataLength += frameInfo.length;
        }
        if (_frameType == Client.EASY_SDK_VIDEO_FRAME_FLAG) {
            //Log.d(TAG,String.format("receive video frame"));
            if (frameInfo.codec != EASY_SDK_VIDEO_CODEC_H264 && frameInfo.codec != EASY_SDK_VIDEO_CODEC_H265) {
                ResultReceiver rr = mRR;
                if (!mNotSupportedVideoCB && rr != null) {
                    mNotSupportedVideoCB = true;
                    //Video format not supported
                    rr.send(RESULT_UNSUPPORTED_VIDEO, null);
                }
                return;
            }
//            save2path(frameInfo.buffer, 0, frameInfo.length, Environment.getExternalStorageDirectory() +"/ShunFengEr/264.h264", true);
            if (frameInfo.width == 0 || frameInfo.height == 0) {
                return;
            }

            if (frameInfo.length >= 4) {
                if (frameInfo.buffer[0] == 0 && frameInfo.buffer[1] == 0 && frameInfo.buffer[2] == 0 && frameInfo.buffer[3] == 1) {
                    if (frameInfo.length >= 8) {
                        if (frameInfo.buffer[4] == 0 && frameInfo.buffer[5] == 0 && frameInfo.buffer[6] == 0 && frameInfo.buffer[7] == 1) {
                            frameInfo.offset += 4;
                            frameInfo.length -= 4;
                        }
                    }
                }
            }


            if (frameInfo.type == 1) {
                Log.e(TAG, String.format("recv I frame"));
            }

            mNewestStample = frameInfo.stamp;
            frameInfo.audio = false;
            if (mWaitingKeyFrame) {

                ResultReceiver rr = mRR;
                Bundle bundle = new Bundle();

                Log.e(TAG, " : " + frameInfo.width);
                Log.e(TAG, " : " + frameInfo.height);

                bundle.putInt(EXTRA_VIDEO_WIDTH, frameInfo.width);
                bundle.putInt(EXTRA_VIDEO_HEIGHT, frameInfo.height);
                mWidth = frameInfo.width;
                mHeight = frameInfo.height;

                Log.e(TAG, String.format("Result video size:%d*%d", frameInfo.width, frameInfo.height));

                if (rr != null) {
                    rr.send(RESULT_VIDEO_SIZE, bundle);
                }

                Log.e(TAG, String.format(" ====    ===  width:%d,height:%d", mWidth, mHeight));


                if (frameInfo.codec == EASY_SDK_VIDEO_CODEC_H264) {
                    byte[] dataOut = new byte[128];
                    int[] outLen = new int[]{128};
                    int result = getXPS(frameInfo.buffer, 0, 256, dataOut, outLen, 7);
                    if (result >= 0) {
                        ByteBuffer csd0 = ByteBuffer.allocate(outLen[0]);
                        csd0.put(dataOut, 0, outLen[0]);
                        csd0.clear();
                        mCSD0 = csd0;
                        Log.e(TAG, String.format("CSD-0 Searched"));
                    }
                    outLen[0] = 128;
                    result = getXPS(frameInfo.buffer, 0, 256, dataOut, outLen, 8);
                    if (result >= 0) {
                        ByteBuffer csd1 = ByteBuffer.allocate(outLen[0]);
                        csd1.put(dataOut, 0, outLen[0]);
                        csd1.clear();
                        mCSD1 = csd1;
                        Log.e(TAG, String.format("CSD-1 Searched"));
                    }
                    if (false) {
                        int off = (result - frameInfo.offset);
                        frameInfo.offset += off;
                        frameInfo.length -= off;
                    }
                } else {
                    byte[] spsPps = getvps_sps_pps(frameInfo.buffer, 0, 256);
                    if (spsPps != null) {
                        mCSD0 = ByteBuffer.wrap(spsPps);
                    }
                }

                if (frameInfo.type != 1) {
                    Log.e(TAG, String.format("Discard p frames"));
                    return;
                }
                mWaitingKeyFrame = false;
                synchronized (this) {
                    if (!TextUtils.isEmpty(mRecordingPath) && mObject == null) {
                        startRecord(mRecordingPath);
                    }
                }
            } else {
                int width = frameInfo.width;
                int height = frameInfo.height;
                if (width != 0 && height != 0) {
                    if (width != mWidth || height != mHeight) {
                        //Resolution change...
                        ResultReceiver rr = mRR;
                        Bundle bundle = new Bundle();

                        bundle.putInt(EXTRA_VIDEO_WIDTH, frameInfo.width);
                        bundle.putInt(EXTRA_VIDEO_HEIGHT, frameInfo.height);
                        mWidth = frameInfo.width;
                        mHeight = frameInfo.height;

                        Log.e(TAG, String.format("Result video size:%d*%d", frameInfo.width, frameInfo.height));
                        if (rr != null) {
                            rr.send(RESULT_VIDEO_SIZE, bundle);
                        }
                    }
                }
            }
//            Log.d(TAG, String.format("queue size :%d", mQueue.size()));
            try {

                VIDEO_STATUS = true;
                mQueue.put(frameInfo);

            } catch (InterruptedException e) {
                VIDEO_STATUS = false;
                e.printStackTrace();
            }
        } else if (_frameType == 0) {
            //overtime...
            if (!mTimeout) {
                mTimeout = true;

                ResultReceiver rr = mRR;

                if (rr != null) {
                    rr.send(RESULT_TIMEOUT, null);
                }
            }
        } else if (_frameType == Client.EASY_SDK_EVENT_FRAME_FLAG) {
            ResultReceiver rr = mRR;
            Bundle resultData = new Bundle();
            resultData.putString("event-msg", new String(frameInfo.buffer));
            if (rr != null) {
                rr.send(RESULT_EVENT, null);
            }
        }
    }

    //MediaInfo，fps，sps，pps。
    @Override
    public void onMediaInfoCallBack(int _channelId, Client.MediaInfo mi) {
        mi.sample = 48000;
        mi.channel = 2;
        mi.bitPerSample = 16;
        mi.fps = 15;

        mMediaInfo = mi;
        Log.e("MediaInfo", String.format("MediaInfo fetchd\n%s", mi));
    }

    //state：
    //1：Connecting，2：Connection error，3：Connection thread exit
    @Override
    public void onEvent(int channel, int err, int info) {
        ResultReceiver rr = mRR;
        Bundle resultData = new Bundle();

        switch (info) {
            case 1:
                resultData.putString("event-msg", "Connecting...");
                break;
            case 2:
                resultData.putInt("errorcode", err);
                resultData.putString("event-msg", String.format("error：%d", err));
                break;
            case 3:
                resultData.putInt("errorcode", err);
                resultData.putString("event-msg", String.format("Thread Exit。%d", err));
                break;
            default:
                break;
        }
        if (rr != null) {
            rr.send(RESULT_EVENT, resultData);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public synchronized void startRecord(String path) {

        if (mMediaInfo == null || mWidth == 0 || mHeight == 0 || mCSD0 == null || !VIDEO_STATUS) {
            return;
        }

        mRecordingPath = path;


        //native init proffmpeg VideoCodecer
        EasyMuxer2 muxer2 = new EasyMuxer2();

        mMuxerCuttingMillis = 0L;
        mRecordingStatus = 0;
        muxerPausedMillis = 0;
        ByteBuffer csd1 = this.mCSD1;

        if (csd1 == null) {
            csd1 = ByteBuffer.allocate(0);
        }

        byte[] extra = new byte[mCSD0.capacity() + csd1.capacity()];

        mCSD0.clear();
        csd1.clear();

        mCSD0.get(extra, 0, mCSD0.capacity());

        csd1.get(extra, mCSD0.capacity(), csd1.capacity());

        int r = muxer2.create(path, mMediaInfo.videoCodec == EASY_SDK_VIDEO_CODEC_H265 ? VIDEO_TYPE_H265 : VIDEO_TYPE_H264, mWidth, mHeight, extra, mMediaInfo.sample, mMediaInfo.channel);

        if (r != 0) {
            Log.e(TAG, "create muxer2:" + r);
            return;
        }

        mMuxerWaitingKeyVideo = true;
        this.muxer2 = muxer2;

        ResultReceiver rr = mRR;

        if (rr != null) {
            rr.send(RESULT_RECORD_BEGIN, null);
        }

    }

    //Write Video
    private synchronized void pumpVideoSample(Client.FrameInfo frameInfo) {
        EasyMuxer2 muxer2 = this.muxer2;

        if (muxer2 == null) {
            return;
        }

        if (mRecordingStatus < 0) {
            return;
        }

        if (mMuxerWaitingKeyVideo) {

            if (frameInfo.type == 1) {

                mMuxerWaitingKeyVideo = false;

                if (mRecordingStatus == 1) {

                    mMuxerCuttingMillis += SystemClock.elapsedRealtime() - muxerPausedMillis;
                    mRecordingStatus = 0;
                }
            }
        }
        if (mMuxerWaitingKeyVideo) {
            Log.i(TAG, "writeFrame ignore due to no key frame!");
            return;
        }


        int r = muxer2.writeFrame(EasyMuxer2.AVMEDIA_TYPE_VIDEO, frameInfo.buffer, frameInfo.offset, frameInfo.length, (long) (video_index++ * 1000.0 / 15.0));


    }

    //Write audio
    public synchronized void pumpPCMSample(byte[] pcm, int length, long stampMS) {

        EasyMuxer2 muxer2 = this.muxer2;

        if (muxer2 == null) {
            return;
        }

        if (mRecordingStatus < 0) {
            return;
        }

        if (mMuxerWaitingKeyVideo) {
            Log.i(TAG, "writeFrame ignore due to no key frame!");
            return;
        }
        long timeStampMillis = Math.max(0, stampMS);

        //native
        int r = muxer2.writeFrame(EasyMuxer2.AVMEDIA_TYPE_AUDIO, pcm, 0, length, timeStampMillis);


    }

    public synchronized void stopRecord() {
        video_index = 0;
        mRecordingPath = null;
        mMuxerCuttingMillis = 0L;
        mRecordingStatus = 0;
        muxerPausedMillis = 0;
        EasyMuxer2 muxer2 = this.muxer2;
        if (muxer2 == null) {
            return;
        }
        this.muxer2 = null;
        muxer2.close();
        mObject = null;
        ResultReceiver rr = mRR;
        if (rr != null) {
            rr.send(RESULT_RECORD_END, null);
        }

    }


    //=======================================================================================================================*/


    public EasyPlayerClient(Context context, final TextureView view, ResultReceiver receiver, I420DataCallback callback) {
        lifecycler = new TextureLifecycler(view);
        mContext = context;

        mRR = receiver;
        i420callback = callback;

        LifecycleObserver observer1 = new LifecycleObserver() {
            @OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
            public void destory() {
                stop();
                mSurface.release();
                mSurface = null;
            }

            @OnLifecycleEvent(value = Lifecycle.Event.ON_CREATE)
            private void create() {
                mSurface = new Surface(view.getSurfaceTexture());
            }
        };
        lifecycler.getLifecycle().addObserver(observer1);
        if (context instanceof LifecycleOwner) {
            LifecycleObserver observer = new LifecycleObserver() {
                @OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
                public void destory() {
                    stop();
                }

                @OnLifecycleEvent(value = Lifecycle.Event.ON_PAUSE)
                private void pause() {
                    EasyPlayerClient.this.pause();
                }


                @OnLifecycleEvent(value = Lifecycle.Event.ON_RESUME)
                private void resume() {
                    EasyPlayerClient.this.resume();
                }
            };
            ((LifecycleOwner) context).getLifecycle().addObserver(observer);
        }
    }

    public synchronized void pauseRecord() {
        if (mRecordingStatus != -1) {
            mRecordingStatus = -1;
            muxerPausedMillis = SystemClock.elapsedRealtime();
        }
    }

    public synchronized void resumeRecord() {
        if (mRecordingStatus == -1) {
            mMuxerWaitingKeyVideo = true;
            mRecordingStatus = 1;
        }
    }
}