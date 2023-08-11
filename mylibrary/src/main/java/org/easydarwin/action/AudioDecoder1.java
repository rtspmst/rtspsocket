package org.easydarwin.action;


import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.wzc.agc.AgcUtils;

import org.easydarwin.ByteConvertUtil;
import org.easydarwin.CV;
import org.easydarwin.LanguageTr;
import org.easydarwin.MyToastUtils;
import org.easydarwin.SharedPreferencesUtil;
import org.easydarwin.SingletonInternalClass;
import org.easydarwin.SocketDataCallback;
import org.easydarwin.SocketStateCallback;
import org.easydarwin.client.AudioClient1;
import org.easydarwin.fragment.BasePlayFragment;
import org.easydarwin.opus.opus.MyOpusUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

//Audio decoder
public class AudioDecoder1 implements SocketDataCallback, SocketStateCallback {
    private static final String TAG = "Audio";
    private static final boolean DEBUG = false;
    public static byte AUDIO_CONNECT = 0;
    private BasePlayFragment fragment;
    private AudioClient1 mAudioClient1;
    private DecodeOpusEncodeAacThread mDecodeOpusEncodeAacThread;
    private String filePath;
    private FileOutputStream fos;
    private final AgcUtils agcUtils;
    private final SharedPreferencesUtil sp_util;

    //Set Pause
    public void setPause(byte pause) {
        isPause = pause;
    }

    //Local cache queue
    private LinkedBlockingQueue<byte[]> audioOpusQueue = new LinkedBlockingQueue();
    //Volume value
    public static int VALUE_VOLUME = 0;
    private volatile boolean RECORD_FLAG = false;
    public static byte AUDIO_PAUSE = 0x01;
    public static byte AUDIO_START = 0x02;
    private static byte isPause = AUDIO_START;
    private volatile boolean write_finish_flag = false;
    private AudioManager audioManager;
    public MyOpusUtils opusUtils;
    private long decoder;

    //Audio decoder
    public AudioDecoder1(BasePlayFragment fragment, AudioManager audioManager) {

        agcUtils = new AgcUtils();

        sp_util = new SharedPreferencesUtil();

        VALUE_VOLUME = sp_util.getVolume();

        agcUtils.setAgcConfig(0, VALUE_VOLUME * 3, 1).prepare();

        mAudioClient1 = new AudioClient1(this, this);

        this.fragment = fragment;
        this.audioManager = audioManager;
        this.opusUtils = new MyOpusUtils();

        Log.e(TAG, "AudioDecoder1: ============ 11111111111111  " );
//        opusUtils.getStr();
    }

    @Override
    public void onReceiveData(byte[] data) {

        try {
            if (isPause == AUDIO_START) {
                //put()Produce data into the queue，When the queue is full, the thread blocks
                //Received audio data ，This place will constantly receive data
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

                //Socket connection successful
                start();

                AUDIO_CONNECT = 1;

                if (DEBUG) {
                    Log.e(TAG, "Socket connection successful");
                }

                break;
            case 0x02:

                //The socket connection is damaged！
                stop();

                AUDIO_CONNECT = 0;
                Log.e(TAG, "AudioDecoder1: ============ 55555555555555  " );
                //prompt
                MyToastUtils.showToast(CV.TOAST_TAG1, LanguageTr.AUDIO_CONNECTION_FAILED);

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
        private AudioTrack track;
        private byte[] frame;

        boolean prepare() {

            //Set thread priority
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            /**
             * AudioManager
             * AudioFormat.CHANNEL_OUT_MONO：Output mono audio data
             * AudioFormat.CHANNEL_OUT_STEREO：Output dual channel audio data
             */
            int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

            //Audio Properties
            AudioAttributes audioAttributes = new AudioAttributes
                    .Builder()
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build();

            //AudioFormat.CHANNEL_OUT_MONO Monophonic channel
            AudioFormat audioFormat = new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO)
                    .build();

            int sessionId = audioManager.generateAudioSessionId();

            track = new AudioTrack(audioAttributes, audioFormat, bufferSize, AudioTrack.MODE_STREAM, sessionId);

            //Call C to create decoder
            decoder = opusUtils.createDecoder(48000, NUM_CHANNELS);

            // play audio
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
                    Log.w(TAG, "Decoder initialization failed");
                }
                isRunning = false;
            }
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                decodeOpusToPcm();
            }
            release();
        }

        //Decoding Opus to Pcm
        private void decodeOpusToPcm() {

            boolean isEOS = false;


            /* des: Decoding Opus data into PCM
             *  par: decoded It is the decoded size（short）
             */
            short[] decodeBufferArray = new short[MAX_FRAME_SIZE];
            short[] changeTotalCache = new short[MAX_FRAME_SIZE];

            short[] data = new short[80];
            short[] outData = new short[80];

            while (!isEOS) {// Determine if it is the end of the stream

                try {
                    frame = audioOpusQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isEOS = true;
                }


                int size = opusUtils.decode(decoder, frame, decodeBufferArray);

                byte[] pcm_data = ByteConvertUtil.ShortArraytoByteArray(decodeBufferArray, size * NUM_CHANNELS);

                //Play sound
                if (size > 0) {

                    if (VALUE_VOLUME == 0) {

                        // start record, encode aac and write to file
                        startRecording(pcm_data, false);

                    } else {

                        //byte to short
                        short[] shorts = ByteConvertUtil.byteArrayToShortArray(pcm_data);

                        for (int i = 0; i < 12; i++) {

                            System.arraycopy(shorts, i * data.length, data, 0, data.length);

                            int status = agcUtils.agcProcess(data, 0, 80, outData, 0, 0, 0, 0);

                            //outData Assemble to changeTotalCache
                            System.arraycopy(outData, 0, changeTotalCache, i * outData.length, outData.length);
                        }

                        // shot >> byte
                        byte[] pcm_data1 = ByteConvertUtil.ShortArraytoByteArray(changeTotalCache, size * NUM_CHANNELS);

                        // start record, encode aac and write to file
                        startRecording(pcm_data1, true);
                    }
                }
            }
        }


        //  start record, encode aac and write to file
        private void startRecording(byte[] pcm_data, boolean muteFalse) {

            if (muteFalse) {
                track.write(pcm_data, 0, pcm_data.length);
            }

            if (RECORD_FLAG) {
                write_finish_flag = false;
                //Transferred to fragment
                fragment.pumpPCM(pcm_data, pcm_data.length, (long) (audioTimestampNum++ * (pcm_data.length / 4 * 1000.0 / SAMPLE_RATE)));
                try {
                    if (fos != null) {
                        // Write PCM data to a file
                        fos.write(pcm_data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                write_finish_flag = true;
            }
        }

        //Release resources
        private void release() {
            if (track != null) {
                track.stop();
                track.release();
                track = null;
            }

            if (mAudioClient1 != null) {
                mAudioClient1.socketStop();
            }
            if (DEBUG) {
                Log.e(TAG, "audio render thread quit");
            }
        }
    }

    //Control volume
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

    //Start recording
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

    //Stop recording
    public void stopRecord() {

        if (DEBUG) {
            Log.e(TAG, "Audio stop recording");
        }

        RECORD_FLAG = false;

        audioTimestampNum = 0;
        SingletonInternalClass.getInstance().getThreadPool().execute(new Runnable() {
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
