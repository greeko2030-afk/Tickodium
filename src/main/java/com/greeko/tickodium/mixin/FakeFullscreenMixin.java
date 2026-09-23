package com.greeko.tickodium.mixin;

import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public abstract class FakeFullscreenMixin {

    @Shadow private long handle;
    @Shadow private boolean fullscreen;
    @Shadow private int windowedX;
    @Shadow private int windowedY;
    @Shadow private int windowedWidth;
    @Shadow private int windowedHeight;

    @Unique
    private boolean isFakeFullscreenActive = false;

    /**
     * Safely applies Borderless Windowed mode without causing OpenGL frame buffer desync.
     */
    @Inject(method = "updateWindowRegion", at = @At("HEAD"), cancellable = true)
    private void onUpdateWindowRegion(CallbackInfo ci) {
        ci.cancel();

        long monitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = monitor != 0L ? GLFW.glfwGetVideoMode(monitor) : null;

        if (this.fullscreen && vidMode != null) {
            this.isFakeFullscreenActive = true;

            // Strip borders and expand window size to fill the display physically
            GLFW.glfwSetWindowAttrib(this.handle, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowMonitor(
                this.handle,
                0L,
                0, 0,
                vidMode.width(), vidMode.height(),
                GLFW.GLFW_DONT_CARE
            );
        } else {
            this.isFakeFullscreenActive = false;

            // Restore original windowed mode borders and dimensions
            GLFW.glfwSetWindowAttrib(this.handle, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowMonitor(
                this.handle,
                0L,
                this.windowedX, this.windowedY,
                this.windowedWidth, this.windowedHeight,
                GLFW.GLFW_DONT_CARE
            );
        }
    }
}
