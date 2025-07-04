package com.pla.pladailyboss.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import com.pla.pladailyboss.compat.BrutalBossesCompat;
import com.pla.pladailyboss.config.PlaDailyBossConfig;
import com.pla.pladailyboss.data.BossLootData;
import com.pla.pladailyboss.data.DailyBossLoader;
import com.pla.pladailyboss.data.KeyEntityManager;
import com.pla.pladailyboss.enums.KeyEntityState;
import com.pla.pladailyboss.event.RewardEvent;
import com.pla.pladailyboss.ftb.ClaimChunkHelper;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class KeyEntity extends Mob {
    private UUID summonedMobId = null;
    private String summonedMobRL = "";
    private KeyEntityState state = KeyEntityState.NORMAL;
    private long updatedStateTime = 0L;
    private final long rechargeCooldown = PlaDailyBossConfig.COOL_DOWN.get();
    private static final Random RANDOM = new Random();
    private static final Logger LOGGER = LogManager.getLogger();

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(KeyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> UPDATED_STATE_TIME =
            SynchedEntityData.defineId(KeyEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> RECHARGE_COOLDOWN =
            SynchedEntityData.defineId(KeyEntity.class, EntityDataSerializers.STRING);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, KeyEntityState.NORMAL.ordinal());
        this.entityData.define(UPDATED_STATE_TIME, "");
        this.entityData.define(RECHARGE_COOLDOWN, "");
    }

    public KeyEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setBoundingBox(new AABB(getX(), getY(), getZ(), getX(), getY(), getZ()));
        this.entityData.set(RECHARGE_COOLDOWN, String.valueOf(this.rechargeCooldown));
    }

    public void updateDataToManager() {
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            KeyEntityManager manager = KeyEntityManager.get(serverLevel);
            manager.update(this.getUUID(), this.summonedMobId, this.state, this.updatedStateTime, this.summonedMobRL);
        }
    }

    @Override
    public void tick() {
        super.tick();

        Player player = level.getNearestPlayer(this, 10);
        if (player != null) {
            this.lookAt(EntityAnchorArgument.Anchor.EYES, player.position());
        }

        if (!level.isClientSide) {
            if (state == KeyEntityState.DISAPPEARED && !this.isInvisible()) {
                this.setInvisible(true);
                this.setSilent(true);
                this.noPhysics = true;
            }

            if (state == KeyEntityState.DISABLED) {
                long now = System.currentTimeMillis();
                if (now - updatedStateTime >= this.rechargeCooldown) {
                    setState(KeyEntityState.NORMAL);
                }
                return;
            }

            ((ServerLevel) level).sendParticles(ParticleTypes.ENCHANT,
                    this.getX(), this.getY() + 3.5, this.getZ(),
                    2,
                    0.2, 0.2, 0.2,
                    0.0
            );

            if (summonedMobId != null) {
                Entity entity = ((ServerLevel) level).getEntity(summonedMobId);

                if (entity instanceof Mob mob) {
                    double distance = this.distanceToSqr(mob);
                    if (distance > 30 * 30) {
                        mob.teleportTo(this.getX(), this.getY() + 2.5, this.getZ());
                    }

                    long now = System.currentTimeMillis();
                    if (now - updatedStateTime >= rechargeCooldown) {
                        mob.discard();
                        summonedMobId = null;
                        setState(KeyEntityState.NORMAL);
                    }

                } else {
                    // The key move up first then throw reward
                    String tempSummonedMobRL = summonedMobRL;

                    summonedMobRL = "";
                    summonedMobId = null;
                    setState(KeyEntityState.DISABLED);

                    BossLootData data = DailyBossLoader.BOSS_LOOT_TABLES.get(tempSummonedMobRL);
                    List<String> lootTables = data != null ? data.lootTables : Collections.emptyList();
                    for (int i = 0; i < 5; i++) {
                        String lootTableId = lootTables.get(RANDOM.nextInt(lootTables.size()));
                        RewardEvent.dropLoot(
                            (ServerLevel) level,
                            new ResourceLocation(lootTableId),
                            this.getOnPos(),
                            1
                        );
                    }
                    int xpAmount = 1395;
                    level.addFreshEntity(new net.minecraft.world.entity.ExperienceOrb(
                        level,
                        this.getOnPos().getX() + 0.5,
                        this.getOnPos().getY() + 1,
                        this.getOnPos().getZ() + 0.5,
                        xpAmount
                    ));
                }
            }
        }
    }

    private void entityClaimChunk(@NotNull Player player) {
        ClaimChunkHelper claimChunkHelper = null;
        try {
            claimChunkHelper = ClaimChunkHelper.getInstance(player.getServer());
            claimChunkHelper.claimChunk(player.createCommandSourceStack(), (ServerPlayer) player, this.getOnPos());
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean spawnBoss(@NotNull Player player) {
        List<String> mobIds = DailyBossLoader.getListBasedOnKilledMob((ServerPlayer) player, player.getServer());
        if (mobIds.isEmpty()) {
            player.displayClientMessage(
                    new TextComponent("You're too weak. Come back after you've defeated at least one boss or mini-boss.")
                            .withStyle(style -> style.withColor(0xFFFF00)),
                    true
            );
            return false;
        }
        String selectedMobId = mobIds.get(RANDOM.nextInt(mobIds.size()));

        if (Objects.equals(selectedMobId, "brutalbosses:randomboss")) {
            summonedMobId = Objects.requireNonNull(BrutalBossesCompat.spawnRandomBossAndReturn((ServerLevel) this.level, this.getOnPos())).getUUID();
            level.playSound(null, this.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            summonedMobRL = selectedMobId;
            setState(KeyEntityState.DISAPPEARED);
            entityClaimChunk(player);
            return true;
        }

        ResourceLocation mobRL = new ResourceLocation(selectedMobId);
        EntityType<?> type = ForgeRegistries.ENTITIES.getValue(mobRL);

        boolean usedCustomNBT = false;

        if (type != null && type.create(level) instanceof Mob mob) {
            BossLootData lootData = DailyBossLoader.BOSS_LOOT_TABLES.get(selectedMobId);
            if (lootData != null && lootData.nbt != null && !lootData.nbt.entrySet().isEmpty()) {
                CompoundTag tag = CompoundTag.CODEC.parse(JsonOps.INSTANCE, lootData.nbt)
                        .resultOrPartial(msg -> LOGGER.warn("[Daily Boss] Failed to parse NBT for mob {}: {}", selectedMobId, msg))
                        .orElse(new CompoundTag());
                if (!tag.isEmpty()) {
                    tag.putString("id", selectedMobId);
                    Entity loaded = EntityType.loadEntityRecursive(tag, level, e -> {
                        e.setPos(this.getX(), this.getY(), this.getZ());
                        return e;
                    });

                    if (loaded != null) {
                        if (loaded instanceof Mob loadedMob) {
                            loadedMob.setPersistenceRequired();
                            loadedMob.setTarget(player);
                            level.addFreshEntity(loadedMob);
                            summonedMobId = loadedMob.getUUID();
                            usedCustomNBT = true;
                        } else {
                            LOGGER.warn("[DailyBoss] Loaded entity from NBT is not a mob: {}", ForgeRegistries.ENTITIES.getKey(loaded.getType()));
                        }
                    } else {
                        LOGGER.warn("[DailyBoss] No entity was created from NBT for mob {}", selectedMobId);
                    }
                }
            }
            if (!usedCustomNBT){
                mob.setPos(this.getX(), this.getY(), this.getZ());
                mob.setPersistenceRequired();
                mob.setTarget(player);
                level.addFreshEntity(mob);
                summonedMobId = mob.getUUID();
            }
            level.playSound(null, this.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            summonedMobRL = selectedMobId;
            setState(KeyEntityState.DISAPPEARED);
            entityClaimChunk(player);
            return true;
        }

        return false;
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            if (state == KeyEntityState.DISABLED) {
                long remaining = this.rechargeCooldown - (System.currentTimeMillis() - updatedStateTime);
                long seconds = (remaining / 1000) % 60;
                long minutes = (remaining / (1000 * 60)) % 60;
                long hours = remaining / (1000 * 60 * 60);
                player.displayClientMessage(
                        new TextComponent("Come back after " + hours + "h " + minutes + "m " + seconds + "s")
                                .withStyle(style -> style.withColor(0xFFFF00)),
                        true
                );
                return InteractionResult.PASS;
            }

            if (state == KeyEntityState.DISAPPEARED) {
                return InteractionResult.PASS;
            }

            if (spawnBoss(player)) {
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        }

        return super.mobInteract(player, hand);
    }

    public KeyEntityState getState() {
        if (level.isClientSide) {
            return KeyEntityState.values()[this.entityData.get(DATA_STATE)];
        }
        return this.state;
    }

    public String getUpdatedStateTime() {
        if (level.isClientSide) {
            return this.entityData.get(UPDATED_STATE_TIME);
        }
        return String.valueOf(this.updatedStateTime);
    }

    public String getRechargeCooldown() {
        if (level.isClientSide) {
            return this.entityData.get(RECHARGE_COOLDOWN);
        }
        return String.valueOf(this.rechargeCooldown);
    }

    public void setState(KeyEntityState newState) {
        this.state = newState;
        this.entityData.set(DATA_STATE, newState.ordinal());

        if (newState == KeyEntityState.DISAPPEARED) {
            this.setInvisible(true);
            this.setSilent(true);
            this.noPhysics = true;
            this.updatedStateTime = System.currentTimeMillis();
            this.entityData.set(UPDATED_STATE_TIME, String.valueOf(this.updatedStateTime));
            this.refreshDimensions();
        } else {
            this.setInvisible(false);
            this.setSilent(false);
            this.noPhysics = false;
            this.refreshDimensions();
        }
        if (!this.level.isClientSide) {
            ((ServerLevel) this.level).sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    20, 0.3, 0.3, 0.3, 0.01);
            this.updateDataToManager();
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            KeyEntityManager manager = KeyEntityManager.get(serverLevel);
            KeyEntityManager.KeyEntityData data = manager.get(this.getUUID());

            if (data != null) {
                this.summonedMobId = data.mobUUID();
                this.state = data.state();
                this.updatedStateTime = data.updatedTime();
                this.summonedMobRL = data.summonedMobRL();
                this.entityData.set(DATA_STATE, this.state.ordinal());
                this.entityData.set(UPDATED_STATE_TIME, String.valueOf(this.updatedStateTime));
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return true;
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isNoAi() {
        return true;
    }

    @Override
    protected void doPush(@NotNull Entity other) {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        if (reason == RemovalReason.KILLED || reason == RemovalReason.DISCARDED) {
            return;
        }
        super.remove(reason);
    }

    @Override
    public void die(@NotNull DamageSource cause) {
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return new AABB(getX(), getY(), getZ(), getX(), getY(), getZ());
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return this.state != KeyEntityState.DISAPPEARED;
    }
}