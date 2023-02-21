package com.fhc.laser_monitor_sw_android_rtsp_app.utils;

public class CV {

    //    public static final String IP = "192.168.1.104";
//    public static final String IP = "10.42.0.1";
    public static final String IP = "192.168.137.100";

    public static final byte IMG_PROCESS_ON = 0x01;
    public static final byte IMG_PROCESS_OFF = 0x02;
    public static final byte ADJUST_VOLUME = 0x03;
    public static final byte START_UPDATE_UI = 0x04;
    public static final byte STOP_UPDATE_UI = 0x05;
    public static final byte SET_CAMERA1_CNETER = 0x06;
    public static final byte SET_CAMERA2_CNETER = 0x07;
    public static final byte SET_DENOISE_METHOD = 0x08;
    public static final byte SET_BEVIS_GRADE = 0x09;
    public static final byte SET_NOISE_VIEW = 0x0A;//降噪等级选择
    public static final byte SET_CAMERA3_CNETER = 0x0B;//11
    public static final byte GET_CAMERA1_CNETER = 0x0C;
    public static final byte GET_CAMERA2_CNETER = 0x0D;//13
    public static final byte GET_CAMERA3_CNETER = 0x0E;
    public static final byte IMG_PROCESS_QUERY = 0x0F;//15
    public static final byte SWITCH_VIEW = 0x10;
    public static final byte SET_WEBRTC_GRADE = 0x11;//17
    public static final byte STRONG_MODE = 0x12;
    public static final byte TX2SPK_VOLUME = 0x13;//设置音量 19
    public static final byte DEMODE = 0x14;
    public static final byte DEMODE_VALUE = 0x15;//(21)
    public static final byte DEMODE_NONE = 0x16;
    public static final byte DEMODE_SBDSRC = 0x17; //降噪12 声源选择 (23)


    // frame field
    public final static byte HEAD_FIRST_FIELD = 0;
    public final static byte HEAD_SECOND_FIELD = 1;
    public final static byte FRAME_LENGTH_FIELD = 2;
    public final static byte CMD_FIELD = 3;
    public final static byte ENDPOINT_FIELD = 4;
    public final static byte FRAME_DATA_START_FIELD = 5;

    // cmd
    public final static byte DATA_UPLODA = 0x05;
    public final static byte WRITE = 0x02;

    public final static int TOAST_TAG1 = 0x01;
    public final static int TOAST_TAG2 = 0x02;
    public final static int TOAST_TAG3 = 0x03;


    public final static byte READ_ACK = 0x01;
    public final static byte WRITE_ACK = 0x03;
    public final static byte WRITE_NON_ACK = 0x04;
    public final static byte DEFAULT_ACK = 0x06;
    public final static byte ERROR = (byte) 0xFF;

    // endpoint
    public final static byte ENDPOINT = 0x01; //1
    public final static byte HEATBEAT_ENDPOINT = 0x02;

    // Attribute ID
    public final static byte SIGNAL_STRENGTH = 0x01;

    public final static byte AUTO_FOCUS = 0x02;//自动对焦命令
    public final static byte AUTO_FOCUS_IN = 0x51;//进入自动对焦
    public final static byte AUTO_FOCUS_OUT = 0x52;//退出自动对焦

    public final static byte SHOW_MOTOR_STEPS = 0x53;//显示电机步数
    //    public final static byte SET_VOLUME = 0x22;//原来是设置音量的 后改成自动对焦的开关了

    public final static byte MEASURE_DISTANCE = 0x03;

    public final static byte LEFT_MOTOR_MANUAL_FOCUS_PLUS = 0x04;//左马达手册加号
    public final static byte LEFT_MOTOR_MANUAL_FOCUS_MINUS = 0x05;//左马达手册减号

    public final static byte RIGHT_MOTOR_MANUAL_FOCUS_PLUS = 0x2E;//右电机手册加号
    public final static byte RIGHT_MOTOR_MANUAL_FOCUS_MINUS = 0x2F;//右电机手册减号

    public final static byte CRUISING_POINT_SET_S1 = 0x70;//巡航点S1 设置
    public final static byte CRUISING_POINT_RECALL_S1 = 0x71;//巡航点S1 RECALL

    public final static byte CRUISING_POINT_SET_S2 = 0x72;//巡航点S2 设置
    public final static byte CRUISING_POINT_RECALL_S2 = 0x73;//巡航点S2 RECALL

    public final static byte CRUISING_POINT_SET_S3 = 0x74;//巡航点S3 设置
    public final static byte CRUISING_POINT_RECALL_S3 = 0x75;//巡航点S3 RECALL

    public final static byte RESET = 0x06;//重启
    public final static byte BIG_VIEW = 0x07;  // it is attribute ID
    public final static byte SMALL_VIEW = 0x08;  // it is attribute ID
    public final static byte NOISE_ELIMINATING_LEVEL_PLUS = 0x09;
    public final static byte NOISE_ELIMINATING_LEVEL_MINUS = 0x0A;
    public final static byte VIEW = 0x0B;
    public final static byte NOISE_ELIMINATING_LEVEL = 0x0C;

    public final static byte LEFT_MOTOR_MANUAL_FOCUS_PLUS_LONG_CLICK = 0x0D;
    public final static byte LEFT_MOTOR_MANUAL_FOCUS_MINUS_LONG_CLICK = 0x0E;
    public final static byte BATTERY_AND_RSSI_VOLTAGE = 0x0F;// 15 电池和RSSI电压
    public final static byte READ_DISTANCE_VALUE = 0x10;
    public final static byte STOP_AUTO_FOCUS = 0x11;
    public final static byte VOLUME_CONTROL = 0x12;

    public final static byte SET_JZ1 = 0x12;
    public final static byte SET_JZ2 = 0x13;
    public final static byte SET_SOUND_SOURCE_1 = 0x14;
    public final static byte SET_SOUND_SOURCE_2 = 0x15;


    //云台控制=================上==================================
    public final static byte M5_UP = 0x61;//点动方向上
    public final static byte M5_DOWN = 0x62;//点动方向下
    public final static byte M5_LEFT = 0X63;//点动方向左
    public final static byte M5_RIGHT = 0x64;//点动方向右

    public final static byte M5_LONG_UP = 0x65;//长按方向上
    public final static byte M5_LONG_DOWN = 0x66;//长按方向下
    public final static byte M5_LONG_LEFT = 0X67;//长按方向左
    public final static byte M5_LONG_RIGHT = 0x68;//长按方向右

    public final static byte M5_STOP = 0X69;//长按停止
    //云台控制=================下==================================


    // upload
    public final static byte JZ1_LEVEL = 0x1C;
    public final static byte JZ2_LEVEL = 0x1D;//降噪等级

    public final static byte SET_MANUAL_FOCUS_RIGHT_MOTOR = 0x1E; // right motor
    public final static byte SET_MANUAL_FOCUS_LEFT_MOTOR = 0x1F; // left motor

    public final static byte CURRENT_MOTOR = 0x20;
    public final static byte CURRENT_JZ = 0x21;


    public final static byte STOP_TWO_CH_AUTO_FOCUS = 0x29;

    public final static byte AMPLIFIER_CONTROL = 0x2B;
    public final static byte ADJUST_AUTO_FOCUS_START = 0x2C;
    public final static byte ADJUST_AUTO_FOCUS_STOP = 0x2D;
    public final static byte ASDF1 = 0X41;//开始使能
    public final static byte ASDF2 = 0X42;//结束使能


    public final static byte RIGHT_MOTOR_MANUAL_FOCUS_PLUS_LONG_CLICK = 0x30;
    public final static byte RIGHT_MOTOR_MANUAL_FOCUS_MINUS_LONG_CLICK = 0x31;
    public final static byte PROTECT_STEPPER_MOVE = 0x33;
    public final static byte CHARGE_STATUS = 0x34;


    // protocol
    public final static byte PROTOCOL_FIXED_LENGTH = 6;

    // ccd
    public final static byte NONE = 0x00;
    public final static byte CCD1 = 0x01;  // CCD1 is small view
    public final static byte CCD2 = 0x02;  // CCD2 is big view


    //设备中的大视野 big view in device
    public final static byte BIG_SIGHT_VIEW = 0x01;

    // small view in device
    public final static byte SMALL_SIGHT_VIEW = 0x02;

    //在手机上正常预览 normal preview on phone
    public final static byte NORMAL_PREVIEW = 0x04;

    // big preview on phone
    public final static byte BIG_PREVIEW = 0x08;

    // small preview on phone
    public final static byte SMALL_PREVIEW = 0x00;


    public final static byte BIG_SIGHT_VIEW_SMALL_PREVIEW = 0x01;//大视野小预览
    public final static byte BIG_SIGHT_VIEW_BIG_PREVIEW = 0x09;  //大视野大预览
    public final static byte BIG_SIGHT_VIEW_NORMAL_PREVIEW = 0x05;//大视野普通预览

    public final static byte SMALL_SIGHT_VIEW_SMALL_PREVIEW = 0x02; //小视野小预览
    public final static byte SMALL_SIGHT_VIEW_BIG_PREVIEW = 0x0A;   //小视野大预览
    public final static byte SMALL_SIGHT_VIEW_NORMAL_PREVIEW = 0x06;//小视野普通预览

    public final static byte AUXILIARY_VIEW_NORMAL_PREVIEW = 0x0C;//12辅助视图正常预览 右屏

    public final static byte AUXILIARY_RRRR = 0x1C;//获取测试准星坐标什么也不干


    public final static byte PASSWORD_CORRECT = 0x01;//1185 密码正确
    public final static byte MSG_ADJUST_AUTO_FOCUS = 0x20; //1203密码正确
    public final static byte MSG_OPERATE_PROTECT_STEPPER = 0x22;//0815 密码正确
    public final static byte MSG_DISPLAY_MOTOR_STEPS = 0x79;//5579 显示获取步进电机步数按钮 密码正确

    public final static byte LONG_PRESS = 0x02; //长按

    public final static byte LEFT_FLING = 0x03;

    public final static byte RIGHT_FLING = 0x04;

    public final static byte SCROLL = 0x05;

    public final static byte MSG_UPDATE_NOISE_LEVEL = 0x10;
    public final static byte MSG_UPDATE_DISTANCE_VALUE = 0x11;//测量距离
    public final static byte MSG_AUTO_FOCUS_DONE = 0x12;
    public final static byte MSG_UPDATE_VIEW = 0x13;
    public final static byte MSG_HEARTBEAT = 0x14;
    public final static byte MSG_COPY_TO_UDISK = 0x15;
    public final static byte MSG_BATTERY_VOLTAGE = 0x16;
    public final static byte MSG_HUIBO_POWER = 0x17;//光回波信号
    public final static byte MSG_M62429_1 = 0x18;
    public final static byte MSG_M62429_2 = 0x19;

    public final static byte MSG_CURRENT_MOTOR = 0x1A;
    public final static byte MSG_CURRENT_JZ = 0x1B;

    public final static byte MSG_STOP_TWO_CH_AUTO_FOCUS = 0x1C;

    public final static byte MSG_SET_CAM1_CENTER = 0x1D;
    public final static byte MSG_SET_CAM2_CENTER = 0x1E;//右侧辅助视图
    public final static byte MSG_SET_CAM3_CENTER = 0x1F;

    public final static byte MSG_ADJUST_AUTO_FOCUS_UPDATE = 0x21;//返回电机值
    public final static byte MSG_CHARGE_STATUS = 0x23;

    // msg.arg1
    public final static byte MSG_CURRENT_RIGHT_MOTOR = 0x01;//右电机
    public final static byte MSG_CURRENT_LEFT_MOTOR = 0x02;//左电机

    public final static byte MSG_CURRENT_JZ_1 = 0x01;
    public final static byte MSG_CURRENT_JZ_2 = 0x02;

    public final static byte MSG_I_AM_LEFT_VIEW = 0x01;//打开安全认证 输入坐标开始校准弹窗
    public final static byte MSG_I_AM_RIGHT_VIEW = 0x02;//打开安全认证 输入坐标开始校准弹窗


    // plus_minus mode

    public final static byte PLUS_MINUS_NONE = 0x00;

    public final static byte PLUS_MINUS_MANUAL_FOCUS = 0x01;

    public final static byte PLUS_MINUS_NOISE_SET = 0x02;

    // cmd name
    public final static byte JZ1 = 0x01;
    public final static byte JZ2 = 0x02;


    // socket connect state
    public final static byte SOCKET_CONNECT_SUCCESS = 0x01;
    public final static byte SOCKET_CONNECT_BROKEN = 0x02;


    // zoom status
    public final static byte INIT_STATUS = 0x01;
    public final static byte SMALL = 0x02;
    public final static byte NORMAL = 0x03;
    public final static byte BIG = 0x04;

    //usb连接状态
    public final static String OTG_USB_STATE_CONNECTED = "1";
    public final static String OTG_USB_STATE_DISCONNECTED = "2";

    //音量
    public final static String VOLUME_RK3399 = "RK3399Volume";
    public final static String VOLUME_JCQ = "JCQ";//寄存器
    public final static String VOLUME_TX2SPK = "Tx2spkVolume";
    public final static String VOLUME_M62429 = "M62429Volume";
    public final static String PROJECT_LANGUAGE = "LANGUAGE";
    public final static String SP_BRIGHTNESS = "B";//亮度

    //method switch
    public final static String METHOD_SWITHCH = "method_switch";
    public final static String METHOD_SELECT = "method_select";
    public final static String METHOD_LEVEL = "method_level";
    public final static String METHOD_QUALITY = "method_quality";
    public final static String STARTFIRST = "StartFirst";//第一次启动标志


    //  =====二维架======2021 12 14 修改以后四条命令==重新定义为二维调整架的快调功能===========

    //    -------------点击----------------

    //方向 向上指令
    public final static byte M3_FORWARD = 0x16;

    //方向 向下指令
    public final static byte M3_REVERSAL = 0x17;

    public final static byte M3_STOP = 0x18;

    //方向 向左指令
    public final static byte M4_FORWARD = 0x19;

    //方向 向右指令
    public final static byte M4_REVERSAL = 0x1A;

    public final static byte M4_STOP = 0x1B;

//    ---------长按-----------------------------

    //二维调整架向左快调 长按 0x24 36
    public final static byte M3_FORWARD_LONG_CLICK = 0x24;

    //二维调整架向右快调 长按 0x25 37
    public final static byte M3_REVERSAL_LONG_CLICK = 0x25;

    //二维调整架向上快调 长按 0x26 38
    public final static byte M4_FORWARD_LONG_CLICK = 0x26;

    //二维调整架向下快调 长按 0x27 39
    public final static byte M4_REVERSAL_LONG_CLICK = 0x27;

//===========2021 12 14 =============================================================


//=================================================================================
//=================================================================================
//=================================================================================
//=================================================================================
//=================================================================================
//=================================================================================
//=================================================================================
//=================================================================================
//=================================================================================

    public final static int PICTURE_WIDTH = 720;
    public final static int PICTURE_HEIGHT = 480;

    public final static byte TWO_CH_AUTO_FOCUS_ENABLE_DISABLE = 0x28;

    // audio source
    public final static byte AUDIO_SOURCE_1 = 0x01;

    public final static byte AUDIO_SOURCE_2 = 0x02;

    //自动对焦按钮停止状态
    public final static byte AUTO_FOCUS_BUTTON_STATE = 0x38;
    public final static String LANGUAGE = "LANGUAGE";

    // Optical Amplifier
    public final static byte OPTICAL_AMPLIFIER_ON = 0x01;
    public final static byte OPTICAL_AMPLIFIER_OFF = 0x02;
    public final static byte SET_CURRENT_100MV = 0x03;
    public final static byte SET_CURRENT_500MV = 0x04;
    public final static byte SET_CURRENT_1000MV = 0x05;
    public final static byte SET_CURRENT_2000MV = 0x06;
    public final static byte SET_CURRENT_3000MV = 0x07;
    public final static byte SET_CURRENT_4000MV = 0x08;

    public final static byte GET_PUMP_POWER = 0x09;
    public final static byte GET_CURRENT = 0x0A;
    public final static byte GET_TEMP = 0x0B;
    public final static byte GET_POWER_IN = 0x0C;
    public final static byte GET_POWER_OUT = 0x0D;

    // KEOPSYS Amplifier
    public final static byte KEOPSYS_SET_AMPLIFIER_OFF = 0x01;
    public final static byte KEOPSYS_SET_AMPLIFIER_ACC_MODE = 0x02;
    public final static byte KEOPSYS_SET_AMPLIFIER_APC_MODE = 0x03;
    public final static byte KEOPSYS_SET_AMPLIFIER_AACC_MODE = 0x04;
    public final static byte KEOPSYS_SET_AMPLIFIER_AAPC_MODE = 0x05;
    public final static byte KEOPSYS_GET_AMPLIFIER_MAXIMUM_VALUE_IN_APC_MODE = 0x06;
    public final static byte KEOPSYS_GET_AMPLIFIER_MINIMUM_VALUE_IN_APC_MODE = 0x07;


    public final static byte KEOPSYS_SET_AMPLIFIER_OUT_POWER_12DBM = 0x08;
    public final static byte KEOPSYS_SET_AMPLIFIER_OUT_POWER_27DBM = 0x04;
    public final static byte KEOPSYS_SET_AMPLIFIER_OUT_CURRENT = 0x0A;
    public final static byte KEOPSYS_GET_AMPLIFIER_OPW = 0x0B;

    //motor mode
    public final static byte LEFT_MOTOR = 0x01;
    public final static byte RIGHT_MOTOR = 0x02;
    public final static byte NOISE_SET_1 = 0x03;
    public final static byte NOISE_SET_2 = 0x04;

    //zoom mode
    public final static byte NORMAL_TO_BIG = 0x01;
    public final static byte BIG_TO_NORMAL = 0x02;
    public final static byte NORMAL_TO_SMALL = 0x03;
    public final static byte SMALL_TO_NORMAL = 0x04;

    public final static byte MSG_SWITCH_LANGUAGE = 0x78;//9999 切换语言
    public final static int MSG_MAIN_RESULT_CODE = 10000;//Main  resultCode

    public final static String HIDDEN_WIRE_MODE = "HIDDEN_WIRE";//是否显示有线模式
    public final static String HIDDEN_SHAKING_MODE = "SHAKING_MODE";//是否显示晃动模式
    public final static String HIDDEN_NONE = "NONE";//是否显示晃动模式
    public final static String HIDDEN_NOISEREDUCTION = "NOISEREDUCTION";//是否显示晃动模式
    public final static String HIDDEN_POST = "POST";//是否显示开机自检
    public final static String HIDDEN_PTZ = "PTZ";//是否显示电动云台

}