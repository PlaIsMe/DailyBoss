package com.pla.pladailyboss.client.handler;

import com.pla.pladailyboss.PlaDailyBoss;
import com.pla.pladailyboss.client.screen.BossScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = PlaDailyBoss.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        if (Minecraft.getInstance().player != null && ClientModHandler.OPEN_ENTITY_GUI_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(new BossScreen());
        }
    }
}
