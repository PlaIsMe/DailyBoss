package com.pla.pladailyboss.network;
import com.pla.pladailyboss.client.screen.BossScreen;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class ClientPayloadHandler {
    public static void handleBossListMessage(final BossListMessage data, final IPayloadContext context) {
        BossScreen screen = BossScreen.getInstance();
        if (screen != null) {
            screen.setEntityIdStrings(data.bossEntryList());
        }
    }
}