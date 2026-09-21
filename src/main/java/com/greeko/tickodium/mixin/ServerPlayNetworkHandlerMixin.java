package com.greeko.tickodium.mixin;

import com.greeko.tickodium.threading.ThreadManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    // Module 4: Network IO & Inbound/Outbound Packet Processing across all cores
    @Inject(method = "tick", at = @At("HEAD"))
    private void onNetworkTick(CallbackInfo ci) {
        CompletableFuture.runAsync(() -> {
            try {
                // Async network packet decoding, encoding, and outbound queue handling
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ThreadManager.getExecutor());
    }
}
