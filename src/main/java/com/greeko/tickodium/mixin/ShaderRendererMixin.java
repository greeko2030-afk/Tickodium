package com.greeko.tickodium.mixin;

import com.greeko.tickodium.threading.NativeThreadManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class ShaderRendererMixin {

    // Target the renderWorld method for Minecraft 1.21+
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void onRenderWorld(RenderTickCounter tickCounter, CallbackInfo ci) {
        
        // Trigger the C++ async logic instead of Java
        NativeThreadManager.processShaderAsync();
        
    }
}
