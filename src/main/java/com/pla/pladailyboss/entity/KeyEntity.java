package com.pla.pladailyboss.entity;

import com.mojang.serialization.JsonOps;
import com.pla.pladailyboss.compat.BrutalBossesCompat;
import com.pla.pladailyboss.config.PlaDailyBossConfig;
import com.pla.pladailyboss.data.BossLootData;
import com.pla.pladailyboss.data.DailyBossLoader;
import com.pla.pladailyboss.enums.KeyEntityState;
import com.pla.pladailyboss.event.RewardEvent;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
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
import net.unusual.blockfactorysbosses.entity.InfernalDragonEntity;
import net.unusual.blockfactorysbosses.entity.SandwormEntity;
import net.unusual.blockfactorysbosses.entity.UnderworldKnightEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class KeyEntity extends Mob {
    private UUID summonedMobId;
    private String summonedMobRL;
    private KeyEntityState state;
    private long updatedStateTime;
    private boolean multiPhaseBoss = false;
    private final long rechargeCooldown = PlaDailyBossConfig.COOL_DOWN.get();
    private static final Random RANDOM = new Random();
    private static final Logger LOGGER = LogManager.getLogger();
    private List<String> phaseChain = Collections.emptyList();
    private int phaseIndex = -1;

    private static final String NBT_PHASE_CHAIN = "PhaseChain";
    private static final String NBT_PHASE_INDEX = "PhaseIndex";

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(KeyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> UPDATED_STATE_TIME =
            SynchedEntityData.defineId(KeyEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> RECHARGE_COOLDOWN =
            SynchedEntityData.defineId(KeyEntity.class, EntityDataSerializers.LONG);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, KeyEntityState.NORMAL.ordinal());
        this.entityData.define(UPDATED_STATE_TIME, 0L);
        this.entityData.define(RECHARGE_COOLDOWN, 0L);
    }

    public KeyEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setBoundingBox(new AABB(getX(), getY(), getZ(), getX(), getY(), getZ()));
        this.entityData.set(RECHARGE_COOLDOWN, this.rechargeCooldown);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (summonedMobId != null) pCompound.putUUID("SummonedMobUUID", summonedMobId);
        pCompound.putString("SummonedMobRL", summonedMobRL);
        pCompound.putString("KeyState", state.name());
        pCompound.putLong("UpdatedStateTime", updatedStateTime);
        pCompound.putBoolean("MultiPhaseBoss", multiPhaseBoss);
        if (phaseChain != null && !phaseChain.isEmpty()) {
            ListTag list = new ListTag();
            for (String id : phaseChain) {
                list.add(StringTag.valueOf(id));
            }
            pCompound.put(NBT_PHASE_CHAIN, list);
        }
        pCompound.putInt(NBT_PHASE_INDEX, phaseIndex);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("SummonedMobUUID")) {
            try {
                this.summonedMobId = pCompound.getUUID("SummonedMobUUID");
            } catch (Exception e) {
                this.summonedMobId = null;
            }
        } else {
            this.summonedMobId = null;
        }
        this.summonedMobRL = pCompound.contains("SummonedMobRL") ? pCompound.getString("SummonedMobRL") : "";
        if (pCompound.contains("KeyState")) {
            try {
                this.state = KeyEntityState.valueOf(pCompound.getString("KeyState"));
            } catch (IllegalArgumentException e) {
                this.state = KeyEntityState.NORMAL;
            }
        } else {
            this.state = KeyEntityState.NORMAL;
        }
        this.updatedStateTime = pCompound.contains("UpdatedStateTime") ? pCompound.getLong("UpdatedStateTime") : 0L;
        this.multiPhaseBoss = pCompound.contains("MultiPhaseBoss") && pCompound.getBoolean("MultiPhaseBoss");
        this.entityData.set(DATA_STATE, this.state.ordinal());
        this.entityData.set(UPDATED_STATE_TIME, this.updatedStateTime);

        if (pCompound.contains(NBT_PHASE_CHAIN)) {
            ListTag list = pCompound.getList(NBT_PHASE_CHAIN, /* TAG_String */ 8);
            List<String> loaded = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                loaded.add(list.getString(i));
            }
            this.phaseChain = loaded.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(loaded);
        } else {
            this.phaseChain = Collections.emptyList();
        }

        this.phaseIndex = pCompound.contains(NBT_PHASE_INDEX) ? pCompound.getInt(NBT_PHASE_INDEX) : -1;
        if (this.phaseIndex < -1) this.phaseIndex = -1;
        if (!this.phaseChain.isEmpty() && this.phaseIndex >= this.phaseChain.size()) {
            this.phaseIndex = this.phaseChain.size() - 1;
        }
        if (this.phaseChain.isEmpty()) {
            if (this.summonedMobRL != null && !this.summonedMobRL.isEmpty()) {
                this.phaseChain = Collections.singletonList(this.summonedMobRL);
                this.phaseIndex = 0;
            } else {
                this.phaseIndex = -1;
            }
        }
        if (this.multiPhaseBoss && (this.phaseChain.isEmpty() || this.phaseIndex < 0)) {
            tryRebuildPhaseChainFromData();
        }
    }

    private void tryRebuildPhaseChainFromData() {
        if (this.summonedMobRL == null || this.summonedMobRL.isEmpty()) return;

        for (Map.Entry<String, BossLootData> e : DailyBossLoader.BOSS_LOOT_TABLES.entrySet()) {
            BossLootData d = e.getValue();
            if (d != null && d.isMultiPhase() && d.phases.contains(this.summonedMobRL)) {
                this.phaseChain = List.copyOf(d.phases);
                this.phaseIndex = d.phases.indexOf(this.summonedMobRL);
                this.multiPhaseBoss = (this.phaseIndex < this.phaseChain.size() - 1);
                return;
            }
        }

        this.phaseChain = Collections.singletonList(this.summonedMobRL);
        this.phaseIndex = 0;
        this.multiPhaseBoss = false;
    }

    @Override
    public void tick() {
        super.tick();

        Player player = level().getNearestPlayer(this, 10);
        if (player != null) {
            this.lookAt(EntityAnchorArgument.Anchor.EYES, player.position());
        }
        if (!level().isClientSide) {
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

            ((ServerLevel) level()).sendParticles(ParticleTypes.ENCHANT,
                    this.getX(), this.getY() + 3.5, this.getZ(),
                    2,
                    0.2, 0.2, 0.2,
                    0.0
            );

            if (summonedMobId != null) {
                Entity entity = ((ServerLevel) level()).getEntity(summonedMobId);

                if (entity instanceof Mob mob) {
                    double distance = this.distanceToSqr(mob);
                    if (distance > 30 * 30) {
                        mob.teleportTo(this.getX(), this.getY(), this.getZ());
                    }

                    long now = System.currentTimeMillis();
                    if (now - updatedStateTime >= rechargeCooldown) {
                        mob.discard();
                        summonedMobId = null;
                        setState(KeyEntityState.NORMAL);
                    }
                } else if (multiPhaseBoss && phaseChain != null && !phaseChain.isEmpty()
                        && phaseIndex >= 0 && phaseIndex < phaseChain.size() - 1) {
                    checkMob();
                } else {
                    String lastId = this.summonedMobRL;
                    this.summonedMobRL = "";
                    this.summonedMobId = null;
                    this.phaseChain = Collections.emptyList();
                    this.phaseIndex = -1;
                    this.multiPhaseBoss = false;
                    setState(KeyEntityState.DISABLED);

                    BossLootData data = DailyBossLoader.BOSS_LOOT_TABLES.get(lastId);
                    List<String> lootTables = (data != null) ? data.lootTables : Collections.emptyList();

                    if (!lootTables.isEmpty()) {
                        for (int i = 0; i < 5; i++) {
                            String lootTableId = lootTables.get(RANDOM.nextInt(lootTables.size()));
                            RewardEvent.dropLoot(
                                    (ServerLevel) level(),
                                    new ResourceLocation(lootTableId),
                                    this.getOnPos(),
                                    1
                            );
                        }
                    } else {
                        LOGGER.warn("[Daily Boss] No loot tables configured for '{}'; skipping loot drop.", lastId);
                    }

                    int xpAmount = 1395;
                    level().addFreshEntity(new ExperienceOrb(
                            level(),
                            this.getOnPos().getX() + 0.5,
                            this.getOnPos().getY() + 1,
                            this.getOnPos().getZ() + 0.5,
                            xpAmount
                    ));
                }
            }
        }
    }

    private ResourceLocation preProcessMob(String selectedMobId) {
        if (Objects.equals(selectedMobId, "brutalbosses:randomboss")) {
            if (this.level() instanceof ServerLevel sl) {
                Entity e = BrutalBossesCompat.spawnRandomBossAndReturn(sl, this.getOnPos());
                if (e instanceof Mob m) {
                    m.setPersistenceRequired();
                    this.summonedMobId = m.getUUID();
                    this.summonedMobRL = "brutalbosses:randomboss";

                    this.phaseChain = Collections.singletonList(this.summonedMobRL);
                    this.phaseIndex = 0;
                    this.multiPhaseBoss = false;

                    this.level().playSound(null, this.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL,
                            SoundSource.BLOCKS, 1.0f, 1.0f);
                    setState(KeyEntityState.DISAPPEARED);
                    return null;
                } else {
                    LOGGER.warn("[Daily Boss] BrutalBosses random spawn failed for {}", selectedMobId);
                }
            }
        }

        BossLootData data = DailyBossLoader.BOSS_LOOT_TABLES.get(selectedMobId);
        if (data != null && data.isMultiPhase()) {
            this.phaseChain = new ArrayList<>(data.phases);
            this.phaseIndex = 0;
            this.multiPhaseBoss = true;
            return new ResourceLocation(this.phaseChain.get(0));
        } else {
            this.phaseChain = Collections.singletonList(selectedMobId);
            this.phaseIndex = 0;
            this.multiPhaseBoss = false;
            return new ResourceLocation(selectedMobId);
        }
    }

    private void processMob(String spawnedMobId, Mob mob, ServerPlayer player) {
        if (Objects.equals(spawnedMobId, "block_factorys_bosses:sandworm") && mob instanceof SandwormEntity sandworm) {
            sandworm.getEntityData().set(SandwormEntity.DATA_spawn_animtime, 180);
        }
        if (Objects.equals(spawnedMobId, "block_factorys_bosses:infernal_dragon") && mob instanceof InfernalDragonEntity dragon) {
            dragon.getEntityData().set(InfernalDragonEntity.DATA_spawn_animtime, 236);
        }
        if (Objects.equals(spawnedMobId, "block_factorys_bosses:underworld_knight") && mob instanceof UnderworldKnightEntity knight) {
            knight.getEntityData().set(UnderworldKnightEntity.DATA_spawn_animtime, 226);
        }

        if (Objects.equals(spawnedMobId, "irons_spellbooks:dead_king_corpse")) {
            mob.interact(player, InteractionHand.OFF_HAND);
        }
    }

    private void checkMob() {
        if (this.phaseChain == null || this.phaseChain.isEmpty()) return;
        if (this.phaseIndex < 0 || this.phaseIndex >= this.phaseChain.size() - 1) return;

        String nextPhaseId = this.phaseChain.get(this.phaseIndex + 1);
        double radius = 64.0;

        AABB area = new AABB(this.blockPosition()).inflate(radius);
        List<Entity> nearby = level().getEntities(this, area, e ->
                Objects.equals(ForgeRegistries.ENTITY_TYPES.getKey(e.getType()), new ResourceLocation(nextPhaseId)));

        if (!nearby.isEmpty()) {
            Entity next = nearby.get(0);
            this.summonedMobId = next.getUUID();
            this.summonedMobRL = nextPhaseId;

            this.phaseIndex++;
            this.multiPhaseBoss = (this.phaseIndex < this.phaseChain.size() - 1);
        }
    }

    private boolean spawnBoss(@NotNull Player player) {
        List<String> mobIds = DailyBossLoader.getListBasedOnKilledMob((ServerPlayer) player, player.getServer());
        if (mobIds.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("You're too weak. Come back after you've defeated at least one boss or mini-boss.")
                            .withStyle(style -> style.withColor(0xFFFF00)),
                    true
            );
            return false;
        }
        String selectedMobId;
        if (!PlaDailyBossConfig.FORCE_SPAWN.get().isEmpty()) {
            selectedMobId = PlaDailyBossConfig.FORCE_SPAWN.get();
        } else {
            selectedMobId = mobIds.get(RANDOM.nextInt(mobIds.size()));
        }

        ResourceLocation mobRL = preProcessMob(selectedMobId);
        if (mobRL == null) {
            return true;
        }

        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(mobRL);
        boolean usedCustomNBT = false;

        if (type != null && type.create(level()) instanceof Mob mob) {
            BossLootData lootData = DailyBossLoader.BOSS_LOOT_TABLES.get(selectedMobId);
            if (lootData != null && lootData.nbt != null && !lootData.nbt.entrySet().isEmpty()) {
                CompoundTag tag = CompoundTag.CODEC.parse(JsonOps.INSTANCE, lootData.nbt)
                        .resultOrPartial(msg -> LOGGER.warn("[Daily Boss] Failed to parse NBT for mob {}: {}", selectedMobId, msg))
                        .orElse(new CompoundTag());
                if (!tag.isEmpty()) {
                    tag.putString("id", mobRL.toString());
                    Entity loaded = EntityType.loadEntityRecursive(tag, level(), e -> {
                        e.moveTo(this.getX(), this.getY(), this.getZ());
                        return e;
                    });

                    if (loaded instanceof Mob loadedMob && level() instanceof ServerLevel serverLevel) {
                        loadedMob.setPersistenceRequired();
                        loadedMob.setTarget(player);
                        loadedMob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.blockPosition()),
                                MobSpawnType.COMMAND, (SpawnGroupData) null, (CompoundTag) null);
                        serverLevel.addFreshEntity(loadedMob);

                        this.summonedMobId = loadedMob.getUUID();
                        this.summonedMobRL = ForgeRegistries.ENTITY_TYPES.getKey(loadedMob.getType()).toString();
                        usedCustomNBT = true;

                        processMob(this.summonedMobRL, loadedMob, (ServerPlayer) player);
                    } else if (loaded != null) {
                        LOGGER.warn("[DailyBoss] Loaded entity from NBT is not a mob: {}", ForgeRegistries.ENTITY_TYPES.getKey(loaded.getType()));
                    } else {
                        LOGGER.warn("[DailyBoss] No entity was created from NBT for mob {}", selectedMobId);
                    }
                }
            }

            if (!usedCustomNBT && level() instanceof ServerLevel serverLevel) {
                mob.moveTo(this.getX(), this.getY(), this.getZ());
                mob.setPersistenceRequired();
                mob.setTarget(player);

                String spawnedId = ForgeRegistries.ENTITY_TYPES.getKey(type).toString();
                processMob(spawnedId, mob, (ServerPlayer) player);

                mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.blockPosition()),
                        MobSpawnType.COMMAND, (SpawnGroupData) null, (CompoundTag) null);
                serverLevel.addFreshEntity(mob);

                this.summonedMobId = mob.getUUID();
                this.summonedMobRL = spawnedId;
            }

            level().playSound(null, this.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            setState(KeyEntityState.DISAPPEARED);
            return true;
        }

        return false;
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            if (state == KeyEntityState.DISABLED) {
                long remaining = this.rechargeCooldown - (System.currentTimeMillis() - updatedStateTime);
                long seconds = (remaining / 1000) % 60;
                long minutes = (remaining / (1000 * 60)) % 60;
                long hours = remaining / (1000 * 60 * 60);
                player.displayClientMessage(
                        Component.literal("Come back after " + hours + "h " + minutes + "m " + seconds + "s")
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
        if (level().isClientSide) {
            return KeyEntityState.values()[this.entityData.get(DATA_STATE)];
        }
        return this.state;
    }

    public Long getUpdatedStateTime() {
        if (level().isClientSide) {
            return this.entityData.get(UPDATED_STATE_TIME);
        }
        return this.updatedStateTime;
    }

    public Long getRechargeCooldown() {
        if (level().isClientSide) {
            return this.entityData.get(RECHARGE_COOLDOWN);
        }
        return this.rechargeCooldown;
    }

    public void setState(KeyEntityState newState) {
        this.state = newState;
        this.entityData.set(DATA_STATE, newState.ordinal());

        if (newState == KeyEntityState.DISAPPEARED) {
            this.setInvisible(true);
            this.setSilent(true);
            this.noPhysics = true;
            this.updatedStateTime = System.currentTimeMillis();
            this.entityData.set(UPDATED_STATE_TIME, this.updatedStateTime);
            this.refreshDimensions();
        } else {
            this.setInvisible(false);
            this.setSilent(false);
            this.noPhysics = false;
            this.refreshDimensions();
        }
        if (!this.level().isClientSide) {
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    20, 0.3, 0.3, 0.3, 0.01);
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