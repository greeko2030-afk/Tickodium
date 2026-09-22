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
public abstract class BorderlessWindowMixin {

    // Shadowing necessary variables from the original Window class
    @Shadow private long handle;
    @Shadow private boolean fullscreen;
    @Shadow private int windowedX;
    @Shadow private int windowedY;
    @Shadow private int windowedWidth;
    @Shadow private int windowedHeight;

    /**
     * Intercepts the window region update method which is triggered when toggling F11.
     * We cancel the default exclusive fullscreen and replace it with borderless windowed.
     */
    @Inject(method = "updateWindowRegion", at = @At("HEAD"), cancellable = true)
    private void onUpdateWindowRegion(CallbackInfo ci) {
        // Cancel the default vanilla fullscreen logic
        ci.cancel();

        // Get the primary monitor's current video mode (resolution)
        long monitorHandle = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = monitorHandle != 0L ? GLFW.glfwGetVideoMode(monitorHandle) : null;

        if (this.fullscreen && vidMode != null) {
            // APPLY BORDERLESS FULLSCREEN MODE
            
            // 1. Remove the window borders and title bar
            GLFW.glfwSetWindowAttrib(this.handle, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            
            // 2. Set the window to match the screen size and position it at top-left (0, 0)
            // Passing 0L as the monitor handle keeps it in windowed mode internally
            GLFW.glfwSetWindowMonitor(
                this.handle, 
                0L, 
                0, 0, 
                vidMode.width(), vidMode.height(), 
                GLFW.GLFW_DONT_CARE
            );
        } else {
            // APPLY NORMAL WINDOWED MODE
            
            // 1. Restore the window borders and title bar
            GLFW.glfwSetWindowAttrib(this.handle, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            
            // 2. Restore the previous windowed dimensions and position
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
