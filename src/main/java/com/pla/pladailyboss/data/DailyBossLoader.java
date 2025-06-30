package com.pla.pladailyboss.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class DailyBossLoader extends SimpleJsonResourceReloadListener {
    public static Map<String, List<String>> BOSS_LOOT_TABLES = new HashMap<>();
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

            ResourceLocation mobId = new ResourceLocation(namespace, mobPath);
            if (!ForgeRegistries.ENTITY_TYPES.containsKey(mobId)) {
                LOGGER.warn("[DailyBoss] Skipping '{}' because entity '{}' is not registered.", id, mobId);
                continue;
            }
            BOSS_LOOT_TABLES.put(mobId.toString(), lootTables);
        }

        LOGGER.info("[DailyBoss] Loaded loot table map:");
        BOSS_LOOT_TABLES.forEach((mob, loots) -> {
            LOGGER.info("Mob: {}", mob);
            loots.forEach(loot -> LOGGER.info("  -> {}", loot));
        });
    }

    public static List<String> getListBasedOnKilledMob(ServerPlayer player, MinecraftServer server) {
        return BOSS_LOOT_TABLES.keySet().stream()
                .filter(mobId -> StatsReader.getMobKillCountFromStatsFile(server, player, mobId) > 0)
                .collect(Collectors.toList());
    }
}

