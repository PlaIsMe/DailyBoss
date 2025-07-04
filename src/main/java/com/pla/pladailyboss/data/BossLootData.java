package com.pla.pladailyboss.data;

import com.google.gson.JsonObject;
import com.pla.pladailyboss.enums.BossLootDataState;

import java.util.List;

public class BossLootData {
    public final List<String> lootTables;
    public final JsonObject nbt;
    public final BossLootDataState state;

    public BossLootData(List<String> lootTables, JsonObject nbt, BossLootDataState state) {
        this.lootTables = lootTables;
        this.nbt = nbt;
        this.state = state;
    }
}
