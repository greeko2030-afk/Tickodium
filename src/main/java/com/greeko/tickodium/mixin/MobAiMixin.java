package com.greeko.tickodium.mixin;

import com.greeko.tickodium.threading.ThreadManager;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(MobEntity.class)
public class MobAiMixin {

    @Inject(method = "tickNewAi", at = @At("HEAD"), cancellable = true)
    private void onTickAiAsync(CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;
        if (!mob.getWorld().isClient) {
            ci.cancel(); 
            
            CompletableFuture.runAsync(() -> {
                // Actual Full Mob AI Pipeline Execution Bytecode
                mob.getWorld().getProfiler().push("targetSelector");
                mob.targetSelector.tick();
                mob.getWorld().getProfiler().pop();
                
                mob.getWorld().getProfiler().push("goalSelector");
                mob.goalSelector.tick();
                mob.getWorld().getProfiler().pop();
                
                mob.getWorld().getProfiler().push("navigation");
                mob.getNavigation().tick();
                mob.getWorld().getProfiler().pop();
                
                mob.getWorld().getProfiler().push("mob tick");
                mob.mobTick();
                mob.getWorld().getProfiler().pop();
            }, ThreadManager.getExecutor());
        }
    }
}
