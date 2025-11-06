package com.pla.dailyboss_irons_spellbooks.mixins;

import com.pla.pladailyboss.entity.KeyEntity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.FireBossEntity;
import io.redspace.ironsspellbooks.item.CinderousSoulcallerItem;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(value = {KeyEntity.class}, remap = false)
public class KeyEntityMixin {
    private static final Logger LOGGER = LogManager.getLogger();

    public Mob spawnIronBoss(LivingEntity entity, Player player) {
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

    @Inject(method = "processMob", at = @At("TAIL"))
    private void addCompat(String spawnedMobId, Mob mob, ServerPlayer player, CallbackInfo ci) {
        if (Objects.equals(spawnedMobId, "irons_spellbooks:dead_king_corpse")) {
            mob.interact(player, InteractionHand.OFF_HAND);
        }
    }

    @Inject(method = "customizeSpawnMob", at = @At("HEAD"), cancellable = true)
    private void addCustomSpawnLogic(String spawnedMobId, Player player, CallbackInfoReturnable<Mob> cir) {
        if (Objects.equals(spawnedMobId, "irons_spellbooks:fire_boss")) {
            KeyEntity self = (KeyEntity) (Object) this;
            Mob boss = spawnIronBoss(self, player);
            cir.setReturnValue(boss);
            cir.cancel();
        }
    }
}
