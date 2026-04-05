package com.pla.pladailyboss.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pla.pladailyboss.config.PlaDailyBossConfig;
import com.pla.pladailyboss.enums.BossEntryState;
import com.pla.pladailyboss.enums.BossLootDataState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.stats.Stats;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class DailyBossLoader extends SimpleJsonResourceReloadListener {
    public static final Map<String, BossLootData> BOSS_LOOT_TABLES = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();

    public DailyBossLoader() {
        super(GSON, "dailyboss");
    }

    private boolean parseIsWater(JsonElement element) {
        if (!element.isJsonObject()) return false;
        JsonObject obj = element.getAsJsonObject();
        if (!obj.has("is_water")) return false;
        JsonElement w = obj.get("is_water");
        return w.isJsonPrimitive() && w.getAsJsonPrimitive().isBoolean() && w.getAsBoolean();
    }

    private List<String> parseLootTables(JsonElement element) {
        List<String> lootTables = new ArrayList<>();
        if (!element.isJsonObject()) return lootTables;
        JsonObject obj = element.getAsJsonObject();
        JsonElement lootElement = obj.get("loot_table");
        if (lootElement == null) return lootTables;
        if (lootElement.isJsonArray()) {
            for (JsonElement e : lootElement.getAsJsonArray()) {
                lootTables.add(e.getAsString());
            }
        } else if (lootElement.isJsonPrimitive()) {
            lootTables.add(lootElement.getAsString());
        }
        return lootTables;
    }

    private boolean parseBoolean(JsonObject obj, String key, boolean defaultValue) {
        if (!obj.has(key)) return defaultValue;
        JsonElement e = obj.get(key);
        return e.isJsonPrimitive() && e.getAsJsonPrimitive().isBoolean() ? e.getAsBoolean() : defaultValue;
    }

    private int parseInt(JsonObject obj, String key, int defaultValue) {
        if (!obj.has(key)) return defaultValue;
        JsonElement e = obj.get(key);
        return e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber() ? e.getAsInt() : defaultValue;
    }

    private long parseLong(JsonObject obj, String key, long defaultValue) {
        if (!obj.has(key)) return defaultValue;
        JsonElement e = obj.get(key);
        return e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber() ? e.getAsLong() : defaultValue;
    }

    private List<CustomLootEntry> parseCustomLoot(JsonElement element) {
        List<CustomLootEntry> list = new ArrayList<>();
        if (!element.isJsonObject()) return list;

        JsonObject obj = element.getAsJsonObject();
        JsonElement custom = obj.get("custom_loot");
        if (custom == null) return list;

        if (custom.isJsonArray()) {
            for (JsonElement e : custom.getAsJsonArray()) {
                addCustomLootEntry(e, list);
            }
        } else {
            addCustomLootEntry(custom, list);
        }

        return list;
    }

    private void addCustomLootEntry(JsonElement element, List<CustomLootEntry> list) {
        if (element.isJsonPrimitive()) {
            list.add(new CustomLootEntry(element.getAsString(), 1));
            return;
        }

        if (!element.isJsonObject()) return;

        JsonObject obj = element.getAsJsonObject();
        if (!obj.has("item")) return;

        String itemId = obj.get("item").getAsString();
        int count = parseInt(obj, "count", 1);
        list.add(new CustomLootEntry(itemId, count));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        BOSS_LOOT_TABLES.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectMap.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement element = entry.getValue();

            List<String> lootTables = parseLootTables(element);
            BossLootDataState bossLootDataState = BossLootDataState.AVAILABLE;

            String[] splitPath = id.getPath().split("/", 2);
            if (splitPath.length < 2) {
                LOGGER.warn("[DailyBoss] Skipping malformed resource location: {}", id);
                continue;
            }
            String namespace = splitPath[0];
            String mobPath = splitPath[1];

            if (!ModList.get().isLoaded(namespace)) {
                bossLootDataState = BossLootDataState.UN_AVAILABLE;
            }
            String mobId = String.format("%s:%s", namespace, mobPath);
            JsonObject obj = element.getAsJsonObject();
            long encounterTimeoutMs = parseLong(obj, "encounter_timeout_ms", -1L);
            boolean disableMobLoot = parseBoolean(obj, "disable_mob_loot", false);
            List<CustomLootEntry> customLoot = parseCustomLoot(element);
            int lootTableRolls = parseInt(obj, "loot_table_rolls", 5);
            int customLootRolls = parseInt(obj, "custom_loot_rolls", 2);
            JsonObject nbt = obj.has("nbt") && obj.get("nbt").isJsonObject() ? obj.getAsJsonObject("nbt") : new JsonObject();

            List<String> phases = parsePhases(element);
            boolean isWater = parseIsWater(element);
            BOSS_LOOT_TABLES.put(mobId, new BossLootData(
                    lootTables,
                    nbt,
                    bossLootDataState,
                    phases,
                    isWater,
                    encounterTimeoutMs,
                    disableMobLoot,
                    customLoot,
                    lootTableRolls,
                    customLootRolls
            ));
        }
    }

    private List<String> parsePhases(JsonElement element) {
        List<String> phases = new ArrayList<>();
        if (!element.isJsonObject()) return phases;
        JsonObject obj = element.getAsJsonObject();
        JsonElement p = obj.get("phases");
        if (p == null) return phases;
        if (p.isJsonArray()) {
            JsonArray arr = p.getAsJsonArray();
            for (JsonElement e : arr) {
                if (e.isJsonPrimitive()) {
                    phases.add(e.getAsString());
                }
            }
        } else if (p.isJsonPrimitive()) {
            phases.add(p.getAsString());
        }
        return phases;
    }

    public static List<String> getListBasedOnKilledMob(ServerPlayer player, MinecraftServer server) {
        return BOSS_LOOT_TABLES.entrySet().stream()
                .filter(entry -> entry.getValue().state == BossLootDataState.AVAILABLE)
                .map(Map.Entry::getKey)
                .filter(mobId -> {
                    // Force unlock config
                    if (PlaDailyBossConfig.FORCE_UNLOCK.get()) {
                        return true;
                    }

                    // Unlock by default
                    if (Objects.equals(mobId, "brutalbosses:randomboss")) {
                        return true;
                    }
                    // Memory Check
                    String[] parts = mobId.split(":", 2);
                    String namespace = parts.length > 1 ? parts[0] : "minecraft";
                    String path = parts.length > 1 ? parts[1] : parts[0];
                    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
                    EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(id);
                    int inMemoryKillCount = player.getStats().getValue(Stats.ENTITY_KILLED.get(entityType));
                    if (inMemoryKillCount > 0) {
                        return true;
                    }

                    // JSON check
                    int fromFile = StatsReader.getMobKillCountFromStatsFile(server, player, mobId);
                    return fromFile > 0;
                })
                .collect(Collectors.toList());
    }

    public static List<BossEntry> getBossEntriesForPlayer(ServerPlayer player, MinecraftServer server) {
        return BOSS_LOOT_TABLES.entrySet().stream()
                .map(entry -> {
                    String mobIdStr = entry.getKey();

                    BossLootData data = entry.getValue();
                    if (data.state == BossLootDataState.UN_AVAILABLE) {
                        return new BossEntry(mobIdStr, BossEntryState.NOT_INSTALLED);
                    }

                    if (Objects.equals(mobIdStr, "brutalbosses:randomboss")) {
                        return new BossEntry(mobIdStr, BossEntryState.DEFEATED);
                    }

                    String[] parts = mobIdStr.split(":", 2);
                    String namespace = parts.length > 1 ? parts[0] : "minecraft";
                    String path = parts.length > 1 ? parts[1] : parts[0];
                    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
                    EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(id);

                    if (PlaDailyBossConfig.FORCE_UNLOCK.get()) {
                        return new BossEntry(mobIdStr, BossEntryState.DEFEATED);
                    }

                    int inMemoryKillCount = player.getStats().getValue(Stats.ENTITY_KILLED.get(entityType));
                    if (inMemoryKillCount > 0) {
                        return new BossEntry(mobIdStr, BossEntryState.DEFEATED);
                    }

                    int fromFile = StatsReader.getMobKillCountFromStatsFile(server, player, mobIdStr);
                    if (fromFile > 0) {
                        return new BossEntry(mobIdStr, BossEntryState.DEFEATED);
                    }

                    return new BossEntry(mobIdStr, BossEntryState.UNDEFEATED);
                })
                .sorted(Comparator.comparingInt(entry -> getSortPriority(entry.state)))
                .collect(Collectors.toList());
    }

    private static int getSortPriority(BossEntryState state) {
        return switch (state) {
            case DEFEATED -> 0;
            case UNDEFEATED -> 1;
            case NOT_INSTALLED -> 2;
        };
    }
}

