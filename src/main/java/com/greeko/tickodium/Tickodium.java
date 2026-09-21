package com.greeko.tickodium;

import com.greeko.tickodium.threading.ThreadManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tickodium implements ModInitializer {
    public static final String MOD_ID = "tickodium";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Greeko Company] Initializing Tickodium v1.0.2 for Minecraft 1.21.x...");
        LOGGER.info("[Greeko Company] Engine Status: Full-System Multithreading Active. World Gen, Packets, Weather, and Autosave routed to ALL cores.");

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("[Greeko Company] Server stopping. Flushing and shutting down all thread pools safely...");
            ThreadManager.shutdown();
        });
    }
}
