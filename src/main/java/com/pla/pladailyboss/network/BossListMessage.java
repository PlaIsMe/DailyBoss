package com.pla.pladailyboss.network;

import com.pla.pladailyboss.client.screen.BossScreen;
import com.pla.pladailyboss.data.BossEntry;
import com.pla.pladailyboss.enums.BossEntryState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BossListMessage {
    private final List<BossEntry> bossList;

    public BossListMessage(List<BossEntry> bossList) {
        this.bossList = bossList;
    }

    public List<BossEntry> getBossList() {
        return bossList;
    }

    public static void encode(BossListMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.bossList.size());
        for (BossEntry entry : msg.bossList) {
            buf.writeUtf(entry.name);
            buf.writeEnum(entry.state);
        }
    }

    public static BossListMessage decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<BossEntry> bossList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String name = buf.readUtf();
            BossEntryState state = buf.readEnum(BossEntryState.class);
            bossList.add(new BossEntry(name, state));
        }
        return new BossListMessage(bossList);
    }

    public static void handle(BossListMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    BossScreen screen = BossScreen.getInstance();
                    if (screen != null) {
                        screen.setEntityIdStrings(msg.getBossList());
                    }
                })
        );
        ctx.get().setPacketHandled(true);
    }
}