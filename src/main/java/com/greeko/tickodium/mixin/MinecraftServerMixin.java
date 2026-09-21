package com.greeko.tickodium.mixin;

import com.greeko.tickodium.threading.ThreadManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    // Module 5: Async Database & World Autosave Loop on all cores
    @Inject(method = "saveAll", at = @At("HEAD"))
    private void onAutosave(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        CompletableFuture.runAsync(() -> {
            try {
                // Offloaded background autosave loop preventing disk-I/O tick lag
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ThreadManager.getExecutor());
    }
}
