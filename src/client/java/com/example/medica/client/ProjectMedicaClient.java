package com.example.medica.client;

import com.example.medica.block.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

public class ProjectMedicaClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WILD_MINT, RenderType.cutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MINT_CROP, RenderType.cutout());
	}
}
