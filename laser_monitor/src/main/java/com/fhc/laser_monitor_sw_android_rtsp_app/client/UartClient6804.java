package com.fhc.laser_monitor_sw_android_rtsp_app.client;

import android.util.Log;

import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;
import com.fhc.laser_monitor_sw_android_rtsp_app.SocketDataCallback;
import com.fhc.laser_monitor_sw_android_rtsp_app.SocketStateCallback;
import com.fhc.laser_monitor_sw_android_rtsp_app.action.AudioDecoder;
import com.fhc.laser_monitor_sw_android_rtsp_app.activity.MainActivity;
import com.fhc.laser_monitor_sw_android_rtsp_app.fragment.PlayFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

//透传连接  上传数据 下发指令
public class UartClient6804 {

    private final boolean DEBUG = true;
    private final String TAG = "ClientUart";
    private final int CONNECT_TIMEOUT = 3 * 1000;
    private String ip;
    private int port;
    private Socket mSocket = null;
    private InputStream input_s;
    private OutputStream output_s;
    private SocketDataCallback mSocketDataCallback;
    private SocketStateCallback mSocketStateCallback;
    //接收线程
    private ReceiveThread receiveThread;
    byte[] buffer = new byte[1024 * 1024];
    private HeartbateThread heartbateThread;
    public static byte TRANSPARENT_MESSAGE = -22;

    public UartClient6804(String ip, int port, SocketDataCallback socketDataCallback, SocketStateCallback socketStateCallback) {
        this.ip = ip;
        this.port = port;
        mSocketDataCallback = socketDataCallback;
        mSocketStateCallback = socketStateCallback;

        startHeartbate();
    }

    private void startHeartbate() {
        // start reconnect thread
        //开始重新连接线程
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
//                    Log.e(TAG, "SOCKET Running 套接字运行" + e.getMessage());
                }
//                Log.e(TAG, "Heartbeat Running 心跳运行");
            }
            Log.e("HeartbateThread 心跳线程 ", "exit 退出");
        }
    }

    private boolean connect() {
        try {
            mSocket = new Socket();
            mSocket.connect(new InetSocketAddress(ip, port), CONNECT_TIMEOUT);
            input_s = mSocket.getInputStream();//拿到此套接字的输入流，收到的数据就在这里
            output_s = mSocket.getOutputStream();//返回此套接字的输出流。 要发送的数据放到这里|
            mSocket.setKeepAlive(true);//设置保持活跃1
            mSocket.setTcpNoDelay(true);//设置无延时1
            mSocketStateCallback.onSocketState((byte) 0x01);

            //启动接收线程
            start();

        } catch (IOException e) {
            e.printStackTrace();
            mSocket = null;
            // if connect error in the beginning, all work will be not start, here just notify socket error
            //如果连接错误在一开始，所有的工作都不会开始，这里只是通知套接字错误
            mSocketStateCallback.onSocketState((byte) 0x02);
            Log.e(TAG, "循环———套接字连接错误272  " + e.getMessage());
            return false;
        }
        return true;
    }

    //如果开始时出现连接错误，则receiveThread将结束（而循环无法运行）//并且heartbeatThread将不会启动
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

                //判断是否断开连接，断开返回true,没有返回false
                if (isServerClose(mSocket)) {
                    //音频
                    AudioDecoder.AUDIO_CONNECT = 0;
                    //电量
                    MainActivity.voltage = 0f;
                    //是否充电中
                    MainActivity.CHARGING = false;
                    //测距机
                    MainActivity.DISTANCE_VALUE = 0;
                    //透传消息
                    TRANSPARENT_MESSAGE = 0;
                    //视频
                    PlayFragment.DETECT_VIDEO = 0;

                    socketStop();

                } else {

                    TRANSPARENT_MESSAGE = 1;

                    byte[] bytes = readBytes(1);

                    if (bytes != null && bytes.length > 0) {
                        //接收数据
                        mSocketDataCallback.onReceiveData(bytes);
                    }
                }
            }
            Log.e(TAG, "读取线程退出read thread quit");
        }
    }

    // active stop
    public void socketStop() {

        //停止发送修订线程
        stopSendRevThread();

        MyApplication.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                // join(): wait receiveThread and heartbeatThread finish,then release resource
                try {
                    if (receiveThread != null) {
                        //等待该线程终止
                        receiveThread.join();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (DEBUG) {
                    Log.e(TAG, "释放资源 release resource");
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

    //停止发送修订线程
    private void stopSendRevThread() {

        // because have a block IO which read from socket in the receiveThread, so we need close the socket to broken the block IO
        //因为在receiveThread中有一个从套接字读取的块IO，所以我们需要关闭套接字以破坏该块IO
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

//        Log.e(TAG, "Write data == " + Arrays.toString(data));

        if (mSocket == null || output_s == null) {
            return false;
        }
        try {

            //警告：请勿将os.write（data）放入线程中，否则发送长度和发送数据的顺序不正确
            //Warning: don't put os.write(data) in a thread, otherwise send length and send data not order
            output_s.write(data);

            //flush() 方法刷新bai此输出du流并强制将所有缓冲的输出字节被写出
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
}
