package com.example.medica.worldgen;

import com.example.medica.ProjectMedica;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class ModPlacementModifiers {
    public static final PlacementModifierType<BiomeEdgePlacementModifier> BIOME_EDGE =
            Registry.register(
                    BuiltInRegistries.PLACEMENT_MODIFIER_TYPE,
                    ProjectMedica.id("biome_edge"),
                    () -> BiomeEdgePlacementModifier.CODEC
            );

    public static final PlacementModifierType<RiverbankPlacementModifier> RIVERBANK =
            Registry.register(
                    BuiltInRegistries.PLACEMENT_MODIFIER_TYPE,
                    ProjectMedica.id("riverbank"),
                    () -> RiverbankPlacementModifier.CODEC
            );

    public static void initialize() {
    }
}
