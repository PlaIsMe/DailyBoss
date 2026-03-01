package com.pla.dailyboss_bosses_rise.mixins;

import com.pla.pladailyboss.entity.KeyEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.unusual.block_factorys_bosses.entity.boss.dragon.boss.InfernalDragonEntity;
import net.unusual.block_factorys_bosses.entity.boss.knight.UnderworldKnightEntity;
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
        try {
            if (Objects.equals(spawnedMobId, "block_factorys_bosses:infernal_dragon") && mob instanceof InfernalDragonEntity dragon) {
                dragon.getEntityData().set(InfernalDragonEntity.DATA_SPAWN_ANIMTIME, 236);
            }
            if (Objects.equals(spawnedMobId, "block_factorys_bosses:yeti") && mob instanceof YetiEntity yeti) {
                yeti.getEntityData().set(YetiEntity.DATA_SPAWN_ANIMTIME, 200);
            }
            if (Objects.equals(spawnedMobId, "block_factorys_bosses:underworld_knight") && mob instanceof UnderworldKnightEntity knight) {
                knight.getEntityData().set(UnderworldKnightEntity.DATA_SPAWN_ANIMTIME, 226);
                knight.getEntityData().set(UnderworldKnightEntity.DATA_CINEMATIC, true);
                knight.getEntityData().set(UnderworldKnightEntity.DATA_INTRO_ATTACK, UnderworldKnightEntity.TICKS_BEFORE_INTRO_ATTACK);
                knight.setState("intro");
            }
        } catch (Exception e) {
            LOGGER.warn("[DailyBoss] Failed to set spawn animation data for {}: {}", spawnedMobId, e.getMessage());
        }
    }
}
