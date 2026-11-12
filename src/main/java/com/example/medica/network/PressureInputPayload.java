package com.example.medica.network;

import com.example.medica.ProjectMedica;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PressureInputPayload(boolean pressing) implements CustomPacketPayload {

    public static final Type<PressureInputPayload> TYPE =
            new Type<>(ProjectMedica.id("pressure_input"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PressureInputPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    PressureInputPayload::pressing,
                    PressureInputPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
