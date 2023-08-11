package com.bk.webrtc;

public class JniCallNative {

    static {

        System.loadLibrary("mstnative");

    }

    private JniCallNative() {
    }

    public static JniCallNative getInstance() {
        return SingletonInternalClassHolder.instance;
    }

    private static class SingletonInternalClassHolder {
        private static final JniCallNative instance = new JniCallNative();

    }

    public native String jniGetRTSPkey();

    public native String jniGetIP();

    public native int jniGetAudioClient1();

    public native int jniGetJsonHandle2();

    public native int jniGetUartHandle4();


}
