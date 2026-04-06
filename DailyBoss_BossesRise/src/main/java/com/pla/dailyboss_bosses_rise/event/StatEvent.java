package com.pla.dailyboss_bosses_rise.event;


import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber
public class StatEvent {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Set<String> TRACKED_BOSSES = Set.of(
            "block_factorys_bosses:infernal_dragon",
            "block_factorys_bosses:underworld_knight",
            "block_factorys_bosses:yeti",
            "block_factorys_bosses:sandworm"
    );

    private static final Map<UUID, PendingKill> PENDING_KILLS = new HashMap<>();

    private record PendingKill(UUID killerId) {}

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
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
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
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
