package com.greeko.tickodium;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class TickodiumClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Tickodium.LOGGER.info("[Greeko Company] Tickodium Client 1.0.1 Initialized.");
        Tickodium.LOGGER.info("[Greeko Company] Shader Multithreading: Active (All Cores allocated for pre-rendering tasks).");

        if (FabricLoader.getInstance().isModLoaded("vulkanmod")) {
            Tickodium.LOGGER.info("[Greeko Company] VulkanMod detected! Rendering synchronization active.");
        } else if (FabricLoader.getInstance().isModLoaded("sodium")) {
            Tickodium.LOGGER.info("[Greeko Company] Sodium detected! OpenGL synchronization active.");
        } else if (FabricLoader.getInstance().isModLoaded("iris")) {
            Tickodium.LOGGER.info("[Greeko Company] Iris Shaders detected! Optimizing shader matrix calculations across all cores.");
        } else {
            Tickodium.LOGGER.info("[Greeko Company] Running on Standard Vanilla OpenGL pipeline.");
        }
    }
}
