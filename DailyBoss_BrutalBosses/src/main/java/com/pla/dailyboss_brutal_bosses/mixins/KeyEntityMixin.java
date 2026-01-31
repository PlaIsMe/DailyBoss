package com.pla.dailyboss_brutal_bosses.mixins;

import com.pla.dailyboss_brutal_bosses.BrutalBossesCompat;
import com.pla.pladailyboss.entity.KeyEntity;
import com.pla.pladailyboss.enums.KeyEntityState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Objects;

@Mixin(value = {KeyEntity.class}, remap = false)
public class KeyEntityMixin {
    private static final Logger LOGGER = LogManager.getLogger();

    @Inject(method = "preProcessMob", at = @At("HEAD"), cancellable = true)
    private void addCompat(String selectedMobId, CallbackInfoReturnable<ResourceLocation> cir) {
        KeyEntity self = (KeyEntity) (Object) this;
        if (Objects.equals(selectedMobId, "brutalbosses:randomboss")) {
            if (self.level() instanceof ServerLevel sl) {
                Entity e = BrutalBossesCompat.spawnRandomBossAndReturn(sl, self.getOnPos());
                if (e instanceof Mob m) {
                    m.setPersistenceRequired();
                    self.setSummonedMobId(m.getUUID());
                    self.setSummonedMobRL("brutalbosses:randomboss");

                    self.setPhaseChain(Collections.singletonList(self.getSummonedMobRL()));
                    self.setPhaseIndex(0);
                    self.setMultiPhaseBoss(false);

                    self.level().playSound(null, self.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL,
                            SoundSource.BLOCKS, 1.0f, 1.0f);
                    self.setState(KeyEntityState.DISAPPEARED);
                    cir.setReturnValue(null);
                    cir.cancel();
                } else {
                    LOGGER.warn("[Daily Boss] BrutalBosses random spawn failed for {}", selectedMobId);
                }
            }
        }
    }
}
