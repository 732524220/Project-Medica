package com.example.medica.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public class BiomeEdgePlacementModifier extends PlacementModifier {

    public static final BiomeEdgePlacementModifier INSTANCE = new BiomeEdgePlacementModifier();

    public static final MapCodec<BiomeEdgePlacementModifier> CODEC = MapCodec.unit(INSTANCE);

    private static final int RADIUS = 8;

    private static final int[][] OFFSETS = {
            {-RADIUS, 0}, {RADIUS, 0}, {0, -RADIUS}, {0, RADIUS},
            {-RADIUS, -RADIUS}, {-RADIUS, RADIUS}, {RADIUS, -RADIUS}, {RADIUS, RADIUS}
    };

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        Holder<Biome> centerBiome = context.getLevel().getBiome(pos);

        for (int[] offset : OFFSETS) {
            BlockPos checkPos = pos.offset(offset[0], 0, offset[1]);
            Holder<Biome> checkBiome = context.getLevel().getBiome(checkPos);

            if (!checkBiome.is(centerBiome)) {
                return Stream.of(pos);
            }
        }

        return Stream.empty();
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModPlacementModifiers.BIOME_EDGE;
    }
}