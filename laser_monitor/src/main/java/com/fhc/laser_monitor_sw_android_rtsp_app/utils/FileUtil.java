package com.fhc.laser_monitor_sw_android_rtsp_app.utils;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;

public class FileUtil {

    private static String path = Environment.getExternalStorageDirectory() + "/ShunFengEr";

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd HH_mm_ss");// HH:mm:ss

    public static String getPicturePath(String url) {
        return path + "/" + urlDir(url) + "/picture";
    }

    public static File getPictureName() {
        File file = new File(path + "/picture");
        file.mkdirs();

        String format = simpleDateFormat.format(System.currentTimeMillis());
        File res = new File(file, format + ".jpg");
        return res;
    }

    public static String getMoviePath() {
//        return path + "/" + urlDir(url) + "/movie";
        return path + "/movie";
    }

    public static File getMovieName() {
        File file = new File(getMoviePath());
        file.mkdirs();

        File pcmFile = new File(path + "/pcm");
        pcmFile.mkdirs();

        String format = simpleDateFormat.format(System.currentTimeMillis());
        File res = new File(file, format + ".mp4");
        return res;
    }

    private static String urlDir(String url) {
        url = url.replace("://", "");
        url = url.replace("/", "");
        url = url.replace(".", "");

        if (url.length() > 64) {
            url.substring(0, 63);
        }

        return url;
    }

    /*
     * 截屏
     * */
    public static File getSnapFile(String url) {
        File file = new File(getPicturePath(url));
        file.mkdirs();

        File res = new File(file, "snap.jpg");
        return res;
    }
}
