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

    @Inject(method = "saveAll", at = @At("HEAD"))
    private void onAutosaveAsync(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        
        // ACTUAL WORKLOAD: Touching player data asynchronously
        CompletableFuture.runAsync(() -> {
            if (server.getPlayerManager() != null) {
                server.getPlayerManager().saveAllPlayerData(); // Real bytecode execution for Autosave
            }
        }, ThreadManager.getExecutor());
    }
}
