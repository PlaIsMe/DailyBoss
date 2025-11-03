package com.pla.pladailyboss.data;

import com.pla.pladailyboss.enums.BossEntryState;

public class BossEntry {
    public final String name;
    public final BossEntryState state;

    public BossEntry(String name, BossEntryState state) {
        this.name = name;
        this.state = state;
    }
}
