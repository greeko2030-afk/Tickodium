package com.greeko.tickodium.mixin;

import com.greeko.tickodium.threading.ThreadManager;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(MobEntity.class)
public abstract class MobAiMixin {

    // Shadowing protected fields and methods to allow access inside the mixin
    @Shadow protected GoalSelector targetSelector;
    @Shadow protected GoalSelector goalSelector;
    @Shadow protected abstract void mobTick();

    @Inject(method = "tickNewAi", at = @At("HEAD"), cancellable = true)
    private void onTickAiAsync(CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;
        if (!mob.getWorld().isClient) {
            ci.cancel(); 
            
            CompletableFuture.runAsync(() -> {
                // Actual Full Mob AI Pipeline Execution Bytecode
                mob.getWorld().getProfiler().push("targetSelector");
                this.targetSelector.tick();
                mob.getWorld().getProfiler().pop();
                
                mob.getWorld().getProfiler().push("goalSelector");
                this.goalSelector.tick();
                mob.getWorld().getProfiler().pop();
                
                mob.getWorld().getProfiler().push("navigation");
                mob.getNavigation().tick();
                mob.getWorld().getProfiler().pop();
                
                mob.getWorld().getProfiler().push("mob tick");
                this.mobTick();
                mob.getWorld().getProfiler().pop();
            }, ThreadManager.getExecutor());
        }
    }
}
