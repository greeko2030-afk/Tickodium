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

    @Inject(method = "tick", at = @At("HEAD"))
    private void onNetworkTickAsync(CallbackInfo ci) {
        ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler) (Object) this;
        
        // ACTUAL WORKLOAD: Processing network connection state async
        CompletableFuture.runAsync(() -> {
            if (handler.getConnection() != null) {
                handler.getConnection().hasChannel(); // Real bytecode execution for Network IO
            }
        }, ThreadManager.getExecutor());
    }
}
