package com.fhc.laser_monitor_sw_android_rtsp_app.action;

import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.ByteConvertUtil.ShortArraytoByteArray;
import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.ByteConvertUtil.byteArrayToShortArray;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;
import com.fhc.laser_monitor_sw_android_rtsp_app.SocketDataCallback;
import com.fhc.laser_monitor_sw_android_rtsp_app.SocketStateCallback;
import com.fhc.laser_monitor_sw_android_rtsp_app.client.AudioClient6801;
import com.fhc.laser_monitor_sw_android_rtsp_app.fragment.PlayFragment;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.CV;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.Language;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.MyToastUtils;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.SharedPreferencesUtil;
import com.wzc.agc.AgcUtils;

import org.easydarwin.opus.opus.OpusUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

//音频解码器  音频连接断开
public class AudioDecoder implements SocketDataCallback, SocketStateCallback {

    private static final String TAG = "Audio";
    private static final boolean DEBUG = false;
    public static byte AUDIO_CONNECT = 0;

    private PlayFragment fragment;

    private AudioClient6801 mAudioClient6801;
    private DecodeOpusEncodeAacThread mDecodeOpusEncodeAacThread;

    private String filePath;
    private FileOutputStream fos;

    private final AgcUtils agcUtils;
    private final SharedPreferencesUtil sp_util;

    //设置暂停
    public void setPause(byte pause) {
        isPause = pause;
    }

    //本地的缓存队列
    private LinkedBlockingQueue<byte[]> audioOpusQueue = new LinkedBlockingQueue();
    //音量值
    public static int VALUE_VOLUME = 0;
    //判断是fou开启录像标志
    private volatile boolean RECORD_FLAG = false;

    public static byte AUDIO_PAUSE = 0x01;
    public static byte AUDIO_START = 0x02;
    private static byte isPause = AUDIO_START;
    private volatile boolean write_finish_flag = false;
    private AudioManager audioManager;
    private OpusUtils opusUtils;
    private long decoder;

    //音频解码器
    public AudioDecoder(PlayFragment fragment, AudioManager audioManager) {

        agcUtils = new AgcUtils();

        sp_util = new SharedPreferencesUtil();

        VALUE_VOLUME = sp_util.getVolume();

        agcUtils.setAgcConfig(0, VALUE_VOLUME * 3, 1).prepare();

        mAudioClient6801 = new AudioClient6801(CV.IP, 6801, this, this);

        this.fragment = fragment;
        this.audioManager = audioManager;
        this.opusUtils = OpusUtils.Companion.getInstant();
    }

    @Override
    public void onReceiveData(byte[] data) {

        try {
            if (isPause == AUDIO_START) {
                //put()方法向队列中生产数据，当队列满时，线程阻塞
                //接收到音频数据 这里会不停的接受数据
                audioOpusQueue.put(data);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSocketState(byte state) {
        switch (state) {
            case 0x01:

                //套接字连接成功
                start();

                AUDIO_CONNECT = 1;

                if (DEBUG) {
                    Log.e(TAG, "音频 套接字连接成功");
                }

                break;
            case 0x02:

                //套接字连接已损坏！
                stop();
                AUDIO_CONNECT = 0;

                //提示 音频连接失败，请检查设备连接!
                MyToastUtils.showToast(CV.TOAST_TAG1, Language.AUDIO_CONNECTION_FAILED);

                break;
            default:
                break;
        }
    }

    private class DecodeOpusEncodeAacThread extends Thread {
        private boolean isRunning = false;

        //FRAME_SIZE = 960;
        //960 for 48K
        private static final int MAX_FRAME_SIZE = 8 * 960;
        private static final int SAMPLE_RATE = 48000;

        private static final int NUM_CHANNELS = 2;

        //MediaPlayer可以播放多种格式的声音文件，例如MP3，WAV，OGG，AAC，MIDI等。
        // 然而AudioTrack只能播放PCM数据流
        private AudioTrack track;

        private byte[] frame;

        boolean prepare() {

            //设置线程优先级
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            /**
             * 音频管理员AudioManager
             * AudioFormat.CHANNEL_OUT_MONO：输出单声道音频数据
             * AudioFormat.CHANNEL_OUT_STEREO：输出双声道音频数据（立体声）
             */
            int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

            //音频属性
            AudioAttributes audioAttributes = new AudioAttributes
                    .Builder()
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build();

            //AudioFormat.CHANNEL_OUT_MONO 单声道
            AudioFormat audioFormat = new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO)
                    .build();

            int sessionId = audioManager.generateAudioSessionId();

            track = new AudioTrack(audioAttributes, audioFormat, bufferSize, AudioTrack.MODE_STREAM, sessionId);

            //调用C创建解码器
            decoder = OpusUtils.Companion.getInstant().createDecoder(48000, NUM_CHANNELS);

            //播放 play audio
            track.play();

            return true;
        }

        void setRunning(boolean running) {
            isRunning = running;
        }

        @Override
        public void run() {
            if (!prepare()) {
                if (DEBUG) {
                    Log.w(TAG, "解码器初始化失败");
                }
                isRunning = false;
            }
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                decodeOpusToPcm();
            }
            release();
        }

        //将Opus解码为Pcm
        private void decodeOpusToPcm() {

            boolean isEOS = false;

            while (!isEOS) {// 判断是否是流的结尾

                try {
                    frame = audioOpusQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isEOS = true;
                }

                /* des: 将Opus数据解码为PCM
                 *  par: decoded 是解码后的大小（short）
                 */
                short[] decodeBufferArray = new short[MAX_FRAME_SIZE];
                short[] changeTotalCache = new short[MAX_FRAME_SIZE];

                int size = opusUtils.decode(decoder, frame, decodeBufferArray);

                byte[] pcm_data = ShortArraytoByteArray(decodeBufferArray, size * NUM_CHANNELS);

                //写入pcm文件流 播放声音
                if (size > 0) {

                    if (VALUE_VOLUME == 0) {

                        //音量为0时 不写入播放器 假静音 下面为正常播放数据代码
                        //开始录音， 对aac进行编码并写入文件 start record, encode aac and write to file
                        startRecording(pcm_data, false);

                    } else {

                        //byte转short
                        short[] shorts = byteArrayToShortArray(pcm_data);

                        for (int i = 0; i < 12; i++) {
                            short[] data = new short[80];
                            short[] outData = new short[80];
                            System.arraycopy(shorts, i * data.length, data, 0, data.length);

                            //从webtrc_agc项目复制过来的 不注释掉不好使
                            //ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(data);

                            int status = agcUtils.agcProcess(data, 0, 80, outData, 0, 0, 0, 0);

                            //把outData组装到changeTotalCache
                            System.arraycopy(outData, 0, changeTotalCache, i * outData.length, outData.length);
                        }

                        // shot转byte
                        byte[] pcm_data1 = ShortArraytoByteArray(changeTotalCache, size * NUM_CHANNELS);

                        //开始录音，
                        // 对aac进行编码并写入文件 start record, encode aac and write to file
                        startRecording(pcm_data1, true);
                    }
                }
            }
        }

        //开始录音，参数 pcm_data 不一样
        // 对aac进行编码并写入文件 start record, encode aac and write to file
        private void startRecording(byte[] pcm_data, boolean muteFalse) {

            //如果返回true 则写入播放器播放声音 否则为静音 不播放
            if (muteFalse) {
                track.write(pcm_data, 0, pcm_data.length);
            }

            if (RECORD_FLAG) {
                write_finish_flag = false;
                //把pcm传递给fragment
                fragment.pumpPCM(pcm_data, pcm_data.length, (long) (audioTimestampNum++ * (pcm_data.length / 4 * 1000.0 / SAMPLE_RATE)));
                try {
                    if (fos != null) {
                        // 将PCM数据写入文件
                        fos.write(pcm_data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                write_finish_flag = true;
            }
        }

        //释放资源
        private void release() {
            if (track != null) {
                track.stop();
                track.release();
                track = null;
            }

            if (mAudioClient6801 != null) {
                mAudioClient6801.socketStop();
            }
            if (DEBUG) {
                Log.e(TAG, "audio render thread quit");
            }
        }
    }

    //控制音量
    public void controlVolume() {
        if (agcUtils != null) {
            agcUtils.setAgcConfig(0, VALUE_VOLUME * 3, 1).prepare();
        }
        if (sp_util != null) {
            sp_util.saveVolume(VALUE_VOLUME);
        }
    }

    public void start() {
        if (mDecodeOpusEncodeAacThread == null) {
            mDecodeOpusEncodeAacThread = new DecodeOpusEncodeAacThread();
            mDecodeOpusEncodeAacThread.setRunning(true);
            mDecodeOpusEncodeAacThread.start();
        }
    }

    public void stop() {
        if (mDecodeOpusEncodeAacThread != null) {
            mDecodeOpusEncodeAacThread.interrupt();
            mDecodeOpusEncodeAacThread.setRunning(false);
            mDecodeOpusEncodeAacThread = null;
        }
    }

    //开始录音
    public void startRecord(String name) {

        RECORD_FLAG = true;
        filePath = name;

        File audioFile = new File(filePath);
        if (audioFile.exists()) {
            audioFile.delete();
        }

        try {
            audioFile.createNewFile();
            fos = new FileOutputStream(audioFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int audioTimestampNum = 0;

    //停止录音
    public void stopRecord() {

        if (DEBUG) {
            Log.e(TAG, "音频 停止记录");
        }

        RECORD_FLAG = false;

        audioTimestampNum = 0;

        MyApplication.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                while (!write_finish_flag) {

                }
                try {
                    if (fos != null) {
                        fos.flush();
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
