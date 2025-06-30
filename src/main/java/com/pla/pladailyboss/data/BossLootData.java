package com.pla.pladailyboss.data;

import com.google.gson.JsonObject;

import java.util.List;

public class BossLootData {
    public final List<String> lootTables;
    public final JsonObject nbt;

    public BossLootData(List<String> lootTables, JsonObject nbt) {
        this.lootTables = lootTables;
        this.nbt = nbt;
    }
}
