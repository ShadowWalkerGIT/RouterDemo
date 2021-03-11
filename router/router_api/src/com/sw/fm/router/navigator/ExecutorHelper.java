package com.sw.fm.router.navigator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ExecutorHelper {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE;
    private static final int KEEP_ALIVE_TIME = 30;

    private ExecutorHelper() {
    }

    private static volatile ThreadPoolExecutor sThreadPoolExecutor;

    public static ThreadPoolExecutor getExecutor() {
        if (sThreadPoolExecutor == null) {
            synchronized (ExecutorHelper.class) {
                if (sThreadPoolExecutor == null) {
                    sThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, new ArrayBlockingQueue<>(64));
                }
            }
        }
        return sThreadPoolExecutor;
    }
}
