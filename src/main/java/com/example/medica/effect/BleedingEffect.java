package com.example.medica.effect;

import com.example.medica.ProjectMedica;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BleedingEffect extends MobEffect {

    private static final int DAMAGE_INTERVAL_TICKS = 100;

    private static final AttachmentType<Integer> BLEEDING_TICKS =
            AttachmentRegistry.createDefaulted(
                    ProjectMedica.id("bleeding_ticks"),
                    () -> 0
            );

    private static final ResourceKey<DamageType> BLEEDING_DAMAGE_KEY =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    ProjectMedica.id("bleeding")
            );

    public BleedingEffect() {
        super(MobEffectCategory.HARMFUL, 0xA82020);
    }

    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier) {
        super.onEffectAdded(entity, amplifier);

        if (entity instanceof ServerPlayer) {
            entity.setAttached(BLEEDING_TICKS, 0);
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (!(entity instanceof ServerPlayer)) {
            return true;
        }

        if (entity.hasEffect(ModEffects.RESPITE)) {
            return true;
        }

        int ticks = entity.getAttachedOrCreate(BLEEDING_TICKS) + 1;

        if (ticks >= DAMAGE_INTERVAL_TICKS) {
            entity.setAttached(BLEEDING_TICKS, 0);

            DamageSource bleedingSource = new DamageSource(
                    level.registryAccess()
                            .lookupOrThrow(Registries.DAMAGE_TYPE)
                            .getOrThrow(BLEEDING_DAMAGE_KEY)
            );

            entity.hurtServer(level, bleedingSource, 1.0F);
        } else {
            entity.setAttached(BLEEDING_TICKS, ticks);
        }

        return true;
    }
}
