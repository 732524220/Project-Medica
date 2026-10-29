package com.example.medica.block.entity;

import com.example.medica.ProjectMedica;
import com.example.medica.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final BlockEntityType<MintTeaCauldronBlockEntity> MINT_TEA_CAULDRON =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    ProjectMedica.id("mint_tea_cauldron"),
                    FabricBlockEntityTypeBuilder.create(
                            MintTeaCauldronBlockEntity::new,
                            ModBlocks.MINT_TEA_CAULDRON
                    ).build()
            );

    private ModBlockEntities() {
    }

    public static void initialize() {
        ProjectMedica.LOGGER.info("正在注册方块实体");
    }
}
