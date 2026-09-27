package com.greeko.tickodium.mixin;

import com.greeko.tickodium.threading.ThreadManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ClientConnection.class)
public class NetworkPacketMixin {

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onPacketSendAsync(Packet<?> packet, CallbackInfo ci) {
        ClientConnection connection = (ClientConnection) (Object) this;
        if (connection.isOpen()) {
            ci.cancel();
            
            CompletableFuture.runAsync(() -> {
                // Actual Network Packet Encoding and Dispatching Bytecode
                connection.sendImmediately(packet, null);
            }, ThreadManager.getExecutor());
        }
    }
}
