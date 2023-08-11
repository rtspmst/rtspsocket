package org.easydarwin.client;

import android.util.Log;

import com.bk.webrtc.JniCallNative;

import org.easydarwin.SingletonInternalClass;
import org.easydarwin.SocketDataCallback;
import org.easydarwin.SocketStateCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

//Quasi star connection
public class JsonClient2 {

    private final boolean DEBUG = false;
    private final String TAG = "Client";
    private final int CONNECT_TIMEOUT = 3 * 1000;

    private Socket mSocket = null;
    private InputStream input_s;//Received data
    private OutputStream output_s;//Data to be sent
    private SocketDataCallback mSocketDataCallback;
    private SocketStateCallback mSocketStateCallback;
    private ReceiveThread receiveThread;
    byte[] buffer = new byte[4096 * 1024];
    private HeartbateThread heartbateThread;

    public JsonClient2(SocketDataCallback socketDataCallback, SocketStateCallback socketStateCallback) {

        mSocketDataCallback = socketDataCallback;
        mSocketStateCallback = socketStateCallback;

        startHeartbate();
    }

    private void startHeartbate() {

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

                if (mSocket == null || input_s == null) {

                    connect();
                }

                try {

                    Thread.sleep(3500);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                Log.d(TAG, "Heartbeat Running ");
            }

        }
    }

    private boolean connect() {
        try {
            mSocket = new Socket();
            mSocket.connect(new InetSocketAddress(JniCallNative.getInstance().jniGetIP(), JniCallNative.getInstance().jniGetJsonHandle2()), CONNECT_TIMEOUT);
            input_s = mSocket.getInputStream();
            output_s = mSocket.getOutputStream();
            mSocket.setKeepAlive(true);//Set to remain active
            mSocket.setTcpNoDelay(true);//Set no delay
            mSocketStateCallback.onSocketState((byte) 0x01);

            start();

            if (DEBUG) {
                Log.e(TAG, "Socket Connect success!");
            }

        } catch (IOException e) {
            e.printStackTrace();
            mSocket = null;
            // if connect error in the beginning, all work will be not start, here just notify socket error
            mSocketStateCallback.onSocketState((byte) 0x02);
            Log.e(TAG, "Socket Connect Error!");
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

    //Receive Thread
    private class ReceiveThread extends Thread {

        private boolean isRunning = true;

        public void setIsRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }

        @Override
        public void run() {

            while (isRunning) {

                if (isServerClose(mSocket)) {

                    socketStop();

                } else {

                    int length = bytesToInt(readBytes(4));

                    byte[] bytes = readBytes(length);

                    if (bytes != null && bytes.length > 0) {

                        mSocketDataCallback.onReceiveData(bytes);

                    }
                }
            }

            socketStop();

            Log.d(TAG, "read thread quit");
        }
    }

    // active stop
    public void socketStop() {

        stopSendRevThread();

        SingletonInternalClass.getInstance().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                // join(): wait receiveThread and heartbeatThread finish,then release resource
                try {
                    if (receiveThread != null) {
                        receiveThread.join();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
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

            // writeFailCnt  write success, so clear the writeFailCnt

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
                //Math.min （Returns the smallest number, where any parameter can be compared）
                cur = input_s.read(buffer, read, Math.min(1024, length - read));

            } catch (IOException e) {
                e.printStackTrace();

                Log.e(TAG, "read error!");
            }
            if (cur < 0) {
                // if read error,it can't return Arrays.copyOfRange(buffer,0,length),because may be there is OLD data in the buffer
                // so return null
                return null;
            }
            read += cur;
        }

        // because use a global buffer,so may be there is OLD data in the buffer

        // if the length of read is zero, it can't return Arrays.copyOfRange(buffer,0,length),because may be there is OLD data in the buffer
        // please see ReceiveThread#if(length<=0)

        // if read error,it can't return Arrays.copyOfRange(buffer,0,length),because may be there is OLD data in the buffer
        // please see " if (cur < 0) " above
        //Generate a new array
        return Arrays.copyOfRange(buffer, 0, length);
    }

    /**
     * Determine whether to disconnect
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

    private int bytesToInt(byte[] bytes) {
        if (bytes != null) {
            if (bytes.length > 3) {
                return (bytes[0] & 0xff) | ((bytes[1] & 0xff) << 8)
                        | ((bytes[2] & 0xff) << 16) | ((bytes[3] & 0xff) << 24);
            }
        }
        return 0;
    }
}
