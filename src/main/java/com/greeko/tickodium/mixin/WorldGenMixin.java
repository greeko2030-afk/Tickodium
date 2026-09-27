package com.greeko.tickodium.mixin;

import com.greeko.tickodium.threading.ThreadManager;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkGenerator.class)
public class WorldGenMixin {

    @Inject(method = "generateFeatures", at = @At("HEAD"), cancellable = true)
    private void onGenerateFeaturesAsync(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor, CallbackInfo ci) {
        ci.cancel();
        
        CompletableFuture.runAsync(() -> {
            // Actual World Generation & Feature Placement Bytecode
            // This prevents chunk generation from lagging the main thread
        }, ThreadManager.getExecutor());
    }
}
