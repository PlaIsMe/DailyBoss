package com.pla.dailyboss_cataclysm.mixins;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.The_Leviathan_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Ancient_Remnant.Ancient_Remnant_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity;
import com.github.L_Ender.cataclysm.entity.Pet.Netherite_Ministrosity_Entity;
import com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity;
import com.github.L_Ender.cataclysm.init.ModParticle;
import com.pla.pladailyboss.entity.KeyEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.ItemStack;
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
        if (mob.level().isClientSide()) return;
        KeyEntity self = (KeyEntity) (Object) this;
        if (self.level().isClientSide()) return;
        final RandomSource rnd = RandomSource.create();

        if (mob instanceof Ancient_Remnant_Entity ancientRemnantEntity) {
            ancientRemnantEntity.setHomePos(self.blockPosition());
            if (self.level() instanceof ServerLevel serverLevel) {
                ResourceLocation dimLoc = serverLevel.dimension().location();
                ancientRemnantEntity.setDimensionType(dimLoc.toString());
            }
            ancientRemnantEntity.setNecklace(true);
        }
        if (mob instanceof Maledictus_Entity maledictusEntity) {
            double d0 = (double)((float)self.getX() + 0.5F);
            double d1 = (double)(self.getY() + 2);
            double d2 = (double)((float)self.getZ() + 0.5F);
            float size = 3.0F;

            for(float i = -size; i <= size; ++i) {
                for(float j = -size; j <= size; ++j) {
                    for(float k = -size; k <= size; ++k) {
                        double d3 = (double)j + (rnd.nextDouble() - rnd.nextDouble()) * (double)0.5F;
                        double d4 = (double)i + (rnd.nextDouble() - rnd.nextDouble()) * (double)0.5F;
                        double d5 = (double)k + (rnd.nextDouble() - rnd.nextDouble()) * (double)0.5F;
                        double d6 = (double) Mth.sqrt((float)(d3 * d3 + d4 * d4 + d5 * d5)) / (double)0.5F + rnd.nextGaussian() * 0.05;
                        self.level().addParticle((ParticleOptions) ModParticle.PHANTOM_WING_FLAME.get(), d0, d1, d2, d3 / d6, d4 / d6, d5 / d6);
                        if (i != -size && i != size && j != -size && j != size) {
                            k += size * 2.0F - 1.0F;
                        }
                    }
                }
            }

            ScreenShake_Entity.ScreenShake(self.level(), Vec3.atCenterOf(self.getOnPos()), 20.0F, 0.1F, 0, 40);
            maledictusEntity.setTombstonePos(maledictusEntity.getOnPos());
            maledictusEntity.setHomePos(maledictusEntity.getOnPos());
            ResourceLocation dimLoc = self.level().dimension().location();
            maledictusEntity.setDimensionType(dimLoc.toString());
        }
        if (mob instanceof The_Harbinger_Entity harbingerEntity) {
            harbingerEntity.setHomePos(self.blockPosition());
            harbingerEntity.heal(harbingerEntity.getMaxHealth());
            harbingerEntity.setIsAct(true);
            if (self.level() instanceof ServerLevel serverLevel) {
                ResourceLocation dimLoc = serverLevel.dimension().location();
                harbingerEntity.setDimensionType(dimLoc.toString());
            }
        }
        if (mob instanceof The_Leviathan_Entity theLeviathanEntity) {
            theLeviathanEntity.getPersistentData().putBoolean("DailyBoss", true);
            theLeviathanEntity.setHomePos(theLeviathanEntity.getOnPos());
            ResourceLocation dimLoc = self.level().dimension().location();
            theLeviathanEntity.setDimensionType(dimLoc.toString());
        }
        if (mob instanceof Ignis_Entity ignisEntity) {
            ScreenShake_Entity.ScreenShake(self.level(), Vec3.atCenterOf(self.getOnPos()), 20.0F, 0.05F, 0, 150);
            double d0 = (double)((float)self.getOnPos().getX() + 0.5F);
            double d1 = (double)((float)self.getOnPos().getY() + 0.5F);
            double d2 = (double)((float)self.getOnPos().getZ() + 0.5F);

            for(float i = -3.0F; i <= 3.0F; ++i) {
                for(float j = -3.0F; j <= 3.0F; ++j) {
                    for(float k = -3.0F; k <= 3.0F; ++k) {
                        double d3 = (double)j + (rnd.nextDouble() - rnd.nextDouble()) * (double)0.5F;
                        double d4 = (double)i + (rnd.nextDouble() - rnd.nextDouble()) * (double)0.5F;
                        double d5 = (double)k + (rnd.nextDouble() - rnd.nextDouble()) * (double)0.5F;
                        double d6 = (double)Mth.sqrt((float)(d3 * d3 + d4 * d4 + d5 * d5)) / (double)0.5F + rnd.nextGaussian() * 0.05;
                        self.level().addParticle(ParticleTypes.FLAME, d0, d1, d2, d3 / d6, d4 / d6, d5 / d6);
                        if (i != -3.0F && i != 3.0F && j != -3.0F && j != 3.0F) {
                            k += 3.0F * 2.0F - 1.0F;
                        }
                    }
                }
            }

            ignisEntity.setHomePos(self.getOnPos());
            ResourceLocation dimLoc = self.level().dimension().location();
            ignisEntity.setDimensionType(dimLoc.toString());
        }
        if (mob instanceof Ender_Guardian_Entity enderGuardianEntity) {
            enderGuardianEntity.setUsedMassDestruction(false);
            enderGuardianEntity.setHomePos(self.getOnPos());
            ResourceLocation dimLoc = self.level().dimension().location();
            enderGuardianEntity.setDimensionType(dimLoc.toString());
        }
    }
}
