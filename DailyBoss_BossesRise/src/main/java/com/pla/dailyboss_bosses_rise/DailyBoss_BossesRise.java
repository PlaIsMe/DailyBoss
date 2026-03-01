package com.pla.dailyboss_bosses_rise;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod(DailyBoss_BossesRise.MOD_ID)
public class DailyBoss_BossesRise
{
    public static final String MOD_ID = "dailyboss_bosses_rise";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Set<String> TRACKED_BOSSES = Set.of(
            "block_factorys_bosses:infernal_dragon",
            "block_factorys_bosses:underworld_knight",
            "block_factorys_bosses:yeti",
            "block_factorys_bosses:sandworm"
    );

    private static final Map<UUID, PendingKill> PENDING_KILLS = new HashMap<>();

    private record PendingKill(UUID killerId) {}

    public DailyBoss_BossesRise(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!(entity.level() instanceof ServerLevel)) return;

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String entityIdStr = entityId.toString();
        if (!TRACKED_BOSSES.contains(entityIdStr)) return;

        if (entity.getHealth() > 0.1f || entity.getHealth() <= 0.0f) return;

        Player nearest = entity.level().getNearestPlayer(entity, 64.0);
        if (nearest instanceof ServerPlayer killer) {
            PendingKill previous = PENDING_KILLS.get(entity.getUUID());
            UUID killerId = killer.getUUID();
            if (previous == null || !previous.killerId().equals(killerId)) {
                PENDING_KILLS.put(entity.getUUID(), new PendingKill(killerId));
            }
        }
    }

    @SubscribeEvent
    public void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        PendingKill pending = PENDING_KILLS.remove(event.getEntity().getUUID());
        if (pending == null) return;
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;

        ServerPlayer killer = serverLevel.getServer().getPlayerList().getPlayer(pending.killerId());
        if (killer == null) return;

        int currentKills = killer.getStats().getValue(Stats.ENTITY_KILLED.get(event.getEntity().getType()));
        if (currentKills == 0) {
            killer.awardStat(Stats.ENTITY_KILLED.get(event.getEntity().getType()), 1);
        }
    }
}
