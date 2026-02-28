package com.pla.dailyboss_brutal_bosses;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossSpawnHandler;
import com.brutalbosses.entity.BossType;
import com.brutalbosses.entity.BossTypeManager;
import com.brutalbosses.entity.capability.BossCapEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BrutalBossesCompat extends BossSpawnHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ConcurrentLinkedQueue<Tuple<BlockPos, BossType>> spawns = new ConcurrentLinkedQueue<>();

    public static @Nullable Mob spawnBossAndReturn(ServerLevelAccessor world, BlockPos pos, BossType bossType) {
        try
        {
            spawns.add(new Tuple<>(pos, bossType));
            if (spawns.size() > 20)
            {
                spawns.poll();
            }

            final CompoundTag bossTag = bossType.createBossTag(world.getLevel());
            if (bossTag == null)
            {
                LOGGER.warn("[DailyBoss] BrutalBosses createBossTag returned null for {}", bossType.getID());
                return null;
            }

            Entity boss = EntityType.loadEntityRecursive(bossTag, world.getLevel(), e -> {
                e.setUUID(UUID.randomUUID());
                return e;
            });

            if (boss == null)
            {
                LOGGER.warn("[DailyBoss] BrutalBosses loadEntityRecursive returned null for {}", bossType.getID());
                return null;
            }

            boss.setUUID(UUID.randomUUID());

            if (!(boss instanceof Mob mob))
            {
                LOGGER.warn("[DailyBoss] BrutalBosses entity is not a Mob for {}", bossType.getID());
                boss.remove(Entity.RemovalReason.DISCARDED);
                return null;
            }

            final BlockPos spawnPos = findSpawnPosForBoss(world, (LivingEntity) boss, pos);
            if (spawnPos == null)
            {
                boss.remove(Entity.RemovalReason.DISCARDED);
                return null;
            }
            else
            {
                boss.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            }

            bossType.initForEntity(mob);
            mob.setHealth(mob.getMaxHealth());

            if (boss instanceof BossCapEntity bossCapEntity && bossCapEntity.getBossCap() != null)
            {
                bossCapEntity.getBossCap().setSpawnPos(pos);
            }

            if (!boss.isRemoved())
            {
                world.addFreshEntity(boss);
            }

            return mob;
        }
        catch (Exception spawnException)
        {
            LOGGER.error("[DailyBoss] BrutalBoss {} failed to spawn:", bossType.getID(), spawnException);
            return null;
        }
    }

    public static @Nullable Mob spawnRandomBossAndReturn(final ServerLevel world, final BlockPos pos)
    {
        if (BossTypeManager.instance == null || BossTypeManager.instance.bosses == null
                || BossTypeManager.instance.bosses.isEmpty())
        {
            LOGGER.warn("[DailyBoss] BrutalBosses has no bosses registered, cannot spawn random boss");
            return null;
        }

        final List<BossType> list = new ArrayList<>(BossTypeManager.instance.bosses.values());
        final BossType bossType = list.get(BrutalBosses.rand.nextInt(list.size()));
        return spawnBossAndReturn(world, pos, bossType);
    }
}
