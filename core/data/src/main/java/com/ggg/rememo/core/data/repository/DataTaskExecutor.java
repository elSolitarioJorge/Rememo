package com.ggg.rememo.core.data.repository;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Process-scoped owner of the executor used by synchronous Room DAO calls. */
final class DataTaskExecutor {
    private static final int KEEP_ALIVE_SECONDS = 30;
    private static final AtomicInteger THREAD_SEQUENCE = new AtomicInteger(1);
    private static final ThreadPoolExecutor EXECUTOR = createExecutor();

    private DataTaskExecutor() {
    }

    static Executor get() {
        return EXECUTOR;
    }

    private static ThreadPoolExecutor createExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                1,
                KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                runnable -> new Thread(
                        runnable,
                        "rememo-room-" + THREAD_SEQUENCE.getAndIncrement()
                )
        );
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }
}
