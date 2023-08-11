package org.easydarwin.client;

import android.util.Log;

import com.bk.webrtc.JniCallNative;

import org.easydarwin.SingletonInternalClass;
import org.easydarwin.SocketDataCallback;
import org.easydarwin.SocketStateCallback;
import org.easydarwin.action.AudioDecoder1;
import org.easydarwin.action.UartHandle4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

//Transparent connection
public class UartClient4 {

    private final boolean DEBUG = true;
    private final String TAG = "ClientUart";
    private final int CONNECT_TIMEOUT = 3 * 1000;

    private Socket mSocket = null;
    private InputStream input_s;
    private OutputStream output_s;
    private SocketDataCallback mSocketDataCallback;
    private SocketStateCallback mSocketStateCallback;
    private ReceiveThread receiveThread;
    byte[] buffer = new byte[1024 * 1024];
    private HeartbateThread heartbateThread;
    public static byte TRANSPARENT_MESSAGE = -22;

    public UartClient4(UartHandle4 socketDataCallback, UartHandle4 socketStateCallback) {

        mSocketDataCallback = socketDataCallback;
        mSocketStateCallback = socketStateCallback;

        startHeartbate();
    }

    private void startHeartbate() {
        // start reconnect thread
        heartbateThread = new HeartbateThread();
        heartbateThread.setIsRunning(true);
        heartbateThread.start();

    }

    private class HeartbateThread extends Thread {
        private boolean isRunning = false;

        public void setIsRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }

        @Override
        public void run() {
            while (isRunning) {

                if (mSocket == null || output_s == null) {

                    connect();

                }

                try {
                    Thread.sleep(3200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
//                    Log.e(TAG, "SOCKET Running " + e.getMessage());
                }
//                Log.e(TAG, "Heartbeat Running ");
            }
            Log.e("HeartbateThread  ", "exit ");
        }
    }

    private boolean connect() {
        try {
            mSocket = new Socket();

            mSocket.connect(new InetSocketAddress(JniCallNative.getInstance().jniGetIP(), JniCallNative.getInstance().jniGetUartHandle4()), CONNECT_TIMEOUT);
            input_s = mSocket.getInputStream();
            output_s = mSocket.getOutputStream();
            mSocket.setKeepAlive(true);//Set to remain active
            mSocket.setTcpNoDelay(true);//Set no delay
            mSocketStateCallback.onSocketState((byte) 0x01);

            start();

        } catch (IOException e) {
            e.printStackTrace();
            mSocket = null;
            // if connect error in the beginning, all work will be not start, here just notify socket error
            mSocketStateCallback.onSocketState((byte) 0x02);
            return false;
        }
        return true;
    }

    // if connect error in the beginning, the receiveThread will over(while loop can't run)
    // and the heartbeatThread will be not start
    private void start() {
        receiveThread = new ReceiveThread();
        receiveThread.setIsRunning(true);
        receiveThread.start();
    }

    private class ReceiveThread extends Thread {
        private boolean isRunning = true;

        public void setIsRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }

        @Override
        public void run() {

            while (isRunning) {

                //Determine whether to disconnect，
                if (isServerClose(mSocket)) {
                    //audio frequency
                    AudioDecoder1.AUDIO_CONNECT = 0;

                    socketStop();

                } else {

                    TRANSPARENT_MESSAGE = 1;

                    byte[] bytes = readBytes(1);

                    if (bytes != null && bytes.length > 0) {
                        //receive data
                        mSocketDataCallback.onReceiveData(bytes);
                    }
                }
            }
            Log.e(TAG, "read thread quit");
        }
    }

    // active stop
    public void socketStop() {

        //Stop sending revision thread
        stopSendRevThread();

        SingletonInternalClass.getInstance().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                // join(): wait receiveThread and heartbeatThread finish,then release resource
                try {
                    if (receiveThread != null) {
                        //Wait for the thread to terminate
                        receiveThread.join();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (DEBUG) {
                    Log.e(TAG, " release resource");
                }

                try {
                    if (input_s != null) {
                        input_s.close();
                        input_s = null;
                    }
                    if (output_s != null) {
                        output_s.close();
                        output_s = null;
                    }
                    if (mSocket != null) {
                        mSocket.close();
                        mSocket = null;
                    }

                    if (receiveThread != null) {
                        receiveThread = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //Stop sending revision thread
    private void stopSendRevThread() {

        // because have a block IO which read from socket in the receiveThread, so we need close the socket to broken the block IO
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = null;
        }

        // and then set the running flag to false
        if (receiveThread != null) {
            receiveThread.setIsRunning(false);
        }
    }

    public synchronized boolean sendBytes(byte[] data) {

        if (mSocket == null || output_s == null) {
            return false;
        }
        try {

            //Warning: don't put os.write(data) in a thread, otherwise send length and send data not order
            output_s.write(data);

            output_s.flush();

            // write success, so clear the writeFailCnt

            if (DEBUG) {
                Log.d(TAG, "Write Success");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /*
     * Use a fixed-length array,
     * because make a new array every time will result in OOM
     */
    private byte[] readBytes(int length) {
        if (mSocket == null || input_s == null) {
            return null;
        }
        int read = 0;
        while (read < length) {
            int cur = -1;
            try {
                cur = input_s.read(buffer, read, Math.min(1024, length - read));
            } catch (IOException e) {
                e.printStackTrace();
                if (DEBUG) {
                    Log.d(TAG, "read error!");
                }
            }
            if (cur < 0) {
                // if read error,it can't return Arrays.copyOfRange(buffer,0,length),because may be there is OLD data in the buffer
                // so return null
                return null;
            }
            read += cur;
        }

//        Log.d(TAG,"read loop over");
        // because use a global buffer,so may be there is OLD data in the buffer

        // if the length of read is zero, it can't return Arrays.copyOfRange(buffer,0,length),because may be there is OLD data in the buffer
        // please see ReceiveThread#if(length<=0)

        // if read error,it can't return Arrays.copyOfRange(buffer,0,length),because may be there is OLD data in the buffer
        // please see " if (cur < 0) " above
        return Arrays.copyOfRange(buffer, 0, length);
    }

    /**
     * Determine whether to disconnect，
     *
     * @param socket
     * @return
     */
    public Boolean isServerClose(Socket socket) {
        try {
            socket.sendUrgentData(0xFF);//Send 1 byte of emergency data
            return false;
        } catch (Exception se) {
            return true;
        }
    }
}
