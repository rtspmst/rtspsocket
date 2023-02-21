package com.fhc.laser_monitor_sw_android_rtsp_app;

public interface SocketDataCallback {
    //接收数据
    void onReceiveData(byte[] data);
}
