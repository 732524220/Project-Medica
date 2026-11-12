package com.example.medica.network;

import com.example.medica.ProjectMedica;
import com.example.medica.firstaid.PressureTreatment;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ModNetworking {

    public static void initialize() {
        PayloadTypeRegistry.playC2S().register(
                PressureInputPayload.TYPE,
                PressureInputPayload.STREAM_CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                PressureInputPayload.TYPE,
                (payload, context) -> {
                    context.server().execute(() ->
                            PressureTreatment.setPressing(context.player(), payload.pressing())
                    );
                }
        );
    }
}
