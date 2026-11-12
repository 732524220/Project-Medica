package com.example.medica.firstaid;

import com.example.medica.effect.ModEffects;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PressureTreatment {

    public static final int REQUIRED_TICKS = 160;
    public static final int RESPITE_TICKS = 300;
    private static final int ACTIVE_MESSAGE_INTERVAL_TICKS = 10;

    private static final Map<UUID, TreatmentProgress> ACTIVE_TREATMENTS = new HashMap<>();

    private static final class TreatmentProgress {
        private final ServerPlayer player;
        private int ticks;

        private TreatmentProgress(ServerPlayer player) {
            this.player = player;
        }
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                MobEffectInstance bleeding = player.getEffect(ModEffects.BLEEDING);
                if (bleeding != null && player.hasEffect(ModEffects.RESPITE)) {
                    player.addEffect(new MobEffectInstance(
                            ModEffects.BLEEDING,
                            bleeding.getDuration() + 1,
                            bleeding.getAmplifier(),
                            bleeding.isAmbient(),
                            bleeding.isVisible(),
                            bleeding.showIcon()
                    ));
                }
            }

            for (TreatmentProgress progress : ACTIVE_TREATMENTS.values().toArray(TreatmentProgress[]::new)) {
                ServerPlayer player = progress.player;

                if (!canStart(player)) {
                    ACTIVE_TREATMENTS.remove(player.getUUID());
                    player.displayClientMessage(
                            Component.translatable("message.project-medica.pressure_treatment.interrupted"),
                            true
                    );
                    continue;
                }

                progress.ticks++;
                if (progress.ticks < REQUIRED_TICKS
                        && progress.ticks % ACTIVE_MESSAGE_INTERVAL_TICKS == 0) {
                    player.displayClientMessage(
                            Component.translatable("message.project-medica.pressure_treatment.active"),
                            true
                    );
                }

                if (progress.ticks >= REQUIRED_TICKS) {
                    player.addEffect(new MobEffectInstance(ModEffects.RESPITE, RESPITE_TICKS, 0));
                    player.displayClientMessage(
                            Component.translatable("message.project-medica.pressure_treatment.respite"),
                            true
                    );
                    ACTIVE_TREATMENTS.remove(player.getUUID());
                }
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                ACTIVE_TREATMENTS.remove(handler.player.getUUID())
        );
    }

    public static void setPressing(ServerPlayer player, boolean pressing) {
        if (!pressing) {
            ACTIVE_TREATMENTS.remove(player.getUUID());
            return;
        }

        if (canStart(player)) {
            ACTIVE_TREATMENTS.put(player.getUUID(), new TreatmentProgress(player));
            player.displayClientMessage(
                    Component.translatable("message.project-medica.pressure_treatment.active"),
                    true
            );
        }
    }

    public static boolean canStart(Player player) {
        return player.isAlive()
                && !player.isSpectator()
                && player.hasEffect(ModEffects.BLEEDING)
                && player.isShiftKeyDown()
                && player.getMainHandItem().isEmpty()
                && player.getOffhandItem().isEmpty();
    }
}
