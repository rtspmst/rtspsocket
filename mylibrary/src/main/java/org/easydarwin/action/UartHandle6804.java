package org.easydarwin.action;


import android.util.Log;

import org.easydarwin.CV;
import org.easydarwin.HideyBarUtils;
import org.easydarwin.Language;
import org.easydarwin.MyToastUtils;
import org.easydarwin.SharedPreferencesUtil;
import org.easydarwin.SingletonInternalClass;
import org.easydarwin.SocketDataCallback;
import org.easydarwin.SocketStateCallback;
import org.easydarwin.client.UartClient6804;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 串口连接 透传连接断开
 * 6804
 * 获取电量信息
 * 获取电池充电状态
 * 获取距离
 * 获取光波信号强度
 * 向上	    0x16	单次点击发送一条指令 然后发送停止指令
 * 向下	    0x17	长按没100毫秒发送一次指令 松开发送停止指令
 * 向上向下停止	0x18
 * 向左	    0x19
 * 向右	    0x1A
 * 向左向右停止	0x1B
 * 左电机加号	0x04	单次点击发送一条指令
 * 左电机减号	0x05	长按没100毫秒发送一次指令
 * 右电机加号	0x2E
 * 右电机减号	0x2F
 * 自动对焦	0x02	发送一条
 * 复位	0x06
 */
public class UartHandle6804 implements SocketDataCallback, SocketStateCallback {

    private static final String TAG = "UART";
    private static final boolean DEBUG = false;

    private UartClient6804 mClientUart;
    private Worker mWorker;//不停的从queue取数据的线程
    private LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();

    private Callback mCallback;
    public static boolean socketIsBroken = true;


    public UartHandle6804() {

        mClientUart = new UartClient6804(CV.IP, 6804, this, this);
//        mClient = new ClientUart("192.168.10.6",6804,this,this);
//        mClient = new ClientUart("192.168.10.5",8006,this,this);
    }

    @Override
    public void onReceiveData(byte[] data) {

        //接收到的数据

        try {

            //不停的接收数据 保存在queue

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

                //设置音量
                setVolume(new SharedPreferencesUtil().getTx2spkVolume());

                //切换为正常模式
                switchNormalMode();

                //电池电压 电量信息
                sendWriteCmd_WithAttrID(CV.BATTERY_AND_RSSI_VOLTAGE);

                //测距
                sendWriteCmd_WithAttrID(CV.MEASURE_DISTANCE);

                //取消使能
                sendWriteCmd_WithAttrID(CV.ASDF2);

                Log.e(TAG, "onSocketState: 套接字连接成功！㐊");

                break;
            case CV.SOCKET_CONNECT_BROKEN:

                socketIsBroken = true;

                stop();

                //提示 主控板连接失败，请检查设备连接!
                MyToastUtils.showToast(CV.TOAST_TAG3, Language.CONTROL_BOARD_CONNECTION_FAILED);

                Log.e(TAG, "run:银临 透传连接断开   " + HideyBarUtils.getTime());

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
                Log.e(TAG, "连接断开 !!! == " + attrID);
            }
            return;
        }

        int length = 0x02 + CV.PROTOCOL_FIXED_LENGTH;
        final byte[] bytes = new byte[length];

        bytes[0] = 0x5A;// first
        bytes[1] = (byte) 0xA5;  // second
        bytes[2] = (byte) length; // length
        bytes[3] = CV.WRITE; // cmd
        bytes[4] = CV.ENDPOINT; // endpoint终点

        if (CV.AUTO_FOCUS_IN == attrID) {

            //2022 03 18 添加  根据这个属性值判断是否进入自动对焦模式 下一位代表间隔多少秒去检测一次
            bytes[5] = 0x58;
            bytes[6] = 0x05;
            //打开自动对焦
            Log.e(TAG, "打开自动对焦 " + Arrays.toString(bytes));

        } else if (CV.AUTO_FOCUS_OUT == attrID) {

            //2022 03 18 添加  根据这个属性值判断是否进入自动对焦模式 下一位代表间隔多少秒去检测一次
            bytes[5] = 0x58;
            bytes[6] = 0x00;
            //关闭自动对焦
            Log.e(TAG, "关闭自动对焦 " + Arrays.toString(bytes));
        } else {

            // 属性 attribute ID
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

//        Log.e(TAG, "get_check_sum_byte: " + check_sum);
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

                    //不停的从queue取数据
                    frame = queue.take();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (frame != null) {
                    for (int i = 0; i < frame.length; i++) {

                        mCallback.UartDataReadyOn(frame[i] & 0x00FF);

//                        Log.e(TAG, "UartHandle run: " + (frame[i] & 0x00FF));
                    }
                }
            }
            mClientUart.socketStop();
        }
    }

    //切换为正常模式     // 5A A5 0A 02 01 71 02 00 00 DD； 正常模式
    public void switchNormalMode() {
        //模式切换
        byte[] data;
        data = new byte[4];
        data[0] = 0x71;
        data[1] = 0x02;
        data[2] = 0x00;
        data[3] = 0x00;
        sendWriteCmd_WithAttrIDAndData(data, data.length);
    }

    //切换为晃动模式     5A A5 0A 02 01 72 02 00 00 DD：  晃动模式
    public void switchWobbleMode() {
        //模式切换
        byte[] data;
        data = new byte[4];
        data[0] = 0x72;
        data[1] = 0x02;
        data[2] = 0x00;
        data[3] = 0x00;
        sendWriteCmd_WithAttrIDAndData(data, data.length);
    }

    //"AmpPwr":"low/high"  放大器功率切换
    //高
    public void switchAmpPwrHigh() {
        byte[] data;
        data = new byte[4];
        data[0] = 0x73;
        data[1] = 0x02;
        data[2] = 0x00;
        data[3] = 0x00;
        sendWriteCmd_WithAttrIDAndData(data, data.length);
    }

    //"AmpPwr":"low/high"  放大器功率切换
    //低
    public void switchAmpPwrLow() {
        byte[] data;
        data = new byte[4];
        data[0] = 0x74;
        data[1] = 0x02;
        data[2] = 0x00;
        data[3] = 0x00;
        sendWriteCmd_WithAttrIDAndData(data, data.length);
    }

    //  步进电机🔒控制 暂未用到
    public void protectStepperSendCmd(int stride) {
        byte[] data;
        data = new byte[4];
        data[0] = CV.PROTECT_STEPPER_MOVE;
        data[1] = 2;
        data[2] = (byte) ((stride & 0xFF00) >> 8);
        data[3] = (byte) (stride & 0x00FF);

        sendWriteCmd_WithAttrIDAndData(data, 4);
    }

    //   音量调节控制
    public void setVolume(int Tx2spk_CurrentVolume) {

        Log.e(TAG, "Tx2spk_CurrentVolume:  " + Tx2spk_CurrentVolume);

        int value = (Tx2spk_CurrentVolume * 10 / 50);
        int number = (int) (3.84 * value + 0.5);//2.96怎么来的 01 28

        byte[] data;
        data = new byte[4];
        data[0] = 0x75;
        data[1] = 0x02;
        data[2] = (byte) ((number & 0xFF00) >> 8);
        data[3] = (byte) (number & 0x00FF);

        sendWriteCmd_WithAttrIDAndData(data, data.length);
    }

}
