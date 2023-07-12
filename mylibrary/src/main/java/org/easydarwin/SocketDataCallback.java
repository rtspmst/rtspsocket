package org.easydarwin;

public interface SocketDataCallback {
    //接收数据
    void onReceiveData(byte[] data);
}
