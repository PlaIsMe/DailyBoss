package com.pla.pladailyboss.event;

import com.pla.pladailyboss.PlaDailyBoss;
import com.pla.pladailyboss.entity.KeyEntity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PlaDailyBoss.MOD_ID)
public class BossMobLootEvent {
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().getPersistentData().getBoolean(KeyEntity.TAG_DISABLE_MOB_LOOT)) {
            event.getDrops().clear();
        }
    }
}