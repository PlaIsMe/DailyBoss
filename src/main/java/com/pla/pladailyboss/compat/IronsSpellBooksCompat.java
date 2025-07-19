package com.pla.pladailyboss.compat;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.FireBossEntity;
import io.redspace.ironsspellbooks.item.CinderousSoulcallerItem;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class IronsSpellBooksCompat {
    public static Mob spawnIronBoss(LivingEntity entity, Player player) {
        Vec3 center = entity.getBoundingBox().getCenter().add(0, 0.6, 0);
        float yRot = Utils.getAngle(center.x, center.z, player.getX(), player.getZ()) * Mth.RAD_TO_DEG;
        FireBossEntity fireBoss = EntityRegistry.FIRE_BOSS.get().create(entity.level());
        fireBoss.moveTo(center);
        fireBoss.setYRot(yRot + 90);
        fireBoss.triggerSpawnAnim();
        fireBoss.finalizeSpawn((ServerLevelAccessor) entity.level(), entity.level().getCurrentDifficultyAt(player.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        entity.level().addFreshEntity(fireBoss);
        CinderousSoulcallerItem item = (CinderousSoulcallerItem) ItemRegistry.CINDEROUS_SOULCALLER.get();
        item.tollEffects((ServerLevel) entity.level(), center, true);
        return fireBoss;
    }
}
