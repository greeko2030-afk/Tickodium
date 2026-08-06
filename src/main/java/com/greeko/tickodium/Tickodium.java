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
        LOGGER.info("[Greeko Company] Initializing Tickodium for Minecraft 1.21.x...");
        LOGGER.info("[Greeko Company] Allocation: 2 Cores TNT | 2 Cores Mobs | 2 Cores World");

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("[Greeko Company] Server stopping. Shutting down Tickodium Thread Pools...");
            ThreadManager.shutdown();
        });
    }
}

