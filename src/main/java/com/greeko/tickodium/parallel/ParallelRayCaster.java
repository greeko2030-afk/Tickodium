package com.greeko.tickodium.parallel;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Pure-Java (no Minecraft types) re-implementation of vanilla's explosion ray casting.
 *
 * Thread-safety model: the caller builds an immutable snapshot of the blast resistance of
 * every block the rays can touch ON THE MAIN THREAD. Worker threads only ever read that
 * snapshot, never the world. So there are no data races and no chunk-access deadlocks.
 *
 * The maths below intentionally mirrors vanilla Explosion#collectBlocksAndDamageEntities
 * (same float/double arithmetic, same ray order) so the result matches vanilla exactly.
 */
public final class ParallelRayCaster {

    /** Grid value meaning "outside the build limit": a ray stops here. */
    public static final float OUTSIDE = Float.NEGATIVE_INFINITY;
    /** Grid value meaning "no blast resistance" (air): a ray passes through freely. */
    public static final float NO_RESISTANCE = Float.NaN;

    private static final float STEP_DECAY = 0.22500001F;
    private static final int GRID = 16;

    /** Number of rays vanilla fires (the surface of a 16x16x16 cube). */
    public static final int RAY_COUNT;
    private static final double[] DIR_X;
    private static final double[] DIR_Y;
    private static final double[] DIR_Z;

    static {
        int count = 0;
        double[] dx = new double[GRID * GRID * GRID];
        double[] dy = new double[GRID * GRID * GRID];
        double[] dz = new double[GRID * GRID * GRID];
        // Same loop order as vanilla, because the random numbers are consumed in this order.
        for (int j = 0; j < GRID; j++) {
            for (int k = 0; k < GRID; k++) {
                for (int l = 0; l < GRID; l++) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d = j / 15.0F * 2.0F - 1.0F;
                        double e = k / 15.0F * 2.0F - 1.0F;
                        double f = l / 15.0F * 2.0F - 1.0F;
                        double g = Math.sqrt(d * d + e * e + f * f);
                        dx[count] = d / g;
                        dy[count] = e / g;
                        dz[count] = f / g;
                        count++;
                    }
                }
            }
        }
        RAY_COUNT = count;
        DIR_X = dx;
        DIR_Y = dy;
        DIR_Z = dz;
    }

    private ParallelRayCaster() {
    }

    /** Upper bound (in blocks) of how far any ray can travel from the explosion centre. */
    public static double maxReach(float power) {
        return Math.ceil(power * 1.3F / STEP_DECAY) * 0.3D;
    }

    /** Half-size of the snapshot cube around floor(explosion position). */
    public static int radiusFor(float power) {
        return (int) Math.ceil(maxReach(power)) + 2;
    }

    /**
     * Casts all rays and returns the set of snapshot cells that vanilla would destroy.
     *
     * @param grid       blast resistance snapshot, index = (cx * side + cy) * side + cz
     * @param side       2 * radius + 1
     * @param radius     see {@link #radiusFor(float)}
     * @param originX    floor(explosion x); same for Y and Z
     * @param x          exact explosion position
     * @param power      explosion power
     * @param rayRandom  one pre-drawn random float per ray, in vanilla order (length RAY_COUNT)
     * @param executor   worker pool, or null to run on the calling thread
     * @param tasks      how many slices to split the rays into
     */
    public static BitSet cast(float[] grid, int side, int radius,
                              int originX, int originY, int originZ,
                              double x, double y, double z,
                              float power, float[] rayRandom,
                              ExecutorService executor, int tasks) {
        if (rayRandom.length != RAY_COUNT) {
            throw new IllegalArgumentException("rayRandom must have " + RAY_COUNT + " entries");
        }
        int slices = Math.max(1, Math.min(tasks, RAY_COUNT));
        if (executor == null || slices == 1) {
            return castRange(grid, side, radius, originX, originY, originZ, x, y, z, power, rayRandom, 0, RAY_COUNT);
        }

        List<Callable<BitSet>> jobs = new ArrayList<>(slices);
        for (int s = 0; s < slices; s++) {
            final int from = (int) ((long) RAY_COUNT * s / slices);
            final int to = (int) ((long) RAY_COUNT * (s + 1) / slices);
            jobs.add(() -> castRange(grid, side, radius, originX, originY, originZ, x, y, z, power, rayRandom, from, to));
        }

        try {
            BitSet merged = new BitSet(side * side * side);
            // invokeAll blocks the caller until every slice is done. Workers never touch the world,
            // so waiting here cannot deadlock.
            for (Future<BitSet> future : executor.invokeAll(jobs)) {
                merged.or(future.get());
            }
            return merged;
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            // Any pool problem: just do the work on the calling thread. Result is identical.
            return castRange(grid, side, radius, originX, originY, originZ, x, y, z, power, rayRandom, 0, RAY_COUNT);
        }
    }

    private static BitSet castRange(float[] grid, int side, int radius,
                                    int originX, int originY, int originZ,
                                    double x, double y, double z,
                                    float power, float[] rayRandom, int from, int to) {
        BitSet out = new BitSet(side * side * side);
        int baseX = originX - radius;
        int baseY = originY - radius;
        int baseZ = originZ - radius;

        for (int ray = from; ray < to; ray++) {
            double d = DIR_X[ray];
            double e = DIR_Y[ray];
            double f = DIR_Z[ray];
            float h = power * (0.7F + rayRandom[ray] * 0.6F);
            double m = x;
            double n = y;
            double o = z;

            for (; h > 0.0F; h -= STEP_DECAY) {
                int cx = floor(m) - baseX;
                int cy = floor(n) - baseY;
                int cz = floor(o) - baseZ;
                if (cx < 0 || cy < 0 || cz < 0 || cx >= side || cy >= side || cz >= side) {
                    break; // cannot happen with radiusFor(), kept as a safety net
                }
                int index = (cx * side + cy) * side + cz;
                float resistance = grid[index];
                if (resistance == OUTSIDE) {
                    break;
                }
                if (resistance == resistance) { // not NaN, i.e. has resistance
                    h -= (resistance + 0.3F) * 0.3F;
                }
                if (h > 0.0F) {
                    out.set(index);
                }
                m += d * 0.3F;
                n += e * 0.3F;
                o += f * 0.3F;
            }
        }
        return out;
    }

    private static int floor(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }
}
