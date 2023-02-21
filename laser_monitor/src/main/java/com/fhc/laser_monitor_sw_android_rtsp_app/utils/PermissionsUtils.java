package com.fhc.laser_monitor_sw_android_rtsp_app.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;
import com.fhc.laser_monitor_sw_android_rtsp_app.action.AudioDecoder;
import com.fhc.laser_monitor_sw_android_rtsp_app.activity.MainActivity;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.Student;

import java.io.File;

/**
 * 权限申请 添加开机记录
 */
public class PermissionsUtils {

    private static final int REQUEST_PERMISSIONS = 1;

    //权限数组
    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.VIBRATE,

    };

    public static void getPermissions(Context context) {

        if (!hasPermissionsGranted(context)) {
            ActivityCompat.requestPermissions((Activity) context, VIDEO_PERMISSIONS, REQUEST_PERMISSIONS);
        } else {

            //创建文件夹
            createFolder();
        }
    }

    private static void createFolder() {
        String path = "/ShunFengEr";
        String s1 = Environment.getExternalStorageDirectory() + File.separator + path;
        String s2 = Environment.getExternalStorageDirectory() + File.separator + path + "/movie";
        String s3 = Environment.getExternalStorageDirectory() + File.separator + path + "/pcm";
        String s4 = Environment.getExternalStorageDirectory() + File.separator + path + "/picture";

        File destDir1 = new File(s1);
        File destDir2 = new File(s2);
        File destDir3 = new File(s3);
        File destDir4 = new File(s4);

        if (!destDir1.exists()) {
            destDir1.mkdirs();
        }

        if (!destDir2.exists()) {
            destDir2.mkdirs();
        }

        if (!destDir3.exists()) {
            destDir3.mkdirs();
        }

        if (!destDir4.exists()) {
            destDir4.mkdirs();
        }
    }

    public static boolean hasPermissionsGranted(Context context) {
        for (String permission : VIDEO_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //开机日志  已经在 Timer-1 中
    public static void recordBoot() {

        //添加数据

        for (int i = 0; i < 1; i++) {
            Student student = new Student();

            //工作时间
            student.setJobsTime(DatesUtils.cccccccc("开机日志"));

            //是否充电中
            if (MainActivity.CHARGING) {
                student.setCharging(1);
            } else {
                student.setCharging(0);
            }

            //音频是否连接
            student.setConnectionAudio(AudioDecoder.AUDIO_CONNECT);

            //剩余电量
            student.setRemainingBattery(MainActivity.voltage);

            //判断是前台工作还是后台工作
//                    student.setisbackstage(isbackground(context1));

            //串口 透传是否连接
//                    student.setconnectionuart(uarthandle.socketisbroken ? 0 : 1);

            //通信是否连接
//                    student.setconnectionjson(jsonhandle.socketisbroken ? 0 : 1);

            //保存到数据库
            MyApplication.getBoxStore().boxFor(Student.class).put(student);

            //保存在txt文件
            TXTManager.getInstance().writeTxtToFile(student.toString().trim());
        }
    }

}
