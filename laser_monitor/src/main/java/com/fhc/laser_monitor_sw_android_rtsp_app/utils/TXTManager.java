package com.fhc.laser_monitor_sw_android_rtsp_app.utils;


import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.FileUtil.getMoviePath;

/**
 * 开机日志管理 保存到txt文件
 */
public class TXTManager {

    private String filePath = getMoviePath();
    public static String fileName = "Important.txt";
    private static TXTManager manager;
    private static EncryptionDecryption des = null;

    private TXTManager() {
    }

    public static TXTManager getInstance() {
        if (manager == null) {
            manager = new TXTManager();
        }

        if (des == null) {
            try {
                des = new EncryptionDecryption("随便写");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return manager;
    }

    //将字符串写入到文本文件中 已经在 Timer-1 中
    synchronized public void writeTxtToFile(String content) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写

        String strContent = content + "\r\n";

        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");

            raf.seek(file.length());

            if (des == null) {
                return;
            }

            //加密字符串
            raf.write(des.encrypt(strContent).getBytes());
            raf.close();

        } catch (Exception e) {
            Log.e("TAG", "写入档案时发生错误：:" + e.getMessage());
        }
    }

    //读取XML内容
    public String readFromXML(String filePath) {
        FileInputStream fileInputStream;
        BufferedReader bufferedReader;
        StringBuilder stringBuilder = new StringBuilder();
        File file = new File(filePath);
        if (file.exists()) {
            try {
                fileInputStream = new FileInputStream(file);
                bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                bufferedReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        if (TextUtils.isEmpty(stringBuilder)) {
            return "";
        }
        //解密字符串
        return des.decrypt(stringBuilder.toString());
    }

    // 生成文件
    private File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    private void makeRootDirectory(String filePath) {
        File file;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
        }
    }
}