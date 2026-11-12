package com.example.medica.effect;

import com.example.medica.ProjectMedica;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public class ModEffects {

    public static final Holder<MobEffect> BLEEDING =
            Registry.registerForHolder(
                    BuiltInRegistries.MOB_EFFECT,
                    ProjectMedica.id("bleeding"),
                    new BleedingEffect()
            );

    public static final Holder<MobEffect> RESPITE =
            Registry.registerForHolder(
                    BuiltInRegistries.MOB_EFFECT,
                    ProjectMedica.id("respite"),
                    new RespiteEffect()
            );

    public static void initialize() {
        ProjectMedica.LOGGER.info("正在注册状态效果");
    }
}
