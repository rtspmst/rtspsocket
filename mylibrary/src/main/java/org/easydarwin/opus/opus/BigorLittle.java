//package org.easydarwin.opus.opus;
//
//import android.util.Log;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.ShortBuffer;
//import java.util.Arrays;
//
//public class BigorLittle {
//    public static byte[] bigtolittle(byte[] inBytes){
//        int dataLength = inBytes.length;
//        int shortlength = dataLength / 2;
//        ByteBuffer byteBuffer = ByteBuffer.wrap(inBytes, 0, dataLength);
//        ShortBuffer shortBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
//        // 此处设置大小端
//        short[] shorts = new short[shortlength];
//        shortBuffer.get(shorts, 0, shortlength);
//        byte[] outBytes = new byte[shortlength];
//        for (int i = 0; i < shorts.length; i++) {
//            outBytes[i] = (byte) shorts[i];
//        }
//        return outBytes;
//    }
//}
