package com.pla.dailyboss_cataclysm.mixins;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.The_Leviathan_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Ancient_Remnant.Ancient_Remnant_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.NewNetherite_Monstrosity.Netherite_Monstrosity_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity;
import com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity;
import com.github.L_Ender.cataclysm.init.ModParticle;
import com.pla.pladailyboss.entity.KeyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {KeyEntity.class}, remap = false)
public class KeyEntityMixin {
    private static final Logger LOGGER = LogManager.getLogger();

    @Inject(method = "processMob", at = @At("TAIL"))
    private void addCompat(String spawnedMobId, Mob mob, ServerPlayer player, CallbackInfo ci) {
        KeyEntity self = (KeyEntity) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) return;
        final RandomSource rnd = RandomSource.create();

        if (mob instanceof Ancient_Remnant_Entity ancientRemnantEntity) {
            ancientRemnantEntity.setHomePos(GlobalPos.of(ancientRemnantEntity.level().dimension(), BlockPos.ZERO));
            ancientRemnantEntity.setNecklace(true);
            ancientRemnantEntity.setAttackState(2);
        }
        if (mob instanceof Maledictus_Entity maledictusEntity) {
            double d0 = (float) self.getX() + 0.5F;
            double d1 = self.getY() + 2;
            double d2 = (float) self.getZ() + 0.5F;
            float size = 3.0F;

            for (float i = -size; i <= size; ++i) {
                for (float j = -size; j <= size; ++j) {
                    for (float k = -size; k <= size; ++k) {
                        double d3 = (double) j + (rnd.nextDouble() - rnd.nextDouble()) * (double) 0.5F;
                        double d4 = (double) i + (rnd.nextDouble() - rnd.nextDouble()) * (double) 0.5F;
                        double d5 = (double) k + (rnd.nextDouble() - rnd.nextDouble()) * (double) 0.5F;
                        double d6 = (double) Mth.sqrt((float) (d3 * d3 + d4 * d4 + d5 * d5)) / (double) 0.5F + rnd.nextGaussian() * 0.05;
                        serverLevel.addParticle(ModParticle.PHANTOM_WING_FLAME.get(), d0, d1, d2, d3 / d6, d4 / d6, d5 / d6);
                        if (i != -size && i != size && j != -size && j != size) {
                            k += size * 2.0F - 1.0F;
                        }
                    }
                }
            }

            ScreenShake_Entity.ScreenShake(serverLevel, Vec3.atCenterOf(self.getOnPos()), 20.0F, 0.1F, 0, 40);
            maledictusEntity.setTombstoneDirection(Direction.EAST);
            maledictusEntity.setHomePos(GlobalPos.of(serverLevel.dimension(), BlockPos.ZERO));
        }
        if (mob instanceof The_Harbinger_Entity harbingerEntity) {
            harbingerEntity.setIsAct(true);
            harbingerEntity.setHomePos(GlobalPos.of(harbingerEntity.level().dimension(), BlockPos.ZERO));
            harbingerEntity.heal(harbingerEntity.getMaxHealth());
        }
        if (mob instanceof The_Leviathan_Entity theLeviathanEntity) {
            theLeviathanEntity.setHomePos(GlobalPos.of(serverLevel.dimension(), BlockPos.ZERO));
        }
        if (mob instanceof Ignis_Entity ignisEntity) {
            ScreenShake_Entity.ScreenShake(self.level(), Vec3.atCenterOf(self.getOnPos()), 20.0F, 0.05F, 0, 150);
            double d0 = (float)self.getOnPos().getX() + 0.5F;
            double d1 = (float)self.getOnPos().getY() + 0.5F;
            double d2 = (float)self.getOnPos().getZ() + 0.5F;

            for(float i = -3.0F; i <= 3.0F; ++i) {
                for(float j = -3.0F; j <= 3.0F; ++j) {
                    for(float k = -3.0F; k <= 3.0F; ++k) {
                        double d3 = (double)j + (rnd.nextDouble() - rnd.nextDouble()) * (double)0.5F;
                        double d4 = (double)i + (rnd.nextDouble() - rnd.nextDouble()) * (double)0.5F;
                        double d5 = (double)k + (rnd.nextDouble() - rnd.nextDouble()) * (double)0.5F;
                        double d6 = (double)Mth.sqrt((float)(d3 * d3 + d4 * d4 + d5 * d5)) / (double)0.5F + rnd.nextGaussian() * 0.05;
                        serverLevel.addParticle(ParticleTypes.FLAME, d0, d1, d2, d3 / d6, d4 / d6, d5 / d6);
                        if (i != -3.0F && i != 3.0F && j != -3.0F && j != 3.0F) {
                            k += 3.0F * 2.0F - 1.0F;
                        }
                    }
                }
            }

            ignisEntity.setHomePos(GlobalPos.of(serverLevel.dimension(), BlockPos.ZERO));
        }
        if (mob instanceof Ender_Guardian_Entity enderGuardianEntity) {
            enderGuardianEntity.setUsedMassDestruction(false);
            enderGuardianEntity.setHomePos(GlobalPos.of(serverLevel.dimension(), BlockPos.ZERO));
        }
        if (mob instanceof Netherite_Monstrosity_Entity netheriteMonstrosityEntity) {
            netheriteMonstrosityEntity.setHomePos(GlobalPos.of(netheriteMonstrosityEntity.level().dimension(), BlockPos.ZERO));
            netheriteMonstrosityEntity.setIsAwaken(true);
        }
        if (mob instanceof Scylla_Entity scyllaEntity) {
            scyllaEntity.setHomePos(GlobalPos.of(scyllaEntity.level().dimension(), BlockPos.ZERO));
            scyllaEntity.setAct(true);
            scyllaEntity.heal(scyllaEntity.getMaxHealth());
        }
    }
}