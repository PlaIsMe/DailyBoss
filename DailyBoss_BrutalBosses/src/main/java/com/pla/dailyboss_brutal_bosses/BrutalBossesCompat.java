package com.pla.dailyboss_brutal_bosses;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.compat.Compat;
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
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BrutalBossesCompat extends BossSpawnHandler {
    private static ConcurrentLinkedQueue<Tuple<BlockPos, BossType>> spawns = new ConcurrentLinkedQueue<>();

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
                return null;
            }

            List<Entity> passengers = new ArrayList<>();
            Entity boss = EntityType.loadEntityRecursive(bossTag, world.getLevel(), e -> {
                e.setUUID(UUID.randomUUID());
                passengers.add(e);
                return e;
            });
            passengers.remove(boss);
            if (boss == null) {
                return null;
            }
            boss.setUUID(UUID.randomUUID());
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

            bossType.initForEntity((Mob) boss);
            ((Mob) boss).setHealth(((Mob) boss).getMaxHealth());
            if (boss instanceof AbstractVillager abstractVillager)
            {
                abstractVillager.offers = new MerchantOffers();
            }

            Objects.requireNonNull(((BossCapEntity) boss).getBossCap()).setSpawnPos(pos);
            Compat.applyAllCompats(world, bossType, pos, boss);

            if (!boss.isRemoved())
            {
                world.addFreshEntity(boss);
                for (final Entity passenger : passengers)
                {
                    if (passenger instanceof BossCapEntity bossPassenger && bossPassenger.getBossCap() != null)
                    {
                        bossPassenger.getBossCap().setSpawnPos(spawnPos);
                    }
                    passenger.setPos(boss.position());
                    if (passenger instanceof AbstractVillager abstractVillager)
                    {
                        abstractVillager.offers = new MerchantOffers();
                    }
                    world.addFreshEntity(passenger);
                }
            }

            return boss instanceof Mob mob ? mob : null;
        }
        catch (Exception spawnException)
        {
            BrutalBosses.LOGGER.error("Boss: " + bossType.getID() + " failed to spawn! Error:", spawnException);
            return null;
        }
    }

    public static @Nullable Mob spawnRandomBossAndReturn(final ServerLevel world, final BlockPos pos)
    {
        final List<BossType> list = new ArrayList<>(BossTypeManager.instance.bosses.values());
        final BossType bossType = list.get(BrutalBosses.rand.nextInt(list.size()));
        return spawnBossAndReturn(world, pos, bossType);
    }
}
