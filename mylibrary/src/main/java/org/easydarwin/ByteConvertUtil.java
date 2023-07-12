package org.easydarwin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteConvertUtil {


//============================小端============================================

    /**
     * 将int数值转换为占四个字节的byte数组，
     * 本方法适用于(低位在前，高位在后)的顺序。
     * 和 bytesToInt（）配套使用
     *
     * @param value 要转换的int值
     * @return byte数组
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
     * byte数组中取int数值，
     * 本方法适用于(低位在前，高位在后)的顺序，
     * 和 intToBytes（）配套使用
     *
     * @param src    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

//=========================小端==================================================


//-----------------------------大端---------------------------------------------------------

    /**
     * byte数组中取int数值，
     * 本方法适用于(低位在后，高位在前)的顺序。
     * 和intToBytes2（）配套使用
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
     * 将int数值转换为占四个字节的byte数组，
     * 本方法适用于(高位在前，低位在后)的顺序。
     * 和bytesToInt2（）配套使用
     */
    public static byte[] intToBytes2(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

//------------------------------大端------------------------------------------------------------------

//*****************字符 字节 转换*************************************************

    /**
     * 把字符串的每个字符转化为两个字节 返回一个字节数组
     *
     * @param str
     * @return
     */
    public static byte[] get2Byte(String str) throws Exception {
        int length = 0;// 字节数组的长度
        byte[] buffer1 = new byte[1024];
        for (int i = 0; i < str.length(); i++) {
            char a = str.charAt(i);
            byte[] bu = new byte[2];
            if (a < 256) { // 英文字符
                bu[0] = 0;
                bu[1] = (byte) a;
            } else { // 中文字符
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
     * 把输入的字节数组还原为字符串的方法（每两位还原为一个字符）
     *
     * @return
     */
    public static String getString(byte[] buffer) throws Exception {
        String str = "";
        for (int i = 0; i < buffer.length; i += 2) {
            byte[] bu = new byte[2];
            bu[0] = buffer[i];
            bu[1] = buffer[i + 1];
            if (bu[0] == 0) { // 高位为0，是英文字符
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

//*****************字符 字节 转换*************************************************

    public static byte[] addBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }


    // 小字节序   little endian   是低地址存放最低有效字节（LSB）
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

//########################### 16进制的字符串  byte数组 互转 ##############################################################################

    /**
     * 将byte数组转换为表现16进制的字符串
     *
     * @param arrB 须要转换的byte数组
     * @return 16进制表现的字符串
     * @throws Exception
     */
    public static String byteArr2HexStr(byte[] arrB) throws Exception {
        int bLen = arrB.length;
        //每一个字符占用两个字节，所以字符串的度长需是数组度长的2倍
        StringBuffer strBuffer = new StringBuffer(bLen * 2);
        for (int i = 0; i != bLen; ++i) {
            int intTmp = arrB[i];
            //把正数转化为正数
            while (intTmp < 0) {
                intTmp = intTmp + 256;//因为字一个字节是8位，从低往高数，第9位为符号为，加256，相当于在第九位加1
            }
            //小于0F的数据须要在后面补0，(因为原来是一个字节，在现成变String是两个字节，如果小于0F的话，明说大最也盛不满第一个字节。第二个需弥补0)
            if (intTmp < 16) {
                strBuffer.append("0");
            }
            strBuffer.append(Integer.toString(intTmp, 16));
        }
        return strBuffer.toString();
    }


    /**
     * 将表现16进制的字符串转化为byte数组
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

//########################### 16进制的字符串  byte数组 互转 ##############################################################################


}
