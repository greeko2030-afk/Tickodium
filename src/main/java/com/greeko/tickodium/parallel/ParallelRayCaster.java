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
        // Same loop order
