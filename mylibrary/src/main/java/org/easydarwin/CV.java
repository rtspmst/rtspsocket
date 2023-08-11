package org.easydarwin;

public class CV {

    public static final byte IMG_PROCESS_ON = 0x01;
    public static final byte IMG_PROCESS_OFF = 0x02;
    public static final byte ADJUST_VOLUME = 0x03;
    public static final byte START_UPDATE_UI = 0x04;
    public static final byte SET_CAMERA1_CNETER = 0x06;
    public static final byte SET_CAMERA2_CNETER = 0x07;
    public static final byte SET_DENOISE_METHOD = 0x08;
    public static final byte SET_BEVIS_GRADE = 0x09;
    public static final byte SET_NOISE_VIEW = 0x0A;//Selection of noise reduction level
    public static final byte SET_CAMERA3_CNETER = 0x0B;//11
    public static final byte GET_CAMERA1_CNETER = 0x0C;
    public static final byte GET_CAMERA2_CNETER = 0x0D;//13
    public static final byte GET_CAMERA3_CNETER = 0x0E;
    public static final byte IMG_PROCESS_QUERY = 0x0F;//15
    public static final byte SWITCH_VIEW = 0x10;
    public static final byte SET_WEBRTC_GRADE = 0x11;//17
    public static final byte STRONG_MODE = 0x12;
    public static final byte TX2SPK_VOLUME = 0x13;//set volume  19
    public static final byte DEMODE = 0x14;
    public static final byte DEMODE_VALUE = 0x15;//(21)
    public static final byte DEMODE_SBDSRC = 0x17;
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
    // endpoint
    public final static byte ENDPOINT = 0x01; //1
    public final static byte HEATBEAT_ENDPOINT = 0x02;
    public final static byte AUTO_FOCUS = 0x02;//Autofocus command
    public final static byte AUTO_FOCUS_IN = 0x51;//Enter autofocus
    public final static byte AUTO_FOCUS_OUT = 0x52;//Exit Autofocus
    public final static byte MEASURE_DISTANCE = 0x03;
    public final static byte LEFT_MOTOR_MANUAL_FOCUS_PLUS = 0x04;//Left motor manual plus sign
    public final static byte LEFT_MOTOR_MANUAL_FOCUS_MINUS = 0x05;//Left Motor Manual Minus Sign
    public final static byte RIGHT_MOTOR_MANUAL_FOCUS_PLUS = 0x2E;//Right motor manual plus sign
    public final static byte RIGHT_MOTOR_MANUAL_FOCUS_MINUS = 0x2F;//Right motor manual minus sign
    public final static byte RESET = 0x06;//
    public final static byte VIEW = 0x0B;
    public final static byte BATTERY_AND_RSSI_VOLTAGE = 0x0F;// 15 Battery and RSSI voltage
    //Pan tilt control===================================================
    public final static byte M5_UP = 0x61;//Up
    public final static byte M5_DOWN = 0x62;//down
    public final static byte M5_LEFT = 0X63;//Left
    public final static byte M5_RIGHT = 0x64;//Right
    public final static byte M5_LONG_UP = 0x65;//
    public final static byte M5_LONG_DOWN = 0x66;//
    public final static byte M5_LONG_LEFT = 0X67;//
    public final static byte M5_LONG_RIGHT = 0x68;//
    public final static byte M5_STOP = 0X69;//Long press to stop
    //yuntai===================================================
    public final static byte ASDF1 = 0X41;//Start enabling
    public final static byte ASDF2 = 0X42;//End Enable
    // protocol
    public final static byte PROTOCOL_FIXED_LENGTH = 6;
    public final static byte BIG_SIGHT_VIEW = 0x01;

    // small view in device
    public final static byte SMALL_SIGHT_VIEW = 0x02;

    // normal preview on phone
    public final static byte NORMAL_PREVIEW = 0x04;

    // big preview on phone
    public final static byte BIG_PREVIEW = 0x08;

    // small preview on phone
    public final static byte SMALL_PREVIEW = 0x00;


    public final static byte BIG_SIGHT_VIEW_SMALL_PREVIEW = 0x01;//Large field of view and small preview
    public final static byte BIG_SIGHT_VIEW_BIG_PREVIEW = 0x09;  //Big View and Preview
    public final static byte BIG_SIGHT_VIEW_NORMAL_PREVIEW = 0x05;//Large View Normal Preview

    public final static byte SMALL_SIGHT_VIEW_SMALL_PREVIEW = 0x02; //Small view and preview
    public final static byte SMALL_SIGHT_VIEW_BIG_PREVIEW = 0x0A;   //Small field of view and large preview
    public final static byte SMALL_SIGHT_VIEW_NORMAL_PREVIEW = 0x06;//Small Field of View Normal Preview

    public final static byte AUXILIARY_VIEW_NORMAL_PREVIEW = 0x0C;//Auxiliary View Normal Preview

    public final static byte MSG_UPDATE_DISTANCE_VALUE = 0x11;//Measuring distance

    public final static byte MSG_UPDATE_VIEW = 0x13;
    public final static byte MSG_HEARTBEAT = 0x14;
    public final static byte MSG_BATTERY_VOLTAGE = 0x16;
    public final static byte MSG_HUIBO_POWER = 0x17;//Light echo signal

    public final static byte MSG_CHARGE_STATUS = 0x23;

    // socket connect state
    public final static byte SOCKET_CONNECT_SUCCESS = 0x01;
    public final static byte SOCKET_CONNECT_BROKEN = 0x02;

    public final static byte SMALL = 0x02;
    public final static byte NORMAL = 0x03;
    public final static byte BIG = 0x04;

    public final static String VOLUME_TX2SPK = "Tx2spkVolume";

    public final static String SP_BRIGHTNESS = "B";//

    //method switch
    public final static String METHOD_SWITHCH = "method_switch";
    public final static String METHOD_SELECT = "method_select";
    public final static String METHOD_LEVEL = "method_level";

    //  =====erweijia=================

    public final static byte M3_FORWARD = 0x16;

    public final static byte M3_REVERSAL = 0x17;

    public final static byte M3_STOP = 0x18;

    public final static byte M4_FORWARD = 0x19;

    public final static byte M4_REVERSAL = 0x1A;

    public final static byte M4_STOP = 0x1B;

//    ---------erweijia changan-----------------------------

    // 0x24 36
    public final static byte M3_FORWARD_LONG_CLICK = 0x24;

    // 0x25 37
    public final static byte M3_REVERSAL_LONG_CLICK = 0x25;

    // 0x26 38
    public final static byte M4_FORWARD_LONG_CLICK = 0x26;

    // 0x27 39
    public final static byte M4_REVERSAL_LONG_CLICK = 0x27;

    public final static String LANGUAGE = "LANGUAGE";

    public final static int MSG_MAIN_RESULT_CODE = 10000;//Main  resultCode


}