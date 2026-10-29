package com.example.medica.client;

import com.example.medica.block.ModBlocks;
import com.example.medica.item.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

public class ProjectMedicaClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WILD_MINT, RenderType.cutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MINT_CROP, RenderType.cutout());

		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			if (stack.is(ModItems.MINT)) {
				lines.add(Component.translatable("tooltip.project-medica.mint.planting"));
				lines.add(Component.translatable("tooltip.project-medica.mint.brewing"));
			}

			if (stack.is(ModItems.MINT_TEA)) {
				// 添加饮用收益提示
				lines.add(Component.translatable("tooltip.project-medica.mint_tea.drinking"));
				// 添加返还玻璃瓶提示
				lines.add(Component.translatable("tooltip.project-medica.mint_tea.container"));
			}
		});
	}
}