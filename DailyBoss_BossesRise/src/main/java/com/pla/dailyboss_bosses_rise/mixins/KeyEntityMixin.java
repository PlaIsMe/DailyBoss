package com.pla.dailyboss_bosses_rise.mixins;

import com.pla.pladailyboss.entity.KeyEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.unusual.blockfactorysbosses.entity.InfernalDragonEntity;
import net.unusual.blockfactorysbosses.entity.SandwormEntity;
import net.unusual.blockfactorysbosses.entity.UnderworldKnightEntity;
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
            sandworm.getEntityData().set(SandwormEntity.DATA_spawn_animtime, 180);
        }
        if (Objects.equals(spawnedMobId, "block_factorys_bosses:infernal_dragon") && mob instanceof InfernalDragonEntity dragon) {
            dragon.getEntityData().set(InfernalDragonEntity.DATA_spawn_animtime, 236);
        }
        if (Objects.equals(spawnedMobId, "block_factorys_bosses:underworld_knight") && mob instanceof UnderworldKnightEntity knight) {
            knight.getEntityData().set(UnderworldKnightEntity.DATA_spawn_animtime, 226);
        }
    }
}
