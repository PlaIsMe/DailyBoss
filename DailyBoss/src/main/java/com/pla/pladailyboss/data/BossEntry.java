package com.pla.pladailyboss.data;

import com.pla.pladailyboss.enums.BossEntryState;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class BossEntry {
    public final String name;
    public final BossEntryState state;

    public BossEntry(String name, BossEntryState state) {
        this.name = name;
        this.state = state;
    }

    public static final StreamCodec<ByteBuf, BossEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            entry -> entry.name,

            ByteBufCodecs.STRING_UTF8.map(
                    BossEntryState::valueOf,
                    BossEntryState::name
            ),
            entry -> entry.state,

            BossEntry::new
    );
}
