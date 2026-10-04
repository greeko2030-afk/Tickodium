package com.greeko.tickodium.mixin;

import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    /**
     * Safely overrides the vanilla fullscreen mode with Borderless Windowed
     * without breaking the JVM OpenGL context.
     */
    @Inject(method = "updateWindowRegion", at = @At("HEAD"), cancellable = true)
    private void onUpdateWindowRegion(CallbackInfo ci) {
        // Cancel the vanilla exclusive fullscreen logic
        ci.cancel();

        long monitor = GLFW.glfwGetPrimaryMonitor();
        if (monitor == 0L) return;

        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
        if (vidMode == null) return;

        if (this.fullscreen) {
            // APPLY BORDERLESS FULLSCREEN
            // Remove window borders
            GLFW.glfwSetWindowAttrib(this.handle, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            
            // Expand window to screen size, keeping it in windowed mode (0L)
            GLFW.glfwSetWindowMonitor(
                this.handle,
                0L,
                0, 0,
                vidMode.width(), vidMode.height(),
                GLFW.GLFW_DONT_CARE
            );
        } else {
            // RESTORE NORMAL WINDOWED MODE
            // Bring back window borders
            GLFW.glfwSetWindowAttrib(this.handle, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            
            // Restore exact previous dimensions
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
