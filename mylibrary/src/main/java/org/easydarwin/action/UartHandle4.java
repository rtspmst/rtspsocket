package org.easydarwin.action;


import android.util.Log;

import org.easydarwin.CV;
import org.easydarwin.LanguageTr;
import org.easydarwin.MyToastUtils;
import org.easydarwin.SharedPreferencesUtil;
import org.easydarwin.SingletonInternalClass;
import org.easydarwin.SocketDataCallback;
import org.easydarwin.SocketStateCallback;
import org.easydarwin.client.UartClient4;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * serial connection
 * 6804
 * Obtain battery information
 * Obtain battery charging status
 * Get Distance
 * Obtain the intensity of the light wave signal
 */
public class UartHandle4 implements SocketDataCallback, SocketStateCallback {

    private static final String TAG = "UART";
    private static final boolean DEBUG = false;

    private UartClient4 mClientUart;
    private Worker mWorker;//A thread that constantly retrieves data from the queue
    private LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();

    private Callback mCallback;
    public static boolean socketIsBroken = true;


    public UartHandle4() {

        mClientUart = new UartClient4(this, this);

    }

    @Override
    public void onReceiveData(byte[] data) {

        //Received data

        try {

            //Continuously receiving data and saving it in queue

            queue.put(data);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSocketState(byte state) {
        switch (state) {
            case CV.SOCKET_CONNECT_SUCCESS:

                socketIsBroken = false;

                start();

                //set volume
                setVolume(new SharedPreferencesUtil().getTx2spkVolume());

                //Switch to normal mode
                switchNormalMode();

                //Battery voltage ， Battery information
                sendWriteCmd_WithAttrID(CV.BATTERY_AND_RSSI_VOLTAGE);

                //distance
                sendWriteCmd_WithAttrID(CV.MEASURE_DISTANCE);

                //Cancel Enable
                sendWriteCmd_WithAttrID(CV.ASDF2);

                Log.e(TAG, "Socket connection successful");

                break;
            case CV.SOCKET_CONNECT_BROKEN:

                socketIsBroken = true;

                stop();

                //prompt
                MyToastUtils.showToast(CV.TOAST_TAG3, LanguageTr.CONTROL_BOARD_CONNECTION_FAILED);

                break;
            default:
                break;
        }
    }

    public interface Callback {
        void UartDataReadyOn(int data);
    }

    public void setmCallback(Callback mCallback) {
        this.mCallback = mCallback;
    }

    private void start() {
        if (mWorker == null) {
            mWorker = new Worker();
            mWorker.setRunning(true);
            mWorker.start();
        }
    }

    public void stop() {
        if (mWorker != null) {
            mWorker.interrupt();
            mWorker.setRunning(false);
            mWorker = null;
        }
    }

    public synchronized void sendWriteCmd_WithAttrIDAndData(final byte[] send_data, final int dataLength) {
        if (socketIsBroken) {
//            if(DEBUG)Log.w(TAG," connect is broken!");
            return;
        }
        int i = 5;
        int length = dataLength + CV.PROTOCOL_FIXED_LENGTH;
        final byte[] bytes = new byte[length];

        bytes[0] = 0x5A;       // first
        bytes[1] = (byte) 0xA5; // second
        bytes[2] = (byte) length;// length
        bytes[3] = CV.WRITE;  // cmd
        bytes[4] = CV.ENDPOINT;

        for (byte b : send_data) {
            bytes[i++] = b;
        }

        System.arraycopy(send_data, 0, bytes, 5, dataLength);

        // check sum
        bytes[length - 1] = get_check_sum_byte(bytes, 0, length - 1);

        SingletonInternalClass.getInstance().getSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mClientUart.sendBytes(bytes);
            }
        });
    }

    public synchronized void sendWriteCmd_WithAttrID(byte attrID) {
        if (socketIsBroken) {
            if (DEBUG) {
                Log.e(TAG, "Connection disconnected! == " + attrID);
            }
            return;
        }

        int length = 0x02 + CV.PROTOCOL_FIXED_LENGTH;
        final byte[] bytes = new byte[length];

        bytes[0] = 0x5A;// first
        bytes[1] = (byte) 0xA5;  // second
        bytes[2] = (byte) length; // length
        bytes[3] = CV.WRITE; // cmd
        bytes[4] = CV.ENDPOINT; // endpoint

        if (CV.AUTO_FOCUS_IN == attrID) {

            //Turn on autofocus
            bytes[5] = 0x58;
            bytes[6] = 0x05;

        } else if (CV.AUTO_FOCUS_OUT == attrID) {

            //Determine whether to enter autofocus mode based on this attribute value
            //How many seconds do the next representative need to detect it
            bytes[5] = 0x58;
            bytes[6] = 0x00;
            //Turn off autofocus
            Log.e(TAG, "Turn off autofocus " + Arrays.toString(bytes));
        } else {

            // attribute ID
            bytes[5] = attrID;
            // data length
            bytes[6] = 0x00;

        }

        // check sum
        bytes[7] = get_check_sum_byte(bytes, 0, 7);

        SingletonInternalClass.getInstance().getSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {

                mClientUart.sendBytes(bytes);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private byte get_check_sum_byte(byte[] pack, int offset, int pack_len) {

        int i;
        byte check_sum = 0;

        for (i = 0; i < pack_len; i++) {
            check_sum += pack[i + offset];
        }

        return check_sum;
    }

    private class Worker extends Thread {
        private boolean isRunning;
        private byte[] frame;

        void setRunning(boolean running) {
            isRunning = running;
        }

        @Override
        public void run() {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {

                    //Continuously fetching data from queue
                    frame = queue.take();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (frame != null) {
                    for (int i = 0; i < frame.length; i++) {

                        mCallback.UartDataReadyOn(frame[i] & 0x00FF);

                    }
                }
            }
            mClientUart.socketStop();
        }
    }

    // 5A A5 0A 02 01 71 02 00 00 DD； Normal mode
    public void switchNormalMode() {
        //Mode switching
        byte[] data;
        data = new byte[4];
        data[0] = 0x71;
        data[1] = 0x02;
        data[2] = 0x00;
        data[3] = 0x00;
        sendWriteCmd_WithAttrIDAndData(data, data.length);
    }


    // Volume adjustment control
    public void setVolume(int Tx2spk_CurrentVolume) {

        int value = (Tx2spk_CurrentVolume * 10 / 50);
        int number = (int) (3.84 * value + 0.5);//01 28

        byte[] data;
        data = new byte[4];
        data[0] = 0x75;
        data[1] = 0x02;
        data[2] = (byte) ((number & 0xFF00) >> 8);
        data[3] = (byte) (number & 0x00FF);

        sendWriteCmd_WithAttrIDAndData(data, data.length);
    }

}
