package com.pla.pladailyboss.network;

import com.pla.pladailyboss.data.BossEntry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record BossListMessage(List<BossEntry> bossEntryList) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BossListMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("pladailyboss", "bost_list_message_data"));
    public static final StreamCodec<ByteBuf, BossListMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, BossEntry.STREAM_CODEC),
            BossListMessage::bossEntryList,
            BossListMessage::new
    );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
