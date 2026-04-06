package com.pla.dailyboss_brutal_bosses;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.compat.Compat;
import com.brutalbosses.entity.BossSpawnHandler;
import com.brutalbosses.entity.BossType;
import com.brutalbosses.entity.BossTypeManager;
import com.brutalbosses.entity.capability.BossCapability;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BrutalBossesCompat extends BossSpawnHandler {
    private static final ConcurrentLinkedQueue<Tuple<BlockPos, BossType>> spawns = new ConcurrentLinkedQueue<>();

    public static @Nullable Mob spawnBossAndReturn(ServerLevelAccessor world, BlockPos pos, BossType bossType) {
        try {
            spawns.add(new Tuple(pos, bossType));
            if (spawns.size() > 20) {
                spawns.poll();
            }

            CompoundTag bossTag = bossType.createBossTag(world.getLevel());
            if (bossTag == null) {
                return null;
            }

            List<Entity> passengers = new ArrayList<>();
            Entity boss = EntityType.loadEntityRecursive(bossTag, world.getLevel(), (e) -> {
                e.setUUID(UUID.randomUUID());
                passengers.add(e);
                return e;
            });
            if (boss == null) {
                return null;
            }
            passengers.remove(boss);
            boss.setUUID(UUID.randomUUID());
            BlockPos spawnPos = findSpawnPosForBoss(world, (LivingEntity)boss, pos);
            if (spawnPos == null) {
                boss.remove(Entity.RemovalReason.DISCARDED);
                return null;
            }

            boss.setPos((double)spawnPos.getX() + (double)0.5F, spawnPos.getY(), (double)spawnPos.getZ() + (double)0.5F);
            bossType.initForEntity((Mob)boss);
            ((Mob)boss).setHealth(((Mob)boss).getMaxHealth());
            if (boss instanceof AbstractVillager abstractVillager) {
                abstractVillager.offers = new MerchantOffers();
            }

            boss.getCapability(BossCapability.BOSS_CAP).orElse(null).setSpawnPos(spawnPos);
            Compat.applyAllCompats(world, bossType, pos, boss);
            if (!boss.isRemoved()) {
                world.addFreshEntity(boss);

                for(Entity passenger : passengers) {
                    passenger.getCapability(BossCapability.BOSS_CAP).ifPresent((cap) -> cap.setSpawnPos(spawnPos));
                    passenger.setPos(boss.position());
                    if (passenger instanceof AbstractVillager abstractVillager) {
                        abstractVillager.offers = new MerchantOffers();
                    }

                    world.addFreshEntity(passenger);
                }
            }

            return boss instanceof Mob mob ? mob : null;
        } catch (Exception e) {
            BrutalBosses.LOGGER.error("Boss: " + bossType.getID() + " failed to spawn! Error:", e);
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
