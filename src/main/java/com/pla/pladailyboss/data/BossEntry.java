package com.pla.pladailyboss.data;

import com.pla.pladailyboss.enums.BossEntryState;

public class BossEntry {
    public final String name;
    public final BossEntryState state;
    public final String message;

    public BossEntry(String name, BossEntryState state, String message) {
        this.name = name;
        this.state = state;
        this.message = message;
    }
}
