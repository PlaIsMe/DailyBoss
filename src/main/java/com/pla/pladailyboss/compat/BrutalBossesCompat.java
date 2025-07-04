//package com.pla.pladailyboss.compat;
//
//import com.brutalbosses.BrutalBosses;
//import com.brutalbosses.compat.Compat;
//import com.brutalbosses.entity.BossSpawnHandler;
//import com.brutalbosses.entity.BossType;
//import com.brutalbosses.entity.BossTypeManager;
//import net.minecraft.core.BlockPos;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.util.Tuple;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.Mob;
//import net.minecraft.world.level.ServerLevelAccessor;
//
//import static com.brutalbosses.entity.capability.BossCapability.BOSS_CAP;
//
//import javax.annotation.Nullable;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ConcurrentLinkedQueue;
//
//public class BrutalBossesCompat extends BossSpawnHandler {
//    private static ConcurrentLinkedQueue<Tuple<BlockPos, BossType>> spawns = new ConcurrentLinkedQueue<>();
//
//    public static @Nullable Mob spawnBossAndReturn(ServerLevelAccessor world, BlockPos pos, BossType bossType) {
//        try {
//            spawns.add(new Tuple<>(pos, bossType));
//            if (spawns.size() > 20) {
//                spawns.poll();
//            }
//
//            final Mob boss = bossType.createBossEntity(world.getLevel());
//            if (boss == null) {
//                return null;
//            }
//
//            BlockPos spawnPos = findSpawnPosForBoss(world, boss, pos);
//            if (spawnPos == null) {
//                boss.remove(Entity.RemovalReason.DISCARDED);
//                return null;
//            } else {
//                boss.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
//            }
//
//            boss.getCapability(BOSS_CAP).ifPresent(cap -> cap.setSpawnPos(pos));
//
//            Compat.applyAllCompats(world, bossType, pos, boss);
//
//            if (!boss.isRemoved()) {
//                world.addFreshEntity(boss);
//            }
//
//            return boss;
//        } catch (Exception e) {
//            BrutalBosses.LOGGER.error("Boss: " + bossType.getID() + " failed to spawn! Error:", e);
//            return null;
//        }
//    }
//
//    public static @Nullable Mob spawnRandomBossAndReturn(final ServerLevel world, final BlockPos pos)
//    {
//        final List<BossType> list = new ArrayList<>(BossTypeManager.instance.bosses.values());
//        final BossType bossType = list.get(BrutalBosses.rand.nextInt(list.size()));
//        return spawnBossAndReturn(world, pos, bossType);
//    }
//}
