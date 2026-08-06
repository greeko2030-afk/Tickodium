package com.greeko.tickodium;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class TickodiumClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Tickodium.LOGGER.info("[Greeko Company] Tickodium Client 1.21.x Initialized.");

        if (FabricLoader.getInstance().isModLoaded("vulkanmod")) {
            Tickodium.LOGGER.info("[Greeko Company] VulkanMod 1.21.x detected! Rendering synchronization active.");
        } else if (FabricLoader.getInstance().isModLoaded("sodium")) {
            Tickodium.LOGGER.info("[Greeko Company] Sodium 1.21.x detected! OpenGL synchronization active.");
        } else {
            Tickodium.LOGGER.info("[Greeko Company] Running on Standard 1.21.x Vanilla OpenGL pipeline.");
        }
    }
}

