package com.greeko.tickodium.threading;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class ThreadManager {

    private static final ExecutorService ALL_CORES_EXECUTOR = ForkJoinPool.commonPool();

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, ALL_CORES_EXECUTOR);
    }

    public static ExecutorService getExecutor() {
        return ALL_CORES_EXECUTOR;
    }

    public static void shutdown() {
        // ForkJoinPool lifecycle is managed natively by JVM
    }
}
