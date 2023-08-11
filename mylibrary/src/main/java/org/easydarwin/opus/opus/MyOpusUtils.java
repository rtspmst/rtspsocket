package org.easydarwin.opus.opus;

public class MyOpusUtils {

    static {
        System.loadLibrary("opusJni");
    }

    public native long createEncoder(int sampleRateInHz, int channelConfig, int complexity);

    public native long createDecoder(int sampleRateInHz, int channelConfig);

    public native int encode(long handle, short[] shortArray, int offset, byte[] encoded);

    public native int decode(long handle, byte[] byteArray, short[] shortArray);

    public native int destroyEncoder(long handle);

    public native int destroyDecoder(long handle);

}
