package com.pla.pladailyboss.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleAskForDataMessage(final AskForDataMessage data, final IPayloadContext context) {
//        ctx.get().enqueueWork(() -> {
//            var player = ctx.get().getSender();
//            if (player != null) {
//                NetworkHandler.INSTANCE.send(
//                        PacketDistributor.PLAYER.with(() -> player),
//                        new BossListMessage(DailyBossLoader.getBossEntriesForPlayer(player, player.server))
//                );
//            }
//        });
//        ctx.get().setPacketHandled(true);
    }
}
