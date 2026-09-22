package com.greeko.tickodium.mixin;

import net.minecraft.client.gl.Framebuffer;
import org11.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Framebuffer.class)
public abstract class FramebufferUpscaleMixin {

    /**
     * Applies linear texture filtering to smooth out lower resolution upscale stretching.
     */
    @Inject(method = "initSize", at = @At("TAIL"))
    private void onInitSize(int width, int height, boolean getError, CallbackInfo ci) {
        Framebuffer self = (Framebuffer) (Object) this;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, self.getColorAttachment());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    }
}
