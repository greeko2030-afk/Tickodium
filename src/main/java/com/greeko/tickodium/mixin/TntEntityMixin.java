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
     * Fixes the issue where a single TNT flies into the air upon ignition.
     * This cancels any initial upward velocity and enforces standard gravity.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void fixTntIgniteFlyIssue(CallbackInfo ci) {
        Vec3d velocity = this.getVelocity();

        // 1. Cancel any upward motion (Y > 0) immediately to prevent flying
        if (velocity.y > 0.0) {
            // Apply a slight downward force (standard Minecraft gravity)
            this.setVelocity(velocity.x, -0.04, velocity.z);
        }

        // 2. Reduce excessive horizontal movement to keep the TNT in place
        if (Math.abs(velocity.x) > 0.5 || Math.abs(velocity.z) > 0.5) {
            this.setVelocity(velocity.x * 0.1, this.getVelocity().y, velocity.z * 0.1);
        }

        // 3. Ensure gravity is always enabled for the TNT entity
        if (this.hasNoGravity()) {
            this.setNoGravity(false);
        }
    }
}
