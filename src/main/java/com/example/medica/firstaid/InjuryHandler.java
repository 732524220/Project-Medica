package com.example.medica.firstaid;

import com.example.medica.effect.ModEffects;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Spider;

public class InjuryHandler {

    public static void initialize() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (!(entity instanceof ServerPlayer player)) {
                return;
            }

            if (damageTaken <= 0) {
                return;
            }

            if (!isSpiderHit(source) && !source.is(DamageTypes.SWEET_BERRY_BUSH)) {
                return;
            }

            if (!player.hasEffect(ModEffects.BLEEDING)) {
                player.addEffect(new MobEffectInstance(ModEffects.BLEEDING, 1200, 0));
            }
        });
    }

    private static boolean isSpiderHit(DamageSource source) {
        Entity attacker = source.getEntity();
        return attacker instanceof Spider;
    }
}
