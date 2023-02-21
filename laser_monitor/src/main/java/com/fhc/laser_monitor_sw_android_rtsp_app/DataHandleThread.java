package com.fhc.laser_monitor_sw_android_rtsp_app;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.fhc.laser_monitor_sw_android_rtsp_app.action.UartHandle6804;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.CV;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * All control cmd is WRITE
 * 所有控件cmd为WRITE
 * All ACK from device is DATA_UPLOAD
 * 所有来自设备的确认都是数据上传
 * 2018-03-28
 */
//接收上传数据  数据处理线程
public class DataHandleThread extends Thread {

    private static final boolean DEBUG = false;    // TODO set false on release

    // queue
    LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<>();

    private Handler mHandler;

    // data handle
    private int[] data_handle_buf = new int[255];
    private int[] uart_rx_buf = new int[255];
    //被设计用来修饰被不同线程bai访问和修改的变量
    private volatile int rx_index = 0;
    private volatile int offset = 0;
//    private volatile int rx_frame_len = 0;

    //透传连接
    public DataHandleThread(Handler mHandler, UartHandle6804 mUartHandle6804) {

        this.mHandler = mHandler;

        mUartHandle6804.setmCallback(new UartHandle6804.Callback() {
            @Override
            public void UartDataReadyOn(int data) {

                if (queue.remainingCapacity() > 0) {

                    queue.add(data);
                }
            }
        });
    }

    private int i = 0;
    private String rev = "REV: ";
    private long cnt = 0;

    @Override
    public void run() {

        int rx_frame_len;
        int ch = 0;

        while (true) {

//            while (rx_index < 15) {
//                try {
//                    /* block I/O */
////                    if(DEBUG)Log.e("QUEUE_TAKE","queque taking");
//                    ch = queue.take();
////                    if(DEBUG)Log.e("QUEUE_TAKE","queque get datas");
//                    uart_rx_buf[rx_index++] =  ch;
////                    if(DEBUG)Log.e("DataHandle","recv: "+Integer.toHexString(ch));
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
////            }

            while ((rx_index < 255) && (queue.size()) > 0) {

                //弹出队顶元素，队列为空时，返回空
                ch = queue.poll();

                uart_rx_buf[rx_index++] = ch;
            }

            if (rx_index < 6) {
                continue;
            }

            while ((rx_index - offset) >= 6) {
                if (uart_rx_buf[offset + CV.HEAD_FIRST_FIELD] != 0x5A) {
                    offset++;
                    continue;
                }

                if (uart_rx_buf[offset + CV.HEAD_SECOND_FIELD] != 0xA5) {
                    offset++;
                    continue;
                }

                rx_frame_len = uart_rx_buf[offset + CV.FRAME_LENGTH_FIELD];

                if ((rx_frame_len > 249) || (rx_frame_len == 0)) {
                    offset += 2;
                    continue;
                }

                if ((rx_index < (rx_frame_len + offset)) || ((rx_frame_len + offset) < 1)) {
                    break;
                }

                int recvSum = uart_rx_buf[offset + rx_frame_len - 1];
                if (DEBUG) {
                    Log.e("DataHandle", "recvSum: " + Integer.toHexString(recvSum));
                }

                int sum = get_check_sum(uart_rx_buf, offset, (rx_frame_len - 1));
                if (DEBUG) {
                    Log.e("DataHandle", "sum: " + Integer.toHexString(sum));
                }

                if (sum != recvSum) {

                    if (DEBUG) {
                        Log.e("DataHandle", "数据处理 校验和失败");
                    }

                    offset += 2;
                    continue;
                }

                //将a数组 复制给b
                System.arraycopy(uart_rx_buf, offset, data_handle_buf, 0, rx_frame_len);

//                for(i=0;i<rx_frame_len;i++)
//                {
//                    rev += Integer.toHexString(data_handle_buf[i])+",";
//                }
//
//                Log.e("Rev",rev+": "+(cnt++));
//
//                rev = "REV: ";

                /* handle cmd */
                cmd_handle();

                if (DEBUG) {
                    Log.e("DataHandle", "recv: " + "一帧解析one frame parse over");
                }

                offset += rx_frame_len;

            }//end while

            rx_index -= offset;

            if (rx_index > 0) {
                if (DEBUG) {
                    Log.e("rx_index", "" + rx_index);
                }
                System.arraycopy(uart_rx_buf, offset, uart_rx_buf, 0, rx_index);
            }

            offset = 0;
        }
    }

    private void cmd_handle() {

//        Log.e("TAG", "run:有没有去发送指令----第2个 == " + data_handle_buf[CV.CMD_FIELD] );

        switch (data_handle_buf[CV.CMD_FIELD]) {

            case CV.DATA_UPLODA:

                // 上传数据句柄upload data handle
                upload_data_handle();

                break;
            default:
                break;
        }
    }

    // 上传数据句柄upload data handle
    private void upload_data_handle() {

        Message msg = mHandler.obtainMessage();

        switch (data_handle_buf[CV.ENDPOINT_FIELD]) {
            case CV.ENDPOINT:

                upload_cmd_handle_with_attr_id();

                break;
            case CV.HEATBEAT_ENDPOINT:

                if (DEBUG) {
                    Log.e("暂无 DataHandle", "HEARTBEAT");
                }
                msg.what = CV.MSG_HEARTBEAT;
                mHandler.sendMessage(msg);
                break;
            default:
                break;
        }
    }

    private void upload_cmd_handle_with_attr_id() {

        Message msg = mHandler.obtainMessage();

        switch (data_handle_buf[CV.FRAME_DATA_START_FIELD]) {

            case CV.VIEW:

                if (DEBUG) {
                    Log.e("暂无 DataHandle", "Switch View ACk");
                }
                msg.what = CV.MSG_UPDATE_VIEW;
                msg.arg1 = data_handle_buf[CV.FRAME_DATA_START_FIELD + 3];
                mHandler.sendMessage(msg);

                break;
            case CV.MEASURE_DISTANCE:

                //收到 测量距离
                int length = data_handle_buf[CV.FRAME_DATA_START_FIELD + 1];
                int[] tempbuf = new int[10];

                System.arraycopy(data_handle_buf, CV.FRAME_DATA_START_FIELD + 2, tempbuf, 0, length);

                msg.what = CV.MSG_UPDATE_DISTANCE_VALUE;
                msg.obj = tempbuf;
                mHandler.sendMessage(msg);

                break;
            case CV.SHOW_MOTOR_STEPS:

                //步进电机前进步数
                msg.what = CV.SHOW_MOTOR_STEPS;
                msg.obj = "左：" + Integer.toHexString(data_handle_buf[7]) + "," + Integer.toHexString(data_handle_buf[8]) + " ；右：" + Integer.toHexString(data_handle_buf[9]) + "," + Integer.toHexString(data_handle_buf[10]);
                //十六进制转为十进制
//                msg.obj = "左：" + (((data_handle_buf[CV.FRAME_DATA_START_FIELD + 2] << 8) & 0x0000FF00) | data_handle_buf[CV.FRAME_DATA_START_FIELD + 3]) + " ；右：" + (((data_handle_buf[CV.FRAME_DATA_START_FIELD + 4] << 8) & 0x0000FF00) | data_handle_buf[CV.FRAME_DATA_START_FIELD + 5]);
                mHandler.sendMessage(msg);

                break;
            case CV.JZ1_LEVEL:

//                msg.what = CV.MSG_UPDATE_NOISE_LEVEL;
//                msg.arg1 = data_handle_buf[CV.FRAME_DATA_START_FIELD+2];
//                msg.arg2 = CV.JZ1;
//                mHandler.sendMessage(msg);
//                int cnt = (data_handle_buf[CV.FRAME_DATA_START_FIELD+2]&0xFF) | ((data_handle_buf[CV.FRAME_DATA_START_FIELD+3]<<8)&0x00FF00);
//                Log.e("CNT",((data_handle_buf[CV.FRAME_DATA_START_FIELD+1]&0xFF) | ((data_handle_buf[CV.FRAME_DATA_START_FIELD+2]<<8)&0x00FF00))+"");
//                for(i=0;i<(data_handle_buf[CV.FRAME_DATA_START_FIELD+1]&0xFF);i++)
//                {
//                    rev += Integer.toHexString(data_handle_buf[CV.FRAME_DATA_START_FIELD+2+i])+",";
//                }
//                Log.e("Recv",rev+"Rev: "+(cnt++));
//                rev = "";

                break;
            case CV.JZ2_LEVEL:

                //降噪等级
                msg.what = CV.MSG_UPDATE_NOISE_LEVEL;
                msg.arg1 = data_handle_buf[CV.FRAME_DATA_START_FIELD + 2];
                msg.arg2 = CV.JZ2;
                mHandler.sendMessage(msg);

                break;
            case CV.SET_SOUND_SOURCE_1:
                //MainActivity 未接受
                msg.what = CV.MSG_M62429_1;
                msg.arg1 = data_handle_buf[CV.FRAME_DATA_START_FIELD + 2];
                mHandler.sendMessage(msg);

                break;
            case CV.SET_SOUND_SOURCE_2:
                //MainActivity 未接受
                msg.what = CV.MSG_M62429_2;
                msg.arg1 = data_handle_buf[CV.FRAME_DATA_START_FIELD + 2];
                mHandler.sendMessage(msg);

                break;
            case CV.BATTERY_AND_RSSI_VOLTAGE:

                //收到 电量信息
                msg.what = CV.MSG_BATTERY_VOLTAGE;
                msg.arg1 = ((data_handle_buf[CV.FRAME_DATA_START_FIELD + 3] << 8) & 0x0000FF00) | data_handle_buf[CV.FRAME_DATA_START_FIELD + 2];
                mHandler.sendMessage(msg);

                Message msg_huibo = mHandler.obtainMessage();

                //收到 光回波信号
                msg_huibo.what = CV.MSG_HUIBO_POWER;

                msg_huibo.arg1 = ((data_handle_buf[CV.FRAME_DATA_START_FIELD + 5] << 8) & 0x0000FF00) | data_handle_buf[CV.FRAME_DATA_START_FIELD + 4];

//                // rssi2
//                msg_huibo.arg2 = ((data_handle_buf[CV.FRAME_DATA_START_FIELD+7]<<8)&0x0000FF00) | data_handle_buf[CV.FRAME_DATA_START_FIELD+6];

                mHandler.sendMessage(msg_huibo);

                break;
            case CV.CURRENT_JZ:

                //左右电机 位置 展示降噪几
                msg.what = CV.MSG_CURRENT_JZ;
                msg.arg1 = data_handle_buf[CV.FRAME_DATA_START_FIELD + 2];
                mHandler.sendMessage(msg);

                break;
            case CV.CURRENT_MOTOR:

                //左右电机相关
                msg.what = CV.MSG_CURRENT_MOTOR;
                msg.arg1 = data_handle_buf[CV.FRAME_DATA_START_FIELD + 2];
                mHandler.sendMessage(msg);

                break;
            case CV.STOP_TWO_CH_AUTO_FOCUS:

                //停止两个通道自动对焦
                msg.what = CV.MSG_STOP_TWO_CH_AUTO_FOCUS;
                mHandler.sendMessage(msg);

                break;
            case CV.ADJUST_AUTO_FOCUS_START:

                //调整自动对焦开始
                msg.what = CV.MSG_ADJUST_AUTO_FOCUS_UPDATE;
                // rssi1
                msg.arg1 = ((data_handle_buf[CV.FRAME_DATA_START_FIELD + 2] << 8) & 0x0000FF00) | data_handle_buf[CV.FRAME_DATA_START_FIELD + 3];
                // rssi2
                msg.arg2 = ((data_handle_buf[CV.FRAME_DATA_START_FIELD + 4] << 8) & 0x0000FF00) | data_handle_buf[CV.FRAME_DATA_START_FIELD + 5];
                mHandler.sendMessage(msg);

                break;
            case CV.CHARGE_STATUS:
                msg.what = CV.MSG_CHARGE_STATUS;
                msg.arg1 = data_handle_buf[CV.FRAME_DATA_START_FIELD + 2];
                mHandler.sendMessage(msg);

                break;
            case CV.M3_STOP:
                if (DEBUG) {
                    Log.e("M3_STOP", "Done");
                }

                break;
            case CV.M4_STOP:
                if (DEBUG) {
                    Log.e("M4_STOP", "Done");
                }
                break;
            case CV.AUTO_FOCUS_BUTTON_STATE:
                //自动对焦按钮停止状态
                msg.what = CV.AUTO_FOCUS_BUTTON_STATE;
                msg.arg1 = data_handle_buf[CV.FRAME_DATA_START_FIELD + 2];
                mHandler.sendMessage(msg);

                break;
            default:
                break;
        }
    }

    private int get_check_sum(int[] pack, int offset, int pack_len) {

        int i;
        int check_sum = 0;

        for (i = 0; i < pack_len; i++) {
            if (DEBUG) {
                Log.e("check sum", "" + Integer.toHexString(pack[i]));
            }
            check_sum += pack[i + offset];
        }

        return check_sum & 0x0FF;
    }
}
