package com.greeko.tickodium.threading;

import net.minecraft.server.MinecraftServer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {

    // Thread pool allocated dynamically based on available CPU cores
    private static final ExecutorService WORKER_POOL = Executors.newFixedThreadPool(
        Math.max(1, Runtime.getRuntime().availableProcessors() / 2)
    );

    /**
     * Returns the worker pool executor service.
     * Fixes "cannot find symbol getExecutor()" across mixins.
     */
    public static ExecutorService getExecutor() {
        return WORKER_POOL;
    }

    /**
     * Safely shuts down the executor service on game/server stop.
     * Fixes "cannot find symbol shutdown()" in Tickodium.java.
     */
    public static void shutdown() {
        if (!WORKER_POOL.isShutdown()) {
            WORKER_POOL.shutdown();
        }
    }

    /**
     * Asynchronously executes a task and returns a CompletableFuture<Void>.
     * Fixes "incompatible types: void cannot be converted to CompletableFuture<Void>" in ServerWorldMixin.java.
     */
    public static CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, WORKER_POOL);
    }

    /**
     * Helper to safely execute tasks back on the main Minecraft server thread.
     */
    public static void runOnMainThread(MinecraftServer server, Runnable task) {
        if (server != null) {
            if (server.isOnThread()) {
                task.run();
            } else {
                server.execute(task);
            }
        }
    }
}
