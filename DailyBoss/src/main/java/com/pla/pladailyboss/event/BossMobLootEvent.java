package com.pla.pladailyboss.event;

import com.pla.pladailyboss.PlaDailyBoss;
import com.pla.pladailyboss.entity.KeyEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = PlaDailyBoss.MOD_ID)
public class BossMobLootEvent {
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().getPersistentData().getBoolean(KeyEntity.TAG_DISABLE_MOB_LOOT)) {
            event.getDrops().clear();
        }
    }
}