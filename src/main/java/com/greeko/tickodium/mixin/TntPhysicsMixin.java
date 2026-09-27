package com.greeko.tickodium.mixin;

import com.greeko.tickodium.threading.ThreadManager;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.MovementType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(TntEntity.class)
public class TntPhysicsMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTntTickAsync(CallbackInfo ci) {
        TntEntity tnt = (TntEntity) (Object) this;
        if (!tnt.getWorld().isClient) {
            ci.cancel(); // Completely overrides vanilla single-thread TNT logic
            
            CompletableFuture.runAsync(() -> {
                // Actual internal TNT physics execution bytecode
                if (tnt.getFuse() > 0) {
                    tnt.setFuse(tnt.getFuse() - 1);
                }
                
                tnt.move(MovementType.SELF, tnt.getVelocity());
                tnt.setVelocity(tnt.getVelocity().multiply(0.98D));
                
                if (tnt.isOnGround()) {
                    tnt.setVelocity(tnt.getVelocity().multiply(0.7D, -0.5D, 0.7D));
                }
                
                if (tnt.getFuse() <= 0) {
                    tnt.discard();
                    if (!tnt.getWorld().isClient) {
                        tnt.getWorld().createExplosion(tnt, tnt.getX(), tnt.getBodyY(0.0625D), tnt.getZ(), 4.0F, net.minecraft.world.World.ExplosionSourceType.TNT);
                    }
                }
            }, ThreadManager.getExecutor());
        }
    }
}
