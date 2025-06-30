package com.pla.pladailyboss.network;

import com.pla.pladailyboss.data.DailyBossLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class AskForDataMessage {
    private static final Logger LOGGER = LogManager.getLogger();

    public AskForDataMessage() {
    }

    public static void encode(AskForDataMessage msg, FriendlyByteBuf buf) {
    }

    public static AskForDataMessage decode(FriendlyByteBuf buf) {
        return new AskForDataMessage();
    }

    public static void handle(AskForDataMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null) {
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new BossListMessage(DailyBossLoader.getBossEntriesForPlayer(player, player.server))
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
