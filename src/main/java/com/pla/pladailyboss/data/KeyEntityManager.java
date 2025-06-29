package com.pla.pladailyboss.data;

import com.google.common.collect.Maps;
import com.pla.pladailyboss.enums.KeyEntityState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Map;
import java.util.UUID;

public class KeyEntityManager extends SavedData {
    private final Map<UUID, KeyEntityData> dataMap = Maps.newHashMap();

    public static final String DATA_NAME = "pla_key_entity_manager";

    public static KeyEntityManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                KeyEntityManager::load,
                KeyEntityManager::new,
                DATA_NAME
        );
    }

    public void update(UUID keyEntityUUID, UUID mobUUID, KeyEntityState state, long updatedTime, boolean underGround) {
        dataMap.put(keyEntityUUID, new KeyEntityData(mobUUID, state, updatedTime, underGround));
        setDirty();
    }

    public KeyEntityData get(UUID keyEntityUUID) {
        return dataMap.get(keyEntityUUID);
    }

    public void remove(UUID keyEntityUUID) {
        dataMap.remove(keyEntityUUID);
        setDirty();
    }

    public static KeyEntityManager load(CompoundTag tag) {
        KeyEntityManager manager = new KeyEntityManager();
        ListTag list = tag.getList("KeyEntities", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag entry = (CompoundTag) t;
            UUID keyUUID = entry.getUUID("KeyUUID");
            UUID mobUUID = entry.contains("MobUUID") ? entry.getUUID("MobUUID") : null;
            KeyEntityState state = KeyEntityState.valueOf(entry.getString("State"));
            long time = entry.getLong("UpdatedTime");
            boolean underground = entry.getBoolean("Underground");
            manager.dataMap.put(keyUUID, new KeyEntityData(mobUUID, state, time, underground));
        }
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, KeyEntityData> entry : dataMap.entrySet()) {
            CompoundTag nbt = new CompoundTag();
            nbt.putUUID("KeyUUID", entry.getKey());
            if (entry.getValue().mobUUID() != null)
                nbt.putUUID("MobUUID", entry.getValue().mobUUID());
            nbt.putString("State", entry.getValue().state().name());
            nbt.putLong("UpdatedTime", entry.getValue().updatedTime());
            nbt.putBoolean("Underground", entry.getValue().underGround());
            list.add(nbt);
        }
        tag.put("KeyEntities", list);
        return tag;
    }

    public record KeyEntityData(UUID mobUUID, KeyEntityState state, long updatedTime, boolean underGround) {}
}
