package com.greeko.tickodium.threading;

import net.minecraft.server.MinecraftServer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadManager {

    private static final Object LOCK = new Object();
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();
    private static volatile ExecutorService pool;

    /** Marker type so code can tell whether it is already running on a Tickodium worker. */
    private static final class Worker extends Thread {
        Worker(Runnable task) {
            super(task, "Tickodium-Worker-" + THREAD_COUNTER.incrementAndGet());
            setDaemon(true); // never keeps the JVM alive after the game closes
        }
    }

    /** Number of worker threads. At least 2, otherwise parallel work is pointless. */
    public static int workerCount() {
        return Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
    }

    /**
     * Returns the worker pool. The pool is re-created on demand, so leaving a singleplayer world
     * (which shuts the pool down) and opening another one in the same game session keeps working.
     */
    public static ExecutorService getExecutor() {
        ExecutorService current = pool;
        if (current == null || current.isShutdown()) {
            synchronized (LOCK) {
                current = pool;
                if (current == null || current.isShutdown()) {
                    current = Executors.newFixedThreadPool(workerCount(), Worker::new);
                    pool = current;
                }
            }
        }
        return current;
    }

    /** True when the calling thread is one of the Tickodium workers. */
    public static boolean isWorkerThread() {
        return Thread.currentThread() instanceof Worker;
    }

    /** Shuts the pool down when the server stops. A new pool is created lazily if needed again. */
    public static void shutdown() {
        synchronized (LOCK) {
            if (pool != null && !pool.isShutdown()) {
                pool.shutdown();
            }
            pool = null;
        }
    }

    /** Asynchronously executes a task on the worker pool. */
    public static CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, getExecutor());
    }

    /** Helper to safely execute tasks back on the main Minecraft server thread. */
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
