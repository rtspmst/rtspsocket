package org.easydarwin.video;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import androidx.core.content.ContextCompat;

import com.bk.webrtc.JniCallNative;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;


public class Client implements Closeable {

    static {
        System.loadLibrary("EasyRTSPClient");
    }

    private static native int getErrorCode(long context);

    private native long init(Context context, String key);

    private native int deInit(long context);

    public native static int getActiveDays(Context context, String key);

    private native int openStream(long context, int channel, String url, int type, int mediaType, String user, String pwd, int reconn, int outRtpPacket, int rtspOption);

    //    private native int startRecord(int context, String path);
//    private native void stopRecord(int context);
    private native void closeStream(long context);


    private static int sKey;
    private static Context mContext;
    private volatile int paused = 0;
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static Set<Integer> m_channelPause = new HashSet<>();
    private final Runnable closeTask = new Runnable() {
        @Override
        public void run() {
            if (paused > 0) {
                Log.i(TAG, "realPause! close stream");
                closeStream();
                paused = 2;
            }
        }
    };
    private int m_channel;
    private String m_url;
    private int m_type;
    private int m_mediaType;
    private String m_user;
    private String m_pwd;
    private int m_sendOption;


    public static final class FrameInfo {
        public int codec;            /* Audio Video Format */

        public int type;            /* Video frame type */
        public byte fps;            /* Video frame rate */
        public short width;            /* Video width */
        public short height;            /* Video High */

        public int reserved1;
        public int reserved2;
        public int sample_rate;    /* Audio sampling rate */
        public int channels;        /* Number of audio channels */
        public int bits_per_sample;    /* Audio sampling accuracy */

        public int length;            /* Audio Video Frame Size */
        public long timestamp_usec;    /* time stamp */
        public long timestamp_sec;    /* time stamp */

        public long stamp;

        public float bitrate;        /* Bit rate */
        public float losspacket;        /* Packet loss rate */

        public byte[] buffer;
        public int offset = 0;
        public boolean audio;
    }

    public static final class MediaInfo {

        int videoCodec;
        int fps;
        int audioCodec;
        int sample;
        int channel;
        int bitPerSample;
        int spsLen;
        int ppsLen;
        byte[] sps;
        byte[] pps;


        @Override
        public String toString() {
            return "MediaInfo{" +
                    "videoCodec=" + videoCodec +
                    ", fps=" + fps +
                    ", audioCodec=" + audioCodec +
                    ", sample=" + sample +
                    ", channel=" + channel +
                    ", bitPerSample=" + bitPerSample +
                    ", spsLen=" + spsLen +
                    ", ppsLen=" + ppsLen +
                    '}';
        }
    }

    public interface SourceCallBack {

        //Get Stream Data
        void onSourceCallBack(int m_channelId, int m_channelPtr, int m_frameType, FrameInfo frameInfo);

        //MediaInfo，fps，sps，pps。
        void onMediaInfoCallBack(int m_channelId, MediaInfo mi);

        //Connection status:
        void onEvent(int m_channelId, int err, int info);
    }


    public static final int EASY_SDK_VIDEO_FRAME_FLAG = 0x01;
    public static final int EASY_SDK_AUDIO_FRAME_FLAG = 0x02;
    public static final int EASY_SDK_EVENT_FRAME_FLAG = 0x04;
    public static final int EASY_SDK_RTP_FRAME_FLAG = 0x08;        /* RTP */
    public static final int EASY_SDK_SDP_FRAME_FLAG = 0x10;        /* SDP */
    public static final int EASY_SDK_MEDIA_INFO_FLAG = 0x20;        /* Media Type Flag*/

    public static final int EASY_SDK_EVENT_CODEC_ERROR = 0x63657272;    /* ERROR */
    public static final int EASY_SDK_EVENT_CODEC_EXIT = 0x65786974;    /* EXIT */

    public static final int TRANSTYPE_TCP = 1;
    public static final int TRANSTYPE_UDP = 2;
    private static final String TAG = Client.class.getSimpleName();

    private long mCtx;
    private static final SparseArray<SourceCallBack> SPARSE_CALLBACKS = new SparseArray<>();

    Client(Context context) {

        if (context == null) {
            throw new NullPointerException();
        }
        mCtx = init(context, JniCallNative.getInstance().jniGetRTSPkey());
        mContext = context.getApplicationContext();
        if (mCtx == 0 || mCtx == -1) {
            Log.wtf(TAG, new IllegalArgumentException("initialization failed，KEY is illegal！"));
        }
    }

    int registerCallback(SourceCallBack cb) {
        synchronized (SPARSE_CALLBACKS) {
            SPARSE_CALLBACKS.put(++sKey, cb);
            return sKey;
        }
    }

    void unrigisterCallback(SourceCallBack cb) {
        synchronized (SPARSE_CALLBACKS) {
            int idx = SPARSE_CALLBACKS.indexOfValue(cb);
            if (idx != -1) {
                SPARSE_CALLBACKS.removeAt(idx);
            }
        }
    }


    public int getLastErrorCode() {
        return getErrorCode(mCtx);
    }

    public int openStream(int channel, String url, int type, int sendOption, int mediaType, String user, String pwd) {
        m_channel = channel;
        m_url = url;
        m_type = type;
        m_mediaType = mediaType;
        m_user = user;
        m_pwd = pwd;
        m_sendOption = sendOption;
        return openStream();
    }

    public void closeStream() {
        HANDLER.removeCallbacks(closeTask);
        if (mCtx != 0) {
            closeStream(mCtx);
        }
    }


    private int openStream() {
        if (null == m_url) {
            throw new NullPointerException();
        }
        if (mCtx == 0) {
            throw new IllegalStateException("Initialization failed. KEY is illegal");
        }
        return openStream(mCtx, m_channel, m_url, m_type, m_mediaType, m_user, m_pwd, 1000, 0, m_sendOption);
    }


    private static void save2path(byte[] buffer, int offset, int length, String path, boolean append) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path, append);
            fos.write(buffer, offset, length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //onSourceCallBack()Get Stream Data
    private static void onSourceCallBack(int m_channelId, int m_channelPtr, int m_frameType, byte[] pBuf, byte[] frameBuffer) {
        if (BuildConfig.MEDIA_DEBUG) {

            int permissionCheck = ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                // frameType + size + buffer
                if (m_frameType != 0) {
                    ByteBuffer bf = ByteBuffer.allocate(5);
                    bf.put((byte) m_frameType);
                    if (m_frameType == EASY_SDK_MEDIA_INFO_FLAG) {
                        bf.putInt(pBuf.length);
                        save2path(bf.array(), 0, 5, "/sdcard/media_degbu.data", true);
                        save2path(pBuf, 0, pBuf.length, "/sdcard/media_degbu.data", true);
                    } else {
                        bf.putInt(frameBuffer.length);
                        save2path(bf.array(), 0, 5, "/sdcard/media_degbu.data", true);
                        save2path(frameBuffer, 0, frameBuffer.length, "/sdcard/media_degbu.data", true);
                    }
                }
            }
        }
        final SourceCallBack callBack;
        synchronized (SPARSE_CALLBACKS) {
            callBack = SPARSE_CALLBACKS.get(m_channelId);
        }
        if (m_frameType == 0) {
            if (callBack != null) {
                callBack.onSourceCallBack(m_channelId, m_channelPtr, m_frameType, null);
            }
            return;
        }

        if (m_frameType == EASY_SDK_MEDIA_INFO_FLAG) {
            if (callBack != null) {
                MediaInfo mi = new MediaInfo();

                ByteBuffer buffer = ByteBuffer.wrap(pBuf);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                mi.videoCodec = buffer.getInt();
                mi.fps = buffer.getInt();
                mi.audioCodec = buffer.getInt();
                mi.sample = buffer.getInt();
                mi.channel = buffer.getInt();
                mi.bitPerSample = buffer.getInt();
                mi.spsLen = buffer.getInt();
                mi.ppsLen = buffer.getInt();
                mi.sps = new byte[128];
                mi.pps = new byte[36];

                buffer.get(mi.sps);
                buffer.get(mi.pps);
//                    int videoCodec;int fps;
//                    int audioCodec;int sample;int channel;int bitPerSample;
//                    int spsLen;
//                    int ppsLen;
//                    byte[]sps;
//                    byte[]pps;

                //get MediaInfo，fps，sps，pps。
                callBack.onMediaInfoCallBack(m_channelId, mi);
            }
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(frameBuffer);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        FrameInfo fi = new FrameInfo();
        fi.codec = buffer.getInt();
        fi.type = buffer.getInt();
        fi.fps = buffer.get();
        buffer.get();
        fi.width = buffer.getShort();
        fi.height = buffer.getShort();
        buffer.getInt();
        buffer.getInt();
        buffer.getShort();
        fi.sample_rate = buffer.getInt();
        fi.channels = buffer.getInt();
        fi.bits_per_sample = buffer.getInt();
        fi.length = buffer.getInt();
        fi.timestamp_usec = buffer.getInt();
        fi.timestamp_sec = buffer.getInt();

        long sec = fi.timestamp_sec < 0 ? Integer.MAX_VALUE - Integer.MIN_VALUE + 1 + fi.timestamp_sec : fi.timestamp_sec;
        long usec = fi.timestamp_usec < 0 ? Integer.MAX_VALUE - Integer.MIN_VALUE + 1 + fi.timestamp_usec : fi.timestamp_usec;
        fi.stamp = sec * 1000000 + usec;

//        long differ = fi.stamp - mPreviewStamp;
//        Log.d(TAG, String.format("%s:%d,%d,%d, %d", EASY_SDK_VIDEO_FRAME_FLAG == m_frameType ? "video" : "audio", fi.stamp, fi.timestamp_sec, fi.timestamp_usec, differ));
        fi.buffer = pBuf;

        boolean paused = false;
        synchronized (m_channelPause) {
            paused = m_channelPause.contains(m_channelId);
        }
        if (callBack != null) {
            if (paused) {
                Log.i(TAG, "channel_" + m_channelId + " is paused!");
            }
            callBack.onSourceCallBack(m_channelId, m_channelPtr, m_frameType, fi);
        }
    }

    private static void onEvent(int channel, int err, int state) {

        Log.e(TAG, String.format("__RTSPClientCallBack onEvent: err=%d, state=%d", err, state));

        synchronized (SPARSE_CALLBACKS) {
            final SourceCallBack callBack = SPARSE_CALLBACKS.get(channel);
            if (callBack != null) {
                callBack.onEvent(channel, err, state);
            }
        }
    }


    public void pause() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalThreadStateException("please call pause in Main thread!");
        }
        synchronized (m_channelPause) {
            m_channelPause.add(m_channel);
        }
        paused = 1;
        Log.i(TAG, "pause:=" + 1);
        HANDLER.postDelayed(closeTask, 10000);
    }

    public void resume() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalThreadStateException("call resume in Main thread!");
        }
        synchronized (m_channelPause) {
            m_channelPause.remove(m_channel);
        }
        HANDLER.removeCallbacks(closeTask);
        if (paused == 2) {
            Log.i(TAG, "resume:=" + 0);
            openStream();
        }
        Log.i(TAG, "resume:=" + 0);
        paused = 0;
    }

    @Override
    public void close() throws IOException {
        HANDLER.removeCallbacks(closeTask);
        m_channelPause.remove(m_channel);
        if (mCtx == 0) {
            throw new IOException("not opened or already closed");
        }
        deInit(mCtx);
        mCtx = 0;
    }
}
