package com.pla.pladailyboss.data;

import com.pla.pladailyboss.PlaDailyBoss;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EventBusSubscriber(modid = PlaDailyBoss.MOD_ID)
public class DailyBossReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new DailyBossLoader());
    }
}

