package org.easydarwin;

import android.content.Context;

import com.tencent.mmkv.MMKV;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SingletonInternalClass {
    private SingletonInternalClass() {

    }

    public static SingletonInternalClass getInstance() {
        return SingletonInternalClassHolder.instance;
    }

    private static class SingletonInternalClassHolder {
        private static final SingletonInternalClass instance = new SingletonInternalClass();
    }

    //单一线程的线程池
    private static ThreadPoolExecutor singleThreadExecutor;

    //如果线程池的规模超过了处理需求，将自动回收空闲线程，
    // 而当需求增加时，则可以自动添加新线程，线程池的规模不存在任何限制
    private static ThreadPoolExecutor threadPoolExecutor;

    // 创建一个定长线程池，支持定时及周期性任务执行
    private static ScheduledExecutorService executor;


    public ExecutorService getSingleThreadExecutor() {
        if (singleThreadExecutor == null) {
            //单一线程的线程池
            singleThreadExecutor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
        }
        return singleThreadExecutor;
    }

    public ExecutorService getThreadPool() {
        if (threadPoolExecutor == null) {
            threadPoolExecutor = new ThreadPoolExecutor(3, Integer.MAX_VALUE,
                    60L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>());
        }
        return threadPoolExecutor;
    }

    //获取定时执行服务
    public ScheduledExecutorService getScheduledExecutorService() {
        if (executor == null) {
            executor = new ScheduledThreadPoolExecutor(1);
        }
        return executor;
    }


    private static MMKV kv;

    public void initMMMKV(Context context){
        MMKV.initialize(context);
    }

    public MMKV getSingleMMKV() {
        if (kv == null) {
            kv = MMKV.defaultMMKV();
        }
        return kv;
    }

}

