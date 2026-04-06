package com.pla.pladailyboss.entity;

import com.mojang.serialization.JsonOps;
import com.pla.pladailyboss.config.PlaDailyBossConfig;
import com.pla.pladailyboss.data.BossLootData;
import com.pla.pladailyboss.data.DailyBossLoader;
import com.pla.pladailyboss.enums.KeyEntityState;
import com.pla.pladailyboss.event.RewardEvent;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class KeyEntity extends Mob {
    private UUID summonedMobId;
    private String summonedMobRL = "";
    private KeyEntityState state = KeyEntityState.NORMAL;
    private long updatedStateTime = 0L;
    private boolean spawningRecoveryClone = false;
    private boolean multiPhaseBoss = false;
    private final long rechargeCooldown = PlaDailyBossConfig.COOL_DOWN.get();
    private static final Random RANDOM = new Random();
    private static final Logger LOGGER = LogManager.getLogger();
    private List<String> phaseChain = Collections.emptyList();
    private int phaseIndex = -1;
    private String activeBossDataId = "";
    private long activeEncounterTimeoutMs = -1L;
    private boolean bypassRecoveryOnRemove = false;

    private static final String NBT_ACTIVE_BOSS_DATA_ID = "ActiveBossDataId";
    private static final String NBT_ACTIVE_ENCOUNTER_TIMEOUT_MS = "ActiveEncounterTimeoutMs";

    public static final String TAG_DISABLE_MOB_LOOT = "PlaDailyBossDisableMobLoot";

    private boolean wfActive = false;
    private int wfMinX, wfMaxX, wfMinZ, wfMaxZ;
    private int wfStartY, wfMaxY;
    private int wfNextY;
    private long wfNextRunTick = 0L;

    private boolean wdActive = false;
    private int wdMinX, wdMaxX, wdMinZ, wdMaxZ;
    private static final int CLEAR_MARGIN = 10;
    private int wdNextY;
    private long wdNextRunTick = 0L;

    private static final int WF_PERIOD_TICKS = 10;

    private static final String NBT_PHASE_CHAIN = "PhaseChain";
    private static final String NBT_PHASE_INDEX = "PhaseIndex";

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(KeyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> UPDATED_STATE_TIME =
            SynchedEntityData.defineId(KeyEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> RECHARGE_COOLDOWN =
            SynchedEntityData.defineId(KeyEntity.class, EntityDataSerializers.LONG);

    public void setSummonedMobId(UUID summonedMobId) {
        this.summonedMobId = summonedMobId;
    }

    public void setSummonedMobRL(String summonedMobRL) {
        this.summonedMobRL = summonedMobRL;
    }

    public String getSummonedMobRL() {
        return summonedMobRL;
    }

    public void setPhaseChain(List<String> phaseChain) {
        this.phaseChain = phaseChain;
    }

    public void setPhaseIndex(int phaseIndex) {
        this.phaseIndex = phaseIndex;
    }

    public void setMultiPhaseBoss(boolean multiPhaseBoss) {
        this.multiPhaseBoss = multiPhaseBoss;
    }

    private final ServerBossEvent cooldownBossBar = new ServerBossEvent(
            Component.literal("Key Recharge"),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.PROGRESS
    );

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, KeyEntityState.NORMAL.ordinal());
        builder.define(UPDATED_STATE_TIME, 0L);
        builder.define(RECHARGE_COOLDOWN, 0L);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);

        if (DATA_STATE.equals(accessor)) {
            this.state = KeyEntityState.values()[this.entityData.get(DATA_STATE)];
            this.refreshDimensions();
        }
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public KeyEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.entityData.set(RECHARGE_COOLDOWN, this.rechargeCooldown);

        this.cooldownBossBar.setVisible(false);
        this.cooldownBossBar.setDarkenScreen(false);
        this.cooldownBossBar.setPlayBossMusic(false);
        this.cooldownBossBar.setCreateWorldFog(false);

        this.refreshDimensions();
    }

    private boolean hasCustomEncounterTimeout() {
        return this.activeEncounterTimeoutMs > 0L;
    }

    private long getCurrentDisappearDurationMs() {
        return this.hasCustomEncounterTimeout() ? this.activeEncounterTimeoutMs : this.rechargeCooldown;
    }

    private void startDisabledCooldownNow() {
        this.updatedStateTime = System.currentTimeMillis();
        this.entityData.set(UPDATED_STATE_TIME, this.updatedStateTime);
        setState(KeyEntityState.DISABLED);
    }

    private void clearActiveEncounter() {
        this.summonedMobId = null;
        this.summonedMobRL = "";
        this.phaseChain = Collections.emptyList();
        this.phaseIndex = -1;
        this.multiPhaseBoss = false;
        this.activeBossDataId = "";
        this.activeEncounterTimeoutMs = -1L;
    }

    private void applyBossFlagsToMob(Mob mob) {
        BossLootData data = DailyBossLoader.BOSS_LOOT_TABLES.get(this.activeBossDataId);
        if (data != null && data.disableMobLoot) {
            mob.getPersistentData().putBoolean(TAG_DISABLE_MOB_LOOT, true);
        }
    }

    private void updateCooldownBossBar() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        KeyEntityState state = this.getState();
        boolean show = state == KeyEntityState.DISAPPEARED;

        if (!show) {
            this.cooldownBossBar.setVisible(false);
            this.cooldownBossBar.removeAllPlayers();
            return;
        }

        long cooldown = Math.max(1L, this.getCurrentDisappearDurationMs());
        long remaining = Math.max(0L, cooldown - (System.currentTimeMillis() - this.getUpdatedStateTime()));
        float progress = Mth.clamp((float) remaining / (float) cooldown, 0.0F, 1.0F);

        long totalSeconds = remaining / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        this.cooldownBossBar.setName(Component.literal(
                String.format("Time remaining %02d:%02d:%02d", hours, minutes, seconds)
        ));
        this.cooldownBossBar.setProgress(progress);
        this.cooldownBossBar.setVisible(true);

        for (ServerPlayer sp : serverLevel.players()) {
            boolean near = sp.distanceToSqr(this) <= 48.0D * 48.0D;
            boolean already = this.cooldownBossBar.getPlayers().contains(sp);

            if (near && !already) {
                this.cooldownBossBar.addPlayer(sp);
            } else if (!near && already) {
                this.cooldownBossBar.removePlayer(sp);
            }
        }
    }

    public void startWaterFillBox() {
        if (!(level() instanceof ServerLevel sl)) return;

        double halfXZ = 30.0;
        int baseY = Mth.floor(this.getY()); // per your spec
        this.wfMinX = Mth.floor(this.getX() - halfXZ);
        this.wfMaxX = Mth.floor(this.getX() + halfXZ);
        this.wfMinZ = Mth.floor(this.getZ() - halfXZ);
        this.wfMaxZ = Mth.floor(this.getZ() + halfXZ);

        this.wfStartY = baseY;
        this.wfMaxY   = baseY + 64 - 1;
        this.wfNextY  = this.wfStartY;

        this.wfNextRunTick = sl.getGameTime();
        this.wfActive = true;
        this.wdActive = false;
    }

    public void startWaterClearBox() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        if (wfMaxY <= wfStartY) return;

        this.wdMinX = this.wfMinX - CLEAR_MARGIN;
        this.wdMaxX = this.wfMaxX + CLEAR_MARGIN;
        this.wdMinZ = this.wfMinZ - CLEAR_MARGIN;
        this.wdMaxZ = this.wfMaxZ + CLEAR_MARGIN;

        this.wdNextY = Math.max(wfStartY, Math.min(wfMaxY, wfNextY - 1));
        this.wdNextRunTick = serverLevel.getGameTime();
        this.wdActive = true;
        this.wfActive = false;
    }

    public void stopAllWaterOps() {
        this.wfActive = false;
        this.wdActive = false;
    }

    private void stepWaterFillLayer(ServerLevel level) {
        final int flags = 2;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int z = wfMinZ; z <= wfMaxZ; z++) {
            for (int x = wfMinX; x <= wfMaxX; x++) {
                pos.set(x, wfNextY, z);
                if (!level.hasChunkAt(pos)) continue;

                BlockState state = level.getBlockState(pos);

                if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    if (!state.getValue(BlockStateProperties.WATERLOGGED)) {
                        level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, true), flags);
                    }
                } else if (state.isAir()) {
                    level.setBlock(pos, Blocks.WATER.defaultBlockState(), flags);
                }
            }
        }

        wfNextY++;
        if (wfNextY > wfMaxY) wfActive = false;
    }

    private void stepWaterClearLayer(ServerLevel level) {
        final int flags = 2;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int z = wdMinZ; z <= wdMaxZ; z++) {
            for (int x = wdMinX; x <= wdMaxX; x++) {
                pos.set(x, wdNextY, z);
                if (!level.hasChunkAt(pos)) continue;

                BlockState state = level.getBlockState(pos);

                if (state.hasProperty(BlockStateProperties.WATERLOGGED)
                        && state.getValue(BlockStateProperties.WATERLOGGED)) {
                    level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), flags);
                } else if (state.getBlock() == Blocks.WATER) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), flags);
                }
            }
        }

        wdNextY--;
        if (wdNextY < wfStartY - 1) wdActive = false;
    }

    @Override
    public boolean skipAttackInteraction(@NotNull Entity attacker) {
        return true;
    }

    @Override
    public boolean isPickable() {
        return this.getState() != KeyEntityState.DISAPPEARED;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void actuallyHurt(@NotNull DamageSource source, float amount) {
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return true;
    }


    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (summonedMobId != null) pCompound.putUUID("SummonedMobUUID", summonedMobId);
        pCompound.putString("SummonedMobRL", summonedMobRL);
        pCompound.putString("KeyState", this.getState().name());
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

        pCompound.putBoolean("wfActive", wfActive);
        pCompound.putInt("wfMinX", wfMinX); pCompound.putInt("wfMaxX", wfMaxX);
        pCompound.putInt("wfMinZ", wfMinZ); pCompound.putInt("wfMaxZ", wfMaxZ);
        pCompound.putInt("wfStartY", wfStartY); pCompound.putInt("wfMaxY", wfMaxY);
        pCompound.putInt("wfNextY", wfNextY);
        pCompound.putLong("wfNextRunTick", wfNextRunTick);

        pCompound.putBoolean("wdActive", wdActive);
        pCompound.putInt("wdNextY", wdNextY);
        pCompound.putLong("wdNextRunTick", wdNextRunTick);

        pCompound.putInt("wdMinX", wdMinX);
        pCompound.putInt("wdMaxX", wdMaxX);
        pCompound.putInt("wdMinZ", wdMinZ);
        pCompound.putInt("wdMaxZ", wdMaxZ);

        pCompound.putString(NBT_ACTIVE_BOSS_DATA_ID, this.activeBossDataId);
        pCompound.putLong(NBT_ACTIVE_ENCOUNTER_TIMEOUT_MS, this.activeEncounterTimeoutMs);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
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
            ListTag list = pCompound.getList(NBT_PHASE_CHAIN, 8);
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

        wfActive = pCompound.getBoolean("wfActive");
        wfMinX = pCompound.getInt("wfMinX"); wfMaxX = pCompound.getInt("wfMaxX");
        wfMinZ = pCompound.getInt("wfMinZ"); wfMaxZ = pCompound.getInt("wfMaxZ");
        wfStartY = pCompound.getInt("wfStartY"); wfMaxY = pCompound.getInt("wfMaxY");
        wfNextY = pCompound.getInt("wfNextY");
        wfNextRunTick = pCompound.getLong("wfNextRunTick");

        wdActive = pCompound.getBoolean("wdActive");
        wdNextY = pCompound.getInt("wdNextY");
        wdNextRunTick = pCompound.getLong("wdNextRunTick");

        if (pCompound.contains("wdMinX")) {
            wdMinX = pCompound.getInt("wdMinX");
            wdMaxX = pCompound.getInt("wdMaxX");
            wdMinZ = pCompound.getInt("wdMinZ");
            wdMaxZ = pCompound.getInt("wdMaxZ");
        } else {
            wdMinX = wfMinX - CLEAR_MARGIN;
            wdMaxX = wfMaxX + CLEAR_MARGIN;
            wdMinZ = wfMinZ - CLEAR_MARGIN;
            wdMaxZ = wfMaxZ + CLEAR_MARGIN;
        }

        this.activeBossDataId = pCompound.contains(NBT_ACTIVE_BOSS_DATA_ID)
                ? pCompound.getString(NBT_ACTIVE_BOSS_DATA_ID) : "";
        this.activeEncounterTimeoutMs = pCompound.contains(NBT_ACTIVE_ENCOUNTER_TIMEOUT_MS)
                ? pCompound.getLong(NBT_ACTIVE_ENCOUNTER_TIMEOUT_MS) : -1L;
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

        if (!this.level().isClientSide) {
            updateCooldownBossBar();
        }

        KeyEntityState state = this.getState();
        Player player = level().getNearestPlayer(this, 10);
        if (player != null) {
            this.lookAt(EntityAnchorArgument.Anchor.EYES, player.position());
        }

        if (!level().isClientSide) {
            if (level() instanceof ServerLevel serverLevel) {
                long now = serverLevel.getGameTime();

                if (wfActive && now >= wfNextRunTick) {
                    stepWaterFillLayer(serverLevel);
                    wfNextRunTick = now + WF_PERIOD_TICKS;
                }

                if (wdActive && now >= wdNextRunTick) {
                    stepWaterClearLayer(serverLevel);
                    wdNextRunTick = now + WF_PERIOD_TICKS;
                }
            }

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
                    double halfXZ = 30.0;
                    double upY    = 62.0;
                    double y0     = this.getBoundingBox().minY;

                    AABB leash = new AABB(
                            this.getX() - halfXZ,
                            y0 - 1.0,
                            this.getZ() - halfXZ,
                            this.getX() + halfXZ,
                            y0 + upY,
                            this.getZ() + halfXZ
                    );

                    if (!leash.intersects(mob.getBoundingBox())) {
                        mob.teleportTo(this.getX(), this.getY(), this.getZ());
                    }

                    long now = System.currentTimeMillis();
                    long activeDuration = this.getCurrentDisappearDurationMs();

                    if (now - updatedStateTime >= activeDuration) {
                        boolean hadCustomEncounterTimeout = this.hasCustomEncounterTimeout();
                        String bossDataId = this.activeBossDataId.isEmpty() ? this.summonedMobRL : this.activeBossDataId;

                        mob.discard();
                        postProcessMob(bossDataId);
                        clearActiveEncounter();

                        if (hadCustomEncounterTimeout) {
                            startDisabledCooldownNow();
                        } else {
                            setState(KeyEntityState.NORMAL);
                        }
                    }
                } else if (multiPhaseBoss && phaseChain != null && !phaseChain.isEmpty()
                        && phaseIndex >= 0 && phaseIndex < phaseChain.size() - 1) {
                    checkMob();
                } else {
                    String bossDataId = this.activeBossDataId.isEmpty() ? this.summonedMobRL : this.activeBossDataId;
                    boolean hadCustomEncounterTimeout = this.hasCustomEncounterTimeout();

                    postProcessMob(bossDataId);
                    clearActiveEncounter();

                    if (hadCustomEncounterTimeout) {
                        startDisabledCooldownNow();
                    } else {
                        setState(KeyEntityState.DISABLED);
                    }

                    BossLootData data = DailyBossLoader.BOSS_LOOT_TABLES.get(bossDataId);
                    List<String> lootTables = (data != null) ? data.lootTables : Collections.emptyList();

                    if (data != null && !lootTables.isEmpty()) {
                        for (int i = 0; i < data.lootTableRolls; i++) {
                            String lootTableId = lootTables.get(RANDOM.nextInt(lootTables.size()));
                            String[] parts = lootTableId.split(":", 2);
                            RewardEvent.dropLoot(
                                    (ServerLevel) level(),
                                    ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]),
                                    this.getOnPos(),
                                    1
                            );
                        }
                    } else {
                        LOGGER.warn("[Daily Boss] No loot tables configured for '{}'; skipping loot drop.", bossDataId);
                    }

                    if (data != null && !data.customLoot.isEmpty()) {
                        RewardEvent.dropCustomLoot(
                                (ServerLevel) level(),
                                this.getOnPos(),
                                data.customLoot,
                                data.customLootRolls
                        );
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

    private void postProcessMob(String spawnedMobId) {
        BossLootData data = DailyBossLoader.BOSS_LOOT_TABLES.get(spawnedMobId);
        if (data != null && data.isWater) {
            LOGGER.info("[Daily Boss DEBUG]: clearing water");
            startWaterClearBox();
        }
    }

    private ResourceLocation preProcessMob(String selectedMobId) {
        BossLootData data = DailyBossLoader.BOSS_LOOT_TABLES.get(selectedMobId);

        if (data != null && data.isWater) {
            startWaterFillBox();
        }

        if (data != null && data.isMultiPhase()) {
            this.phaseChain = new ArrayList<>(data.phases);
            this.phaseIndex = 0;
            this.multiPhaseBoss = true;
            String[] parts = this.phaseChain.get(0).split(":", 2);
            return ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
        } else {
            this.phaseChain = Collections.singletonList(selectedMobId);
            this.phaseIndex = 0;
            this.multiPhaseBoss = false;
            String[] parts = selectedMobId.split(":", 2);
            return ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
        }
    }

    private void processMob(String spawnedMobId, Mob mob, ServerPlayer player) {
    }

    private Mob customizeSpawnMob(String spawnedMobId, Player player) {
        return null;
    }

    private void checkMob() {
        if (this.phaseChain == null || this.phaseChain.isEmpty()) return;
        if (this.phaseIndex < 0 || this.phaseIndex >= this.phaseChain.size() - 1) return;

        String nextPhaseId = this.phaseChain.get(this.phaseIndex + 1);
        String[] parts = nextPhaseId.split(":", 2);
        double halfXZ = 32.0;
        double upY = 64.0;
        double y0 = this.getBoundingBox().minY;

        AABB area = new AABB(
                this.getX() - halfXZ,
                y0 - 1,
                this.getZ() - halfXZ,
                this.getX() + halfXZ,
                y0 + upY,
                this.getZ() + halfXZ
        );

        List<Entity> nearby = level().getEntities(this, area, e ->
                Objects.equals(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()), ResourceLocation.fromNamespaceAndPath(parts[0], parts[1])));

        if (!nearby.isEmpty()) {
            Entity next = nearby.get(0);
            if (next instanceof Mob nextMob) {
                applyBossFlagsToMob(nextMob);
            }
            this.summonedMobId = next.getUUID();
            this.summonedMobRL = nextPhaseId;

            this.phaseIndex++;
            this.multiPhaseBoss = (this.phaseIndex < this.phaseChain.size() - 1);
        }
    }

    public boolean spawnBoss(@NotNull Player player, String forceSpawnMob) {
        String selectedMobId;
        if (forceSpawnMob != null && !forceSpawnMob.isBlank()) {
            selectedMobId = forceSpawnMob;
        } else {
            List<String> mobIds = DailyBossLoader.getListBasedOnKilledMob((ServerPlayer) player, player.getServer());
            if (mobIds.isEmpty()) {
                player.displayClientMessage(
                        Component.literal("You're too weak. Come back after you've defeated at least one boss or mini-boss.")
                                .withStyle(style -> style.withColor(0xFFFF00)),
                        true
                );
                return false;
            }

            if (!PlaDailyBossConfig.FORCE_SPAWN.get().isEmpty()) {
                selectedMobId = PlaDailyBossConfig.FORCE_SPAWN.get();
            } else {
                selectedMobId = mobIds.get(RANDOM.nextInt(mobIds.size()));
            }
        }

        BossLootData selectedData = DailyBossLoader.BOSS_LOOT_TABLES.get(selectedMobId);
        this.activeBossDataId = selectedMobId;
        this.activeEncounterTimeoutMs = selectedData != null ? selectedData.encounterTimeoutMs : -1L;

        ResourceLocation mobRL = preProcessMob(selectedMobId);
        if (mobRL == null) {
            return true;
        }

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(mobRL);
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
                                MobSpawnType.COMMAND, null);
                        serverLevel.addFreshEntity(loadedMob);

                        this.summonedMobId = loadedMob.getUUID();
                        this.summonedMobRL = BuiltInRegistries.ENTITY_TYPE.getKey(loadedMob.getType()).toString();
                        usedCustomNBT = true;

                        processMob(this.summonedMobRL, loadedMob, (ServerPlayer) player);
                    } else if (loaded != null) {
                        LOGGER.warn("[DailyBoss] Loaded entity from NBT is not a mob: {}", BuiltInRegistries.ENTITY_TYPE.getKey(loaded.getType()));
                    } else {
                        LOGGER.warn("[DailyBoss] No entity was created from NBT for mob {}", selectedMobId);
                    }
                }
            }

            if (!usedCustomNBT && level() instanceof ServerLevel serverLevel) {
                Mob spawned = customizeSpawnMob(selectedMobId, player);;
                if (spawned == null) {
                    mob.moveTo(this.getX(), this.getY(), this.getZ());
                    mob.setPersistenceRequired();
                    mob.setTarget(player);

                    String spawnedId = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();

                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.blockPosition()),
                            MobSpawnType.COMMAND, null);
                    serverLevel.addFreshEntity(mob);
                    spawned = mob;
                    applyBossFlagsToMob(spawned);

                    processMob(spawnedId, mob, (ServerPlayer) player);
                }

                this.summonedMobId = spawned.getUUID();
                this.summonedMobRL = selectedMobId;
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
            KeyEntityState state = this.getState();

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
                return InteractionResult.SUCCESS;
            }

            if (state == KeyEntityState.DISAPPEARED) {
                return InteractionResult.PASS;
            }

            return spawnBoss(player, null) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        return super.mobInteract(player, hand);
    }

    public KeyEntityState getState() {
        return KeyEntityState.values()[this.entityData.get(DATA_STATE)];
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
        } else {
            this.setInvisible(false);
            this.setSilent(false);
            this.noPhysics = false;
        }
        this.refreshDimensions();
        if (!this.level().isClientSide) {
            ((ServerLevel) this.level()).sendParticles(
                    ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    20, 0.3, 0.3, 0.3, 0.01
            );
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
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

    public void deletePermanently() {
        this.bypassRecoveryOnRemove = true;
        this.cooldownBossBar.removeAllPlayers();
        this.discard();
    }

    @Override
    public void remove(@NotNull Entity.RemovalReason reason) {
        if (!this.level().isClientSide && !this.spawningRecoveryClone && !this.bypassRecoveryOnRemove) {
            if (reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED) {
                this.cooldownBossBar.removeAllPlayers();
                this.spawnDisabledReplacement();
            }
        }

        super.remove(reason);
    }


    private void spawnDisabledReplacement() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (this.spawningRecoveryClone) return;

        this.spawningRecoveryClone = true;
        try {
            Entity created = this.getType().create(serverLevel);
            if (!(created instanceof KeyEntity replacement)) {
                LOGGER.error("[Daily Boss] Failed to recreate KeyEntity after forced removal.");
                return;
            }

            replacement.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());

            if (this.hasCustomName()) {
                replacement.setCustomName(this.getCustomName());
            }
            replacement.setCustomNameVisible(this.isCustomNameVisible());
            replacement.summonedMobId = null;
            replacement.summonedMobRL = "";
            replacement.phaseChain = Collections.emptyList();
            replacement.phaseIndex = -1;
            replacement.multiPhaseBoss = false;
            replacement.stopAllWaterOps();
            replacement.startDisabledCooldownNow();

            serverLevel.addFreshEntity(replacement);
        } finally {
            this.spawningRecoveryClone = false;
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
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
}