package com.greeko.tickodium.mixin;

import com.greeko.tickodium.parallel.ParallelRayCaster;
import com.greeko.tickodium.threading.ThreadManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.BitSet;
import java.util.Optional;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow
    private World world;

    @Shadow
    private double x;

    @Shadow
    private double y;

    @Shadow
    private double z;

    @Shadow
    private float power;

    @Shadow
    private ExplosionBehavior behavior;

    @Shadow
    private ObjectArrayList<BlockPos> affectedBlocks;

    @Unique
    private static final float TICKODIUM_SKIP_RAY = 0.0F;

    @Unique
    private static final int TICKODIUM_MAX_RADIUS = 32;

    @Unique
    private static boolean tickodium$warned;

    /** 0 = not decided yet, 1 = use vanilla rays, 2 = parallel result already stored. */
    @Unique
    private int tickodium$state;

    /**
     * Vanilla calls random.nextFloat() once per ray. The first call tells us the ray casting is
     * starting, so we try the parallel path there. If it worked, every ray is skipped.
     */
    @Redirect(
        method = "collectBlocksAndDamageEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/random/Random;nextFloat()F"
        )
    )
    private float tickodium$rayRandom(Random random) {
        if (this.tickodium$state == 0) {
            boolean done;
            try {
                done = this.tickodium$tryParallel(random);
            } catch (Throwable t) {
                if (!tickodium$warned) {
                    tickodium$warned = true;
                    com.greeko.tickodium.Tickodium.LOGGER.warn(
                        "[Tickodium] Parallel explosion failed, using vanilla rays.",
                        t
                    );
                }
                done = false;
            }

            this.tickodium$state = done ? 2 : 1;
        }

        return this.tickodium$state == 2
            ? TICKODIUM_SKIP_RAY
            : random.nextFloat();
    }

    @Unique
    private boolean tickodium$tryParallel(Random random) {
        if (!Boolean.parseBoolean(
            System.getProperty("tickodium.parallelExplosions", "true")
        )) {
            return false;
        }

        if (this.world.isClient()) {
            return false;
        }

        Explosion self = (Explosion) (Object) this;
        Entity source = self.getEntity();

        // Only TNT: its destroy rule does not depend on the remaining ray power.
        if (!(source instanceof TntEntity)) {
            return false;
        }

        int radius = ParallelRayCaster.radiusFor(this.power);

        if (radius > TICKODIUM_MAX_RADIUS) {
            return false;
        }

        boolean useWorkers = !ThreadManager.isWorkerThread();

        // ---- 1. Snapshot on the main thread ----
        int ox = MathHelper.floor(this.x);
        int oy = MathHelper.floor(this.y);
        int oz = MathHelper.floor(this.z);

        int side = radius * 2 + 1;

        double reach =
            ParallelRayCaster.maxReach(this.power) + 1.75D;

        double reachSq = reach * reach;

        float[] grid = new float[side * side * side];

        java.util.Arrays.fill(
            grid,
            ParallelRayCaster.OUTSIDE
        );

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int cx = 0; cx < side; cx++) {
            for (int cy = 0; cy < side; cy++) {
                for (int cz = 0; cz < side; cz++) {

                    int bx = ox - radius + cx;
                    int by = oy - radius + cy;
                    int bz = oz - radius + cz;

                    double dx =
                        bx + 0.5D - this.x;

                    double dy =
                        by + 0.5D - this.y;

                    double dz =
                        bz + 0.5D - this.z;

                    // Cells no ray can reach are skipped,
                    // so no extra chunks get touched.
                    if (dx * dx + dy * dy + dz * dz > reachSq) {
                        continue;
                    }

                    pos.set(bx, by, bz);

                    int index =
                        (cx * side + cy) * side + cz;

                    if (!this.world.isInBuildLimit(pos)) {
                        grid[index] =
                            ParallelRayCaster.OUTSIDE;
                        continue;
                    }

                    BlockState state =
                        this.world.getBlockState(pos);

                    FluidState fluid =
                        this.world.getFluidState(pos);

                    Optional<Float> resistance =
                        this.behavior.getBlastResistance(
                            self,
                            this.world,
                            pos,
                            state,
                            fluid
                        );

                    grid[index] =
                        resistance.isPresent()
                            ? resistance.get()
                            : ParallelRayCaster.NO_RESISTANCE;
                }
            }
        }

        // Draw the random numbers exactly like vanilla does:
        // one per ray, same order.
        float[] rayRandom =
            new float[ParallelRayCaster.RAY_COUNT];

        for (int i = 0; i < rayRandom.length; i++) {
            rayRandom[i] = random.nextFloat();
        }

        // ---- 2. Parallel ray casting on the snapshot ----
        // (main thread waits)
        BitSet hit = ParallelRayCaster.cast(
            grid,
            side,
            radius,
            ox,
            oy,
            oz,
            this.x,
            this.y,
            this.z,
            this.power,
            rayRandom,
            useWorkers
                ? ThreadManager.getExecutor()
                : null,
            ThreadManager.workerCount()
        );

        // ---- 3. Apply on the main thread ----
        for (
            int i = hit.nextSetBit(0);
            i >= 0;
            i = hit.nextSetBit(i + 1)
        ) {
            int cz = i % side;
            int cy = (i / side) % side;
            int cx = i / (side * side);

            this.affectedBlocks.add(
                new BlockPos(
                    ox - radius + cx,
                    oy - radius + cy,
                    oz - radius + cz
                )
            );
        }

        return true;
    }
  }
