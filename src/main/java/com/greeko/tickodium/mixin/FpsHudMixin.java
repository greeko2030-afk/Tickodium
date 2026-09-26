package com.greeko.tickodium.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class FpsHudMixin {

    // Injecting at the HEAD ensures the FPS renders even if the HUD is hidden by F1
    @Inject(method = "render", at = @At("HEAD"))
    private void renderPersistentFps(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Get the current FPS directly from the client
        String fpsText = client.getCurrentFps() + " FPS";
        
        // Draw the text on the top left corner (X=5, Y=5)
        // This runs before the F1 (hudHidden) check, so it stays visible
        context.drawTextWithShadow(client.textRenderer, fpsText, 5, 5, 0xFFFFFF);
    }
}
