package com.greeko.tickodium.mixin;

import com.greeko.tickodium.threading.ThreadManager;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

// Replace 'YourTargetClass.class' with the actual class you are mixing into (e.g., GameRenderer.class or WorldRenderer.class)
@Mixin(targets = "net.minecraft.client.render.ShaderRenderer") // Update this to your actual target if different
public class ShaderRendererMixin {

    // Updated descriptor for Minecraft 1.21 using RenderTickCounter instead of (float, long)
    @Inject(method = "render", at = @At("HEAD"), cancellable = true) // Update "render" to your exact target method name
    private void onRenderWorldAsync(RenderTickCounter tickCounter, CallbackInfo ci) {
        ci.cancel();

        CompletableFuture.runAsync(() -> {
            // Actual Shader Rendering Bytecode
            // If you need tickDelta, you can get it using: tickCounter.getTickDelta(true)
            
        }, ThreadManager.getExecutor());
    }
}
