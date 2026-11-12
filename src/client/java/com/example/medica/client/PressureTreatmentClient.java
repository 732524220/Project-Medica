package com.example.medica.client;

import com.example.medica.firstaid.PressureTreatment;
import com.example.medica.network.PressureInputPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class PressureTreatmentClient {

    private static boolean wasPressing;

    private PressureTreatmentClient() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(PressureTreatmentClient::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        boolean canSend = client.player != null
                && client.getConnection() != null
                && ClientPlayNetworking.canSend(PressureInputPayload.TYPE);

        boolean pressing = canSend
                && client.screen == null
                && client.isWindowActive()
                && client.options.keyUse.isDown()
                && PressureTreatment.canStart(client.player);

        if (canSend && pressing != wasPressing) {
            ClientPlayNetworking.send(new PressureInputPayload(pressing));
        }

        wasPressing = pressing;
    }
}
