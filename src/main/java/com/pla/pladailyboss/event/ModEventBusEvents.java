package com.pla.pladailyboss.event;


import com.pla.pladailyboss.PlaDailyBoss;
import com.pla.pladailyboss.entity.KeyEntity;
import com.pla.pladailyboss.init.EntityInit;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PlaDailyBoss.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityInit.KEY_ENTITY.get(), KeyEntity.createAttributes().build());
    }
}