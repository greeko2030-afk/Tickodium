package com.greeko.tickodium.threading;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {

    // 2 Dedicated cores per task category
    private static final ExecutorService TNT_EXECUTOR = Executors.newFixedThreadPool(2);
    private static final ExecutorService MOB_EXECUTOR = Executors.newFixedThreadPool(2);
    private static final ExecutorService WORLD_EXECUTOR = Executors.newFixedThreadPool(2);

    public static CompletableFuture<Void> runTntTask(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, TNT_EXECUTOR);
    }

    public static CompletableFuture<Void> runMobTask(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, MOB_EXECUTOR);
    }

    public static CompletableFuture<Void> runWorldTask(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, WORLD_EXECUTOR);
    }

    public static void shutdown() {
        TNT_EXECUTOR.shutdown();
        MOB_EXECUTOR.shutdown();
        WORLD_EXECUTOR.shutdown();
    }
}

