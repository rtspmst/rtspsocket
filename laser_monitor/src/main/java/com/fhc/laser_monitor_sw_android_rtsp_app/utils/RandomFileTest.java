//package com.fhc.laser_monitor_sw_android_rtsp_app.utils;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.RandomAccessFile;
//
////使用RandomAccessFile实现数据加密
//public class RandomFileTest {
//    //存储文件对象信息
//    private File file;
//    //缓冲区，创建文件中的所有数据
//    byte[] buf;
//    RandomAccessFile fp;
//
//    //用参数filename所指定的文件构建一个对象存储，同时为缓冲区buf分配与文件长度相等的内存空间
//    public RandomFileTest(String filename) {
//        file = new File(filename);
//        buf = new byte[(int) file.length()];
//    }
//
//    public RandomFileTest(File desFilename) {
//        file = desFilename;
//        buf = new byte[(int) desFilename.length()];
//    }
//
//    //对文件进行加密或解密
//    public void coding() throws IOException {
//        //将文件内容读入到缓冲区
//        fp.read(buf);
//        //将缓冲区内的内容按位取反
//        for (int i = 0; i < buf.length; i++) {
//            buf[i] = (byte) (~buf[i]);
//        }
//        //将文件指针定位到文件头
//        fp.seek(0);
//        //将缓冲区中的内容写入到文件中
//        fp.write(buf);
//    }
//
//    public static void main(String[] args) {
//        RandomFileTest ran = new RandomFileTest("d:/test.txt");
//            try {
//
//            ran.openFile();
//            ran.coding();
//            ran.closeFile();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    //按照读写方式打开文件
//    public void openFile() throws FileNotFoundException {
//        fp = new RandomAccessFile(file, "rw");
//    }
//
//    //关闭文件
//    public void closeFile() throws IOException {
//        fp.close();
//    }
//}
