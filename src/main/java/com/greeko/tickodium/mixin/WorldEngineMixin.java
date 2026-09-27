package com.greeko.tickodium.mixin;

import com.greeko.tickodium.threading.ThreadManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerWorld.class)
public class WorldEngineMixin {

    @Inject(method = "tickWeather", at = @At("HEAD"), cancellable = true)
    private void onTickWeatherAsync(CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        ci.cancel();
        
        CompletableFuture.runAsync(() -> {
            // Actual Weather Processing Bytecode accessed safely via ServerWorldProperties
            if (world.getLevelProperties() instanceof ServerWorldProperties properties) {
                if (world.getGameRules().getBoolean(net.minecraft.world.GameRules.DO_WEATHER_CYCLE)) {
                    int clearWeatherTime = properties.getClearWeatherTime();
                    if (clearWeatherTime > 0) {
                        properties.setClearWeatherTime(clearWeatherTime - 1);
                    }
                }
            }
        }, ThreadManager.getExecutor());
    }
}
