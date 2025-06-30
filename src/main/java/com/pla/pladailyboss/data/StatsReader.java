package com.pla.pladailyboss.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class StatsReader {
    private static final Logger LOGGER = LogManager.getLogger();

    public static int getMobKillCountFromStatsFile(MinecraftServer server, ServerPlayer player, String mobId) {
        UUID playerUUID = player.getUUID();
        Path statsFilePath = server.getWorldPath(LevelResource.PLAYER_STATS_DIR).resolve(playerUUID + ".json");

        if (!Files.exists(statsFilePath)) {
            return -1;
        }

        try (FileReader reader = new FileReader(statsFilePath.toFile())) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject stats = root.getAsJsonObject("stats");

            if (stats == null) return -1;

            JsonObject killed = stats.getAsJsonObject("minecraft:killed");
            if (killed == null) return 0;

            JsonElement killCountElement = killed.get(mobId);
            if (killCountElement == null) return 0;

            return killCountElement.getAsInt();

        } catch (Exception e) {
            LOGGER.warn("[DailyBoss] Failed to read stats file for player {}", player.getScoreboardName(), e);
            return -1;
        }
    }
}
