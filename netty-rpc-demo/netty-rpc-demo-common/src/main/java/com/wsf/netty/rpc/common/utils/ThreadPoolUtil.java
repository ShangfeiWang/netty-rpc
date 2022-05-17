package com.wsf.netty.rpc.common.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wsf
 * @since 20220526
 */
public class ThreadPoolUtil {

    public static ThreadPoolExecutor makeServerThreadPool(String serverName, int coreSize, int maxSize) {

        return new ThreadPoolExecutor(coreSize, maxSize, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "netty-rpc" + serverName + "-" + r.hashCode());
            }
        }, new ThreadPoolExecutor.AbortPolicy());
    }
}
