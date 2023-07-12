package org.easydarwin;

public interface SocketStateCallback {

    //Socket连接状态
    void onSocketState(byte state);
}
