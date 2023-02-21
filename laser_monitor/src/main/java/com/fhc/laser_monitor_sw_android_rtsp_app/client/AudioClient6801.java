package com.fhc.laser_monitor_sw_android_rtsp_app.client;

import android.util.Log;

import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;
import com.fhc.laser_monitor_sw_android_rtsp_app.SocketDataCallback;
import com.fhc.laser_monitor_sw_android_rtsp_app.SocketStateCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

//准星连接
public class AudioClient6801 {

    private final boolean DEBUG = false;
    private final String TAG = "Client";
    private final int CONNECT_TIMEOUT = 3 * 1000;
    private String ip;
    private int port;
    private Socket mSocket = null;
    private InputStream input_s;//收到的数据
    private OutputStream output_s;//要发送的数据
    private SocketDataCallback mSocketDataCallback;
    private SocketStateCallback mSocketStateCallback;
    private ReceiveThread receiveThread;
    byte[] buffer = new byte[4096 * 1024];
    private HeartbateThread heartbateThread;

    private volatile boolean socketStatus = true;

    public AudioClient6801(String ip, int port, SocketDataCallback socketDataCallback, SocketStateCallback socketStateCallback) {
        this.ip = ip;
        this.port = port;
        mSocketDataCallback = socketDataCallback;
        mSocketStateCallback = socketStateCallback;

        //开始心跳
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

                if (mSocket == null || input_s == null) {

                    connect();

                }

                try {
                    Thread.sleep(3500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Log.e(TAG, "Heartbeat Running 心跳运行");
            }
//            Log.e("HeartbateThread  心跳线程 ", "exit 退出");
        }
    }

    private boolean connect() {
        try {
            mSocket = new Socket();
            mSocket.connect(new InetSocketAddress(ip, port), CONNECT_TIMEOUT);//创建一个流套接字并将其连接到指定主机上的指定端口号（就是用来连接到host主机的port端口的）
            input_s = mSocket.getInputStream();//拿到此套接字的输入流，收到的数据就在这里
            output_s = mSocket.getOutputStream();//返回此套接字的输出流。 要发送的数据放到这里|
            mSocket.setKeepAlive(true);//设置保持活跃1
            mSocket.setTcpNoDelay(true);//设置无延时1
            mSocketStateCallback.onSocketState((byte) 0x01);

            start();

        } catch (IOException e) {
            e.printStackTrace();
            mSocket = null;
            // 如果连接错误在一开始，所有的工作都不会开始，这里只是通知套接字错误
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

    //接收线程
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

//            Log.e(TAG, "读取线程退出read thread quit");
        }
    }

    // active stop
    public void socketStop() {

        stopSendRevThread();

        MyApplication.getThreadPool().execute(new Runnable() {
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

                if (DEBUG) {
                    Log.d(TAG, "release resource");
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
            //警告：请勿将os.write（data）放入线程中，否则发送长度和发送数据的顺序不正确

            output_s.write(data);
            output_s.flush();

            // 写成功，所以清除writeFailCnt  write success, so clear the writeFailCnt
            if (DEBUG) {
                Log.d(TAG, "写成功，所以清除");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /*
     * Use a fixed-length array,
     * 使用固定长度的数组
     * because make a new array every time will result in OOM
     * 因为每次创建一个新数组都会导致OOM
     */
    private byte[] readBytes(int length) {
        if (mSocket == null || input_s == null) {
            return null;
        }
        int read = 0;
        while (read < length) {
            int cur = -1;
            try {
                //Math.min 返回最小的那个数字,其中比较的参数可以任意个
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
        //将一个原始的数组original，从下标from开始复制，复制到上标to，生成一个新的数组。
        return Arrays.copyOfRange(buffer, 0, length);
    }

    /**
     * 判断是否断开连接，断开返回true,没有返回false
     *
     * @param socket
     * @return
     */
    public Boolean isServerClose(Socket socket) {
        try {
            socket.sendUrgentData(0xFF);//发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
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
