package com.fhc.laser_monitor_sw_android_rtsp_app.utils;

import com.blankj.utilcode.util.ArrayUtils;

import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Dimorinny on 24.10.15.
 */
public class FileUtils {
    //按目录路径获取文件列表
    public static List<File> getFileListByDirPath(String path, String path0, FileFilter filter) {
        File directory = new File(path);
        File directory0 = new File(path0);
        File[] files = directory.listFiles(filter);
        File[] files0 = directory0.listFiles(filter);

        File[] add = ArrayUtils.add(files, files0);

        if (add == null) {
            return new ArrayList<>();
        }

        List<File> result = Arrays.asList(add);
        Collections.sort(result, new FileComparator());
        return result;
    }

    //获取可读文件大小
    public static String getReadableFileSize(long files) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (files == 0) {
            return wrongSize;
        }
        if (files < 1024) {
            fileSizeString = df.format((float) files) + "B";
        } else if (files < 1024 * 1024) {
            fileSizeString = df.format((float) files / 1024) + "KB";
        } else if (files < 1024 * 1024 * 1024) {
            fileSizeString = df.format((float) files / 1024 / 1024) + "MB";
        } else {
            fileSizeString = df.format((float) files / 1024 / 1024 / 1024) + "GB";
        }
        return fileSizeString;
    }
}
