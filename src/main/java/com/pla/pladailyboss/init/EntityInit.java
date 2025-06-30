package com.pla.pladailyboss.init;

import com.pla.pladailyboss.PlaDailyBoss;
import com.pla.pladailyboss.entity.KeyEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, PlaDailyBoss.MOD_ID);

    public static final Supplier<EntityType<KeyEntity>> KEY_ENTITY =
            ENTITY_TYPES.register("key_entity", () ->
                    EntityType.Builder.<KeyEntity>of(KeyEntity::new, MobCategory.CREATURE)
                            .sized(2.5f, 4.0f)
                            .build("key_entity"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
