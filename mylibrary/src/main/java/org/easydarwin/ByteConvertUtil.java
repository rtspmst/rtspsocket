package org.easydarwin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteConvertUtil {


//============================Small end============================================

    /**
     * Convert the int value into a byte array that occupies four bytes,，
     * This method is applicable to the order of (low order before high order after).
     */
    public static byte[] intToBytes(int value) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (value & 0xFF);
        bytes[1] = (byte) ((value >> 8) & 0xFF);
        bytes[2] = (byte) ((value >> 16) & 0xFF);
        bytes[3] = (byte) ((value >> 24) & 0xFF);
        return bytes;
    }


    /**
     * Take int value from byte array，
     * This method is applicable to the order of (low order before high order after),
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }



//-----------------------------Big end---------------------------------------------------------

    /**
     * Take int value from byte array，
     * This method is applicable to the order of low order followed by high order.
     */
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF));
        return value;
    }

    /**
     * Convert the int value into a byte array that occupies four bytes,
     * This method is applicable to the order of (high order before low order after)
     */
    public static byte[] intToBytes2(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }


//*****************Character Byte Conversion*************************************************

    /**
     * Convert each character of a string into two bytes and return a byte array
     *
     * @param str
     * @return
     */
    public static byte[] get2Byte(String str) throws Exception {
        int length = 0;// The length of a byte array
        byte[] buffer1 = new byte[1024];
        for (int i = 0; i < str.length(); i++) {
            char a = str.charAt(i);
            byte[] bu = new byte[2];
            if (a < 256) { // English characters
                bu[0] = 0;
                bu[1] = (byte) a;
            } else { // Chinese characters
                bu = (a + "").getBytes("GBK");
            }
            buffer1[length++] = bu[0];
            buffer1[length++] = bu[1];
        }
        byte[] buffer2 = new byte[length];
        System.arraycopy(buffer1, 0, buffer2, 0, length);
        return buffer2;
    }

    /**
     * The method of restoring the input byte array to a string (every two bits are restored to one character)
     *
     * @return
     */
    public static String getString(byte[] buffer) throws Exception {
        String str = "";
        for (int i = 0; i < buffer.length; i += 2) {
            byte[] bu = new byte[2];
            bu[0] = buffer[i];
            bu[1] = buffer[i + 1];
            if (bu[0] == 0) { // The high order is 0, which is an English character
                char a = (char) bu[1];
                str += a;
            }
            if (bu[0] != 0) {
                String a = new String(bu, "GBK");
                str += a;
            }
        }
        return str;
    }

//*****************Character Byte Conversion*************************************************

    public static byte[] addBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }


    //    little endian   Is the least significant byte (LSB) stored at a low address（LSB）
    public static byte[] ShortArraytoByteArray(short[] src, int len) {
        //注意字节序
        byte[] dest = new byte[len * 2];
        for (int i = 0; i < len; i++) {
            dest[i * 2] = (byte) src[i];
            dest[i * 2 + 1] = (byte) (src[i] >> 8);
        }
        return dest;
    }

    public static short[] byteArrayToShortArray(byte[] byteArray) {
        short[] shortArray = new short[byteArray.length / 2];
        ByteBuffer.wrap(byteArray).order(ByteOrder.nativeOrder()).asShortBuffer().get(shortArray);
        return shortArray;
    }

//########################### Hexadecimal string byte array mutual conversion ##############################################################################

    /**
     * Convert the byte array to a string representing hexadecimal
     *
     * @param arrB Byte array to be converted
     * @return String represented in hexadecimal
     * @throws Exception
     */
    public static String byteArr2HexStr(byte[] arrB) throws Exception {
        int bLen = arrB.length;
        //Each character occupies two bytes, so the degree length of a string needs to be twice that of an array
        StringBuffer strBuffer = new StringBuffer(bLen * 2);
        for (int i = 0; i != bLen; ++i) {
            int intTmp = arrB[i];
            //Convert positive numbers to positive numbers
            while (intTmp < 0) {
                intTmp = intTmp + 256;//Because a byte of a word is 8 bits, counting from low to high, and the symbol in the 9th bit is, plus 256, which is equivalent to adding 1 in the 9th bit
            }
            //Data less than 0 F needs to be supplemented with 0 at the end
            // (because it was originally one byte, but now it has been changed to a String of two bytes.
            // If it is less than 0 F, it is clear that the maximum is not enough for the first byte.
            // The second byte needs to be supplemented with 0)
            if (intTmp < 16) {
                strBuffer.append("0");
            }
            strBuffer.append(Integer.toString(intTmp, 16));
        }
        return strBuffer.toString();
    }


    /**
     * Convert strings representing hexadecimal to byte arrays
     *
     * @param hexStr
     * @return
     * @throws Exception
     */
    public static byte[] hexStr2ByteArr(String hexStr) throws Exception {
        byte[] arrB = hexStr.getBytes();
        int bLen = arrB.length;
        byte[] arrOut = new byte[bLen / 2];
        for (int i = 0; i < bLen; i = i + 2) {
            String strTmp = new String(arrB, i, 2);
            arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
        }
        return arrOut;
    }

//########################### Hexadecimal string byte array mutual conversion ##############################################################################


}
