package com.pla.pladailyboss.init;

import com.pla.pladailyboss.PlaDailyBoss;
import com.pla.pladailyboss.entity.KeyEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PlaDailyBoss.MOD_ID);

    public static final RegistryObject<EntityType<KeyEntity>> KEY_ENTITY =
            ENTITY_TYPES.register("key_entity", () -> EntityType.Builder.of(KeyEntity::new, MobCategory.CREATURE)
                    .sized(2.5f, 4.0f)
                    .build("key_entity"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
