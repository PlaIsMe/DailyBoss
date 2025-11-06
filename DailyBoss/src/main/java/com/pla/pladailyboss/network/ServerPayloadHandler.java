package com.pla.pladailyboss.network;

import com.pla.pladailyboss.data.DailyBossLoader;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleAskForDataMessage(final AskForDataMessage data, final IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        PacketDistributor.sendToPlayer(player, new BossListMessage(DailyBossLoader.getBossEntriesForPlayer(player, player.getServer())));
    }
}
