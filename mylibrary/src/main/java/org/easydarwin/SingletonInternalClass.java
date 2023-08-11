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

    private static ThreadPoolExecutor singleThreadExecutor;

    private static ThreadPoolExecutor threadPoolExecutor;

    private static ScheduledExecutorService executor;


    public ExecutorService getSingleThreadExecutor() {
        if (singleThreadExecutor == null) {
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

