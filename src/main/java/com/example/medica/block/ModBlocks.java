package com.example.medica.block;

import com.example.medica.ProjectMedica;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public class ModBlocks {
    public static final ResourceLocation WILD_MINT_ID = ProjectMedica.id("wild_mint");

    public static final ResourceKey<Block> WILD_MINT_KEY =
            ResourceKey.create(Registries.BLOCK, WILD_MINT_ID);

    public static final Block WILD_MINT = Registry.register(
            BuiltInRegistries.BLOCK,
            WILD_MINT_KEY,
            new WildMintBlock(
                    BlockBehaviour.Properties.of()
                            .setId(WILD_MINT_KEY)
                            .noCollission()
                            .instabreak()
                            .sound(SoundType.GRASS)
            )
    );

    public static final ResourceKey<Block> MINT_CROP_KEY =
            ResourceKey.create(Registries.BLOCK, ProjectMedica.id("mint_crop"));

    public static final MintCropBlock MINT_CROP = Registry.register(
            BuiltInRegistries.BLOCK,
            MINT_CROP_KEY,
            new MintCropBlock(
                    BlockBehaviour.Properties.of()
                            .setId(MINT_CROP_KEY)
                            .noCollission()
                            .noOcclusion()
                            .randomTicks()
                            .instabreak()
                            .sound(SoundType.CROP)
                            .pushReaction(PushReaction.DESTROY)
            )
    );

    public static final ResourceLocation MINT_TEA_CAULDRON_ID =
            ProjectMedica.id("mint_tea_cauldron");

    public static final ResourceKey<Block> MINT_TEA_CAULDRON_KEY =
            ResourceKey.create(Registries.BLOCK, MINT_TEA_CAULDRON_ID);

    public static final MintTeaCauldronBlock MINT_TEA_CAULDRON = Registry.register(
            BuiltInRegistries.BLOCK,
            MINT_TEA_CAULDRON_KEY,
            new MintTeaCauldronBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)
                            .setId(MINT_TEA_CAULDRON_KEY)
            )
    );

    public static void initialize() {
        MintTeaCauldronBlock.registerInteractions();
        ProjectMedica.LOGGER.info("正在注册方块");
    }
}