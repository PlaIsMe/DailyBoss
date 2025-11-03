package com.pla.pladailyboss.data;

import com.google.gson.JsonObject;
import com.pla.pladailyboss.enums.BossLootDataState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BossLootData {
    public final List<String> lootTables;
    public final JsonObject nbt;
    public final BossLootDataState state;
    public final List<String> phases;

    public BossLootData(List<String> lootTables, JsonObject nbt, BossLootDataState state) {
        this(lootTables, nbt, state, Collections.emptyList());
    }

    public BossLootData(List<String> lootTables, JsonObject nbt, BossLootDataState state, List<String> phases) {
        this.lootTables = lootTables == null ? Collections.emptyList() : lootTables;
        this.nbt = nbt == null ? new JsonObject() : nbt;
        this.state = state;
        this.phases = (phases == null ? Collections.emptyList() : List.copyOf(phases));
    }

    public boolean isMultiPhase() {
        return phases != null && phases.size() > 1;
    }
}
