package com.pla.pladailyboss.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record Data(String message) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<Data> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("plamoneyget", "data"));
    public static final StreamCodec<ByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            Data::message,
            Data::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}