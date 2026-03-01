package com.pla.dailyboss_bosses_rise.mixins;

import com.pla.pladailyboss.entity.KeyEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.unusual.block_factorys_bosses.entity.boss.dragon.boss.InfernalDragonEntity;
import net.unusual.block_factorys_bosses.entity.boss.knight.UnderworldKnightEntity;
import net.unusual.block_factorys_bosses.entity.boss.sandworm.SandwormEntity;
import net.unusual.block_factorys_bosses.entity.boss.yeti.YetiEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = {KeyEntity.class}, remap = false)
public class KeyEntityMixin {
    private static final Logger LOGGER = LogManager.getLogger();

    @Inject(method = "processMob", at = @At("TAIL"))
    private void addCompat(String spawnedMobId, Mob mob, ServerPlayer player, CallbackInfo ci) {
        if (Objects.equals(spawnedMobId, "block_factorys_bosses:sandworm") && mob instanceof SandwormEntity sandworm) {
            sandworm.setHiddenUnderground(false);
            sandworm.triggerAnim("intro");
        }
        if (Objects.equals(spawnedMobId, "block_factorys_bosses:yeti") && mob instanceof YetiEntity yeti) {
            yeti.setState(YetiEntity.YetiState.INTRO.toString());
            yeti.getEntityData().set(YetiEntity.DATA_IS_ENRAGED, 1);
        }
        if (Objects.equals(spawnedMobId, "block_factorys_bosses:underworld_knight") && mob instanceof UnderworldKnightEntity knight) {
            knight.getEntityData().set(UnderworldKnightEntity.DATA_BOSS_PHASE, -1);
            knight.setState("idle");
        }
    }
}
