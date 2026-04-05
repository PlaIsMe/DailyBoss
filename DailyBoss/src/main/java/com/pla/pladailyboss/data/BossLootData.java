package com.pla.pladailyboss.data;

import com.google.gson.JsonObject;
import com.pla.pladailyboss.enums.BossLootDataState;

import java.util.Collections;
import java.util.List;

public class BossLootData {
    public final List<String> lootTables;
    public final JsonObject nbt;
    public final BossLootDataState state;
    public final List<String> phases;
    public final boolean isWater;
    public final long encounterTimeoutMs;
    public final boolean disableMobLoot;
    public final List<CustomLootEntry> customLoot;
    public final int lootTableRolls;
    public final int customLootRolls;

    public BossLootData(List<String> lootTables, JsonObject nbt, BossLootDataState state) {
        this(lootTables, nbt, state, Collections.emptyList(), false, -1L, false, Collections.emptyList(), 5, 2);
    }

    public BossLootData(List<String> lootTables, JsonObject nbt, BossLootDataState state, List<String> phases) {
        this(lootTables, nbt, state, phases, false, -1L, false, Collections.emptyList(), 5, 2);
    }

    public BossLootData(List<String> lootTables, JsonObject nbt, BossLootDataState state, List<String> phases, boolean isWater) {
        this(lootTables, nbt, state, phases, isWater, -1L, false, Collections.emptyList(), 5, 2);
    }

    public BossLootData(List<String> lootTables, JsonObject nbt, BossLootDataState state,
                        List<String> phases, boolean isWater,
                        long encounterTimeoutMs,
                        boolean disableMobLoot,
                        List<CustomLootEntry> customLoot,
                        int lootTableRolls,
                        int customLootRolls) {
        this.lootTables = lootTables == null ? Collections.emptyList() : lootTables;
        this.nbt = nbt == null ? new JsonObject() : nbt;
        this.state = state;
        this.phases = (phases == null ? Collections.emptyList() : List.copyOf(phases));
        this.isWater = isWater;

        this.encounterTimeoutMs = encounterTimeoutMs > 0L ? encounterTimeoutMs : -1L;
        this.disableMobLoot = disableMobLoot;
        this.customLoot = customLoot == null ? Collections.emptyList() : List.copyOf(customLoot);
        this.lootTableRolls = Math.max(0, lootTableRolls);
        this.customLootRolls = Math.max(0, customLootRolls);
    }

    public boolean isMultiPhase() {
        return phases != null && phases.size() > 1;
    }

    public boolean isWater() {
        return isWater;
    }
}
