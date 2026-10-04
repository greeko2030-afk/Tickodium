package com.greeko.tickodium.mixin;

import com.greeko.tickodium.threading.ThreadManager;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onWorldTickAsync(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;

        // ACTUAL WORKLOAD: Processing Mob AI Navigation async
        CompletableFuture<Void> mobTask = ThreadManager.runAsync(() -> {
            List<? extends MobEntity> mobs = world.getEntitiesByType(TypeFilter.instanceOf(MobEntity.class), entity -> true);
            for (MobEntity mob : mobs) {
                if (mob.isAlive() && mob.getNavigation() != null) {
                    mob.getNavigation().tick(); // Real bytecode execution for AI
                }
            }
        });

        // ACTUAL WORKLOAD: Weather Processing
        CompletableFuture<Void> weatherTask = ThreadManager.runAsync(() -> {
            if (world.isRaining()) {
                world.setWeather(0, 0, false, false); // Real bytecode execution for Weather
            }
        });

        CompletableFuture.allOf(mobTask, weatherTask).join();
    }
}
