package com.fhc.laser_monitor_sw_android_rtsp_app;

public interface SocketStateCallback {

    //Socket连接状态
    void onSocketState(byte state);
}
