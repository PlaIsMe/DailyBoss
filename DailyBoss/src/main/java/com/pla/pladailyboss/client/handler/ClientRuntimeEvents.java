package com.pla.pladailyboss.client.handler;

import com.pla.pladailyboss.PlaDailyBoss;
import com.pla.pladailyboss.client.screen.BossScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = PlaDailyBoss.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientRuntimeEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (ClientKeyHandler.OPEN_ENTITY_GUI_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(new BossScreen());
        }
    }
}
