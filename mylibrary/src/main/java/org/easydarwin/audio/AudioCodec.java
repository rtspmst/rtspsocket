package org.easydarwin.audio;


public class AudioCodec {
    static {
        System.loadLibrary("proffmpeg");
        System.loadLibrary("AudioCodecer");
    }

    public static native long create(int codec, int sample_rate, int channels, int sample_bit);

    public static native int decode(long handle, byte[] buffer, int offset, int length, byte[] pcm, int[] outLen);

    public static native void close(long handle);
}
