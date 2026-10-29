package com.example.medica;

import com.example.medica.block.ModBlocks;
import com.example.medica.block.entity.ModBlockEntities;
import com.example.medica.item.ModItems;
import com.example.medica.worldgen.ModPlacementModifiers;
import com.example.medica.worldgen.ModWorldGeneration;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectMedica implements ModInitializer {
	public static final String MOD_ID = "project-medica";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModItems.initialize();
		ModPlacementModifiers.initialize();
		ModWorldGeneration.initialize();
		LOGGER.info("但愿世间人无病，何妨架上药生尘");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
