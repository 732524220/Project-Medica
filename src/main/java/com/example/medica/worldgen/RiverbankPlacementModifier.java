package com.example.medica.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public class RiverbankPlacementModifier extends PlacementModifier {
    public static final RiverbankPlacementModifier INSTANCE = new RiverbankPlacementModifier();
    public static final MapCodec<RiverbankPlacementModifier> CODEC = MapCodec.unit(INSTANCE);

    private static final int MAX_RADIUS = 8;
    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        for (int distance = 1; distance <= MAX_RADIUS; distance++) {
            for (int[] direction : DIRECTIONS) {
                BlockPos checkPos = pos.offset(direction[0] * distance, 0, direction[1] * distance);
                Holder<Biome> checkBiome = context.getLevel().getBiome(checkPos);

                if (isRiver(checkBiome)) {
                    return Stream.of(pos);
                }
            }
        }

        return Stream.empty();
    }

    private static boolean isRiver(Holder<Biome> biome) {
        return biome.is(Biomes.RIVER) || biome.is(Biomes.FROZEN_RIVER);
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModPlacementModifiers.RIVERBANK;
    }
}
