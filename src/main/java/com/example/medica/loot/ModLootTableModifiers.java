package com.example.medica.loot;

import com.example.medica.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
// [新增] 以下导入
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;

public class ModLootTableModifiers {

    // 普通矮草的掉落表 ID
    private static final ResourceLocation SHORT_GRASS_LOOT_TABLE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "blocks/short_grass");

    // 高草丛的掉落表 ID
    private static final ResourceLocation TALL_GRASS_LOOT_TABLE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "blocks/tall_grass");

    // 辅助方法：判断高草丛的上/下半部
    private static LootItemCondition.Builder tallGrassHalf(DoubleBlockHalf half) {
        return LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(Blocks.TALL_GRASS)
                .setProperties(
                        StatePropertiesPredicate.Builder.properties()
                                .hasProperty(DoublePlantBlock.HALF, half)
                );
    }

    // 辅助方法：判断相邻的另一半是否还在
    private static LootItemCondition.Builder tallGrassNeighbor(
            HolderGetter<Block> blocks,
            DoubleBlockHalf half,
            int offsetY
    ) {
        return LocationCheck.checkLocation(
                LocationPredicate.Builder.location()
                        .setBlock(
                                BlockPredicate.Builder.block()
                                        .of(blocks, Blocks.TALL_GRASS)
                                        .setProperties(
                                                StatePropertiesPredicate.Builder.properties()
                                                        .hasProperty(DoublePlantBlock.HALF, half)
                                        )
                        ),
                new BlockPos(0, offsetY, 0)
        );
    }

    public static void initialize() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }

            // 同时判断矮草和高草丛
            boolean isShortGrass = key.location().equals(SHORT_GRASS_LOOT_TABLE);
            boolean isTallGrass = key.location().equals(TALL_GRASS_LOOT_TABLE);

            if (!isShortGrass && !isTallGrass) {
                return;
            }

            // 矮草掉落
            if (isShortGrass) {
                tableBuilder.withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(ModItems.PLANT_FIBER))
                );
            }

            // [新增] 高草丛掉落
            if (isTallGrass) {
                var blocks = registries.lookupOrThrow(Registries.BLOCK);

                tableBuilder.withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(
                                        AnyOfCondition.anyOf(
                                                // 条件1：自己是下半部，且上方一格是上半部
                                                AllOfCondition.allOf(
                                                        tallGrassHalf(DoubleBlockHalf.LOWER),
                                                        tallGrassNeighbor(blocks, DoubleBlockHalf.UPPER, 1)
                                                ),
                                                // 条件2：自己是上半部，且下方一格是下半部
                                                AllOfCondition.allOf(
                                                        tallGrassHalf(DoubleBlockHalf.UPPER),
                                                        tallGrassNeighbor(blocks, DoubleBlockHalf.LOWER, -1)
                                                )
                                        )
                                )
                                .add(LootItem.lootTableItem(ModItems.PLANT_FIBER))
                );
            }
        });
    }
}
