package com.greeko.tickodium.threading;

import net.minecraft.server.MinecraftServer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {
    // Creates a thread pool based on the user's available CPU cores
    private static final ExecutorService WORKER_POOL = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));

    /**
     * Run heavy tasks off the main thread.
     * WARNING: DO NOT modify blocks, chunks, or entities inside this method!
     */
    public static void runAsync(Runnable task) {
        WORKER_POOL.submit(task);
    }

    /**
     * Safely runs world-modifying tasks back on the main Minecraft server thread.
     * Use this after finishing your async calculations.
     */
    public static void runOnMainThread(MinecraftServer server, Runnable task) {
        if (server.isOnThread()) {
            // Already on the main thread, execute immediately
            task.run();
        } else {
            // Queue the task to run on the next main thread tick to prevent crashes
            server.execute(task);
        }
    }
}
