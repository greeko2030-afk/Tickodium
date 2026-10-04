package com.greeko.tickodium.mixin;

import com.greeko.tickodium.threading.ThreadManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class WorldEngineMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onWorldTick(java.util.function.BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;

        // 1. Send heavy operations to the background thread
        ThreadManager.runAsync(() -> {
            
            // Example: Do some heavy calculations here (Pathfinding, custom logic, block scanning)
            // This happens on a separate thread, so it won't lag the server!
            double heavyMathResult = Math.pow(2048, 2); 

            // 2. Safely apply the results back on the Main Thread
            ThreadManager.runOnMainThread(world.getServer(), () -> {
                
                // SAFE ZONE: You can modify the world here without getting NullPointerExceptions
                // Example: Spawn entities, explode TNT, update blocks safely
                // System.out.println("Applied updates to the world safely! Result: " + heavyMathResult);
                
            });
        });
    }
}
