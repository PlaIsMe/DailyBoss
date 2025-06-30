package com.pla.pladailyboss.event;


import com.pla.pladailyboss.PlaDailyBoss;
import com.pla.pladailyboss.entity.KeyEntity;
import com.pla.pladailyboss.init.EntityInit;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = PlaDailyBoss.MOD_ID)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityInit.KEY_ENTITY.get(), KeyEntity.createAttributes().build());
    }
}