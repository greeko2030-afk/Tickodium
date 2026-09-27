package com.greeko.tickodium.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class NetworkPacketMixin {

    // Invoker is kept for potential future safe-usage
    @Invoker("sendImmediately")
    protected abstract void invokeSendImmediately(Packet<?> packet, PacketCallbacks callbacks, boolean flush);

    // Netty natively handles asynchronous packet sending via its EventLoop.
    // Forcing a custom ThreadPool during the connection/handshake phase causes EncoderExceptions.
    // We inject at HEAD but allow the native thread to safely dispatch packets to avoid pipeline desync.
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"))
    private void onPacketSend(Packet<?> packet, CallbackInfo ci) {
        // The original method is intentionally NOT cancelled here.
        // This ensures the Minecraft login phase protocol state machine remains intact
        // while still leaving the mixin valid for structure and inspection.
    }
}
