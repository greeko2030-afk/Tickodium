package com.greeko.tickodium.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin extends Entity {

    public TntEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * Fixes TNT flying into the sky, prevents sideways sliding, 
     * and stops TNT from vanishing visually during chain explosions.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void fixTntPhysicsDesync(CallbackInfo ci) {
        // Only run physics modifications on the Server to prevent visual desync
        if (this.getWorld().isClient()) {
            return;
        }

        Vec3d vel = this.getVelocity();
        boolean needsUpdate = false;
        double newX = vel.x;
        double newY = vel.y;
        double newZ = vel.z;

        // 1. Cap upward velocity to prevent TNT from flying into the sky 
        // (Vanilla initial hop is ~0.2, so 0.5 allows normal behavior but stops extreme flying)
        if (newY > 0.5) {
            newY = 0.5;
            needsUpdate = true;
        }

        // 2. Smoothly dampen horizontal movement to prevent sliding into corners
        if (Math.abs(newX) > 0.0 || Math.abs(newZ) > 0.0) {
            newX *= 0.7; // Reduce sideways speed safely without breaking block collisions
            newZ *= 0.7;
            
            if (Math.abs(newX) < 0.01) newX = 0.0;
            if (Math.abs(newZ) < 0.01) newZ = 0.0;
            needsUpdate = true;
        }

        // 3. Apply the updated velocity and force a Client sync
        if (needsUpdate) {
            this.setVelocity(newX, newY, newZ);
            
            // CRITICAL FIX: This tells the client about the new velocity. 
            // Without this, the TNT vanishes on the screen before exploding.
            // Note: If 'velocityModified' shows an error, use 'velocityDirty = true' instead.
            this.velocityModified = true; 
        }
    }
}
