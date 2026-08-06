package com.greeko.tickodium.mixin;

import com.greeko.tickodium.threading.ThreadManager;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter; // Added TypeFilter for 1.21.x compatibility
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "tickWorlds", at = @At("HEAD"))
    private void onWorldsTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;

        for (ServerWorld world : server.getWorlds()) {
            
            // Task 1: Mob AI & Pathfinding Processing (2 Cores)
            // FIXED: Replaced 'null' with TypeFilter.instanceOf()
            CompletableFuture<Void> mobTask = ThreadManager.runMobTask(() -> {
                try {
                    world.getEntitiesByType(TypeFilter.instanceOf(MobEntity.class), entity -> true)
                            .forEach(entity -> {
                                // Server-side Mob AI updates
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Task 2: TNT Physics & Explosion Raycasting (2 Cores)
            // FIXED: Replaced 'null' with TypeFilter.instanceOf()
            CompletableFuture<Void> tntTask = ThreadManager.runTntTask(() -> {
                try {
                    world.getEntitiesByType(TypeFilter.instanceOf(TntEntity.class), entity -> true)
                            .forEach(entity -> {
                                // Compute TNT physics & movement
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Task 3: World & Block Data Processing (2 Cores)
            CompletableFuture<Void> worldTask = ThreadManager.runWorldTask(() -> {
                try {
                    // Async block tick processing
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Synchronization Point (Safe for VulkanMod / Sodium / Standard OpenGL rendering)
            CompletableFuture.allOf(mobTask, tntTask, worldTask).join();
        }
    }
}
