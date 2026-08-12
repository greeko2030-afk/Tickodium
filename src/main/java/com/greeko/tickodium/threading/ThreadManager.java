package com.greeko.tickodium.threading;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class ThreadManager {

    // Uses the ForkJoinPool which leverages ALL available CPU cores dynamically for any task
    private static final ExecutorService ALL_CORES_EXECUTOR = ForkJoinPool.commonPool();

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, ALL_CORES_EXECUTOR);
    }

    public static void shutdown() {
        // ForkJoinPool.commonPool() manages its own lifecycle natively, so manual shutdown is not strictly required.
    }
}
