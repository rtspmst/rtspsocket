package com.fhc.laser_monitor_sw_android_rtsp_app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.StrictMode;

import com.blankj.utilcode.util.Utils;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.MyObjectBox;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.CV;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.HomeKeyEventReceiver;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.Language;
import com.tencent.mmkv.MMKV;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.objectbox.BoxStore;

public class MyApplication extends Application {

    public static final String RTSP_KEY = "6D75724D7A4A36526D343241644A35646F6D466B634E5A6A623230755A6D686A4C6D78686332567958323176626D6C3062334A66633364665957356B636D39705A46397964484E77583246776346634D56714459384F4A4659584E355247467964326C755647566862556C7A5647686C516D567A64434D794D4445354F57566863336B3D";

    private static MMKV kv;
    //单一线程的线程池
    private static ThreadPoolExecutor singleThreadExecutor;

    //如果线程池的规模超过了处理需求，将自动回收空闲线程，
    // 而当需求增加时，则可以自动添加新线程，线程池的规模不存在任何限制
    private static ThreadPoolExecutor threadPoolExecutor;

    // 创建一个定长线程池，支持定时及周期性任务执行
    private static ScheduledExecutorService executor;
    public static int language = 0;

    static class Config {
        static final boolean DEVELOPER_MODE = false;
    }

    public static Context context;

    private static BoxStore boxStore;

    @Override
    public void onCreate() {

        MyApplication.context = getApplicationContext();

        if (Config.DEVELOPER_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            //7.28mst 、使用严格模式  一般用来检测在主线程做一些耗时动作，比如IO读写、数据库操作、Sp操作、Activity泄露、未关闭的Closable对象泄露等，以减少发生ANR等
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
        }

        super.onCreate();

        Utils.init(this);

        initDB();

        MMKV.initialize(this);

        kv = MMKV.defaultMMKV();

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(new HomeKeyEventReceiver(), intentFilter);

        language = MyApplication.getMMKV().decodeInt(CV.LANGUAGE, 1);
    }

    private void initDB() {
        boxStore = MyObjectBox.builder().androidContext(getApplicationContext()).build();
    }

    public static ExecutorService getSingleThreadExecutor() {
        if (singleThreadExecutor == null) {
            //单一线程的线程池
            singleThreadExecutor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
        }
        return singleThreadExecutor;
    }

    public static ExecutorService getThreadPool() {
        if (threadPoolExecutor == null) {
            threadPoolExecutor = new ThreadPoolExecutor(3, Integer.MAX_VALUE,
                    60L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>());
        }
        return threadPoolExecutor;
    }

    //获取定时执行服务
    public static ScheduledExecutorService getScheduledExecutorService() {
        if (executor == null) {
            executor = new ScheduledThreadPoolExecutor(1);
        }
        return executor;
    }

    public static BoxStore getBoxStore() {
        return boxStore;
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static MMKV getMMKV() {
        return MyApplication.kv;
    }

}
