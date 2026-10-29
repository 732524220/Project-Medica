package com.example.medica.item;

import com.example.medica.ProjectMedica;
import com.example.medica.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;

public class ModItems {
    public static final ResourceLocation MINT_ID = ProjectMedica.id("mint");

    public static final ResourceKey<Item> MINT_KEY =
            ResourceKey.create(Registries.ITEM, MINT_ID);

    public static final Item MINT = Registry.register(
            BuiltInRegistries.ITEM,
            MINT_KEY,
            new BlockItem(ModBlocks.MINT_CROP,
                    new Item.Properties().setId(MINT_KEY).useItemDescriptionPrefix())
    );

    // ===== 新增：薄荷茶成品 =====
    public static final ResourceLocation MINT_TEA_ID = ProjectMedica.id("mint_tea");

    public static final ResourceKey<Item> MINT_TEA_KEY =
            ResourceKey.create(Registries.ITEM, MINT_TEA_ID);

    public static final FoodProperties MINT_TEA_FOOD = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.3F)
            .alwaysEdible()
            .build();

    public static final Item MINT_TEA = Registry.register(
            BuiltInRegistries.ITEM,
            MINT_TEA_KEY,
            new Item(new Item.Properties()
                    .setId(MINT_TEA_KEY)
                    .stacksTo(16)
                    .food(MINT_TEA_FOOD, Consumables.DEFAULT_DRINK)
                    .usingConvertsTo(Items.GLASS_BOTTLE))
    );

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.accept(MINT));
        ProjectMedica.LOGGER.info("正在注册物品");
    }
}