package com.example.medica.worldgen;

import com.example.medica.ProjectMedica;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class ModWorldGeneration {
    public static final ResourceKey<PlacedFeature> WILD_MINT_PATCH =
            ResourceKey.create(Registries.PLACED_FEATURE, ProjectMedica.id("wild_mint_patch"));

    public static final ResourceKey<PlacedFeature> WILD_MINT_SECONDARY_PATCH =
            ResourceKey.create(Registries.PLACED_FEATURE, ProjectMedica.id("wild_mint_secondary_patch"));

    public static final ResourceKey<PlacedFeature> WILD_MINT_BIOME_EDGE_PATCH =
            ResourceKey.create(Registries.PLACED_FEATURE, ProjectMedica.id("wild_mint_biome_edge_patch"));

    public static final ResourceKey<PlacedFeature> WILD_MINT_RIVERBANK_PATCH =
            ResourceKey.create(Registries.PLACED_FEATURE, ProjectMedica.id("wild_mint_riverbank_patch"));

    public static void initialize() {
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(Biomes.FLOWER_FOREST),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                WILD_MINT_PATCH
        );

        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.MEADOW),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                WILD_MINT_SECONDARY_PATCH
        );

        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(
                        Biomes.FOREST,
                        Biomes.BIRCH_FOREST,
                        Biomes.SWAMP,
                        Biomes.MANGROVE_SWAMP
                ),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                WILD_MINT_BIOME_EDGE_PATCH
        );

        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld().and(
                        BiomeSelectors.excludeByKey(
                                Biomes.RIVER,
                                Biomes.FROZEN_RIVER,
                                Biomes.OCEAN,
                                Biomes.DEEP_OCEAN,
                                Biomes.COLD_OCEAN,
                                Biomes.DEEP_COLD_OCEAN,
                                Biomes.FROZEN_OCEAN,
                                Biomes.DEEP_FROZEN_OCEAN,
                                Biomes.LUKEWARM_OCEAN,
                                Biomes.DEEP_LUKEWARM_OCEAN,
                                Biomes.WARM_OCEAN
                        )
                ),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                WILD_MINT_RIVERBANK_PATCH
        );
    }
}
