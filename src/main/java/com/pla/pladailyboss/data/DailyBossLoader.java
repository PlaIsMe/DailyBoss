package com.pla.pladailyboss.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pla.pladailyboss.enums.BossEntryState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.stats.Stats;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
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

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        BOSS_LOOT_TABLES.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectMap.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement element = entry.getValue();

            List<String> lootTables = parseLootTables(element);

            String[] splitPath = id.getPath().split("/", 2);
            if (splitPath.length < 2) {
                LOGGER.warn("[DailyBoss] Skipping malformed resource location: {}", id);
                continue;
            }
            String namespace = splitPath[0];
            String mobPath = splitPath[1];

            if (!ModList.get().isLoaded(namespace)) {
                LOGGER.warn("[DailyBoss] Skipping '{}' because mod '{}' is not loaded.", id, namespace);
                continue;
            }

            ResourceLocation mobId = ResourceLocation.fromNamespaceAndPath(namespace, mobPath);
            if (!ForgeRegistries.ENTITY_TYPES.containsKey(mobId)) {
                LOGGER.warn("[DailyBoss] Skipping '{}' because entity '{}' is not registered.", id, mobId);
                continue;
            }

            JsonObject obj = element.getAsJsonObject();
            JsonObject nbt = obj.has("nbt") && obj.get("nbt").isJsonObject() ? obj.getAsJsonObject("nbt") : new JsonObject();
            JsonElement messageElement = obj.get("message");
            String message = messageElement != null && messageElement.isJsonPrimitive()
                    ? messageElement.getAsString()
                    : "";

            BOSS_LOOT_TABLES.put(mobId.toString(), new BossLootData(lootTables, nbt, message));
        }

        LOGGER.info("[DailyBoss] Loaded loot table map:");
        BOSS_LOOT_TABLES.forEach((mob, data) -> {
            LOGGER.info("Mob: {}", mob);
            data.lootTables.forEach(loot -> LOGGER.info("  -> {}", loot));
            if (!data.nbt.entrySet().isEmpty()) {
                LOGGER.info("  NBT: {}", data.nbt);
            }
        });
    }

    public static List<String> getListBasedOnKilledMob(ServerPlayer player, MinecraftServer server) {
        return BOSS_LOOT_TABLES.keySet().stream()
                .filter(mobId -> {
                    // Memory Check
                    String[] parts = mobId.split(":");
                    EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]));
                    if (entityType != null) {
                        int inMemoryKillCount = player.getStats().getValue(Stats.ENTITY_KILLED.get(entityType));
                        if (inMemoryKillCount > 0) {
                            return true;
                        }
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

                    String[] parts = mobIdStr.split(":");
                    ResourceLocation mobId = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
                    EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(mobId);

                    if (entityType == null) {
                        return new BossEntry(mobIdStr, BossEntryState.NOT_INSTALLED, data.message);
                    }

                    int inMemoryKillCount = player.getStats().getValue(Stats.ENTITY_KILLED.get(entityType));
                    if (inMemoryKillCount > 0) {
                        return new BossEntry(mobIdStr, BossEntryState.DEFEAT, data.message);
                    }

                    int fromFile = StatsReader.getMobKillCountFromStatsFile(server, player, mobIdStr);
                    if (fromFile > 0) {
                        return new BossEntry(mobIdStr, BossEntryState.DEFEAT, data.message);
                    }

                    return new BossEntry(mobIdStr, BossEntryState.NOT_DEFEAT, data.message);
                })
                .collect(Collectors.toList());
    }


}

