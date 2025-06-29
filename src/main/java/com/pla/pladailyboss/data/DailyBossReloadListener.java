package com.pla.pladailyboss.data;

import com.pla.pladailyboss.PlaDailyBoss;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = PlaDailyBoss.MOD_ID)
public class DailyBossReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new DailyBossLoader());
    }
}

