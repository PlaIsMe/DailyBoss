package com.pla.pladailyboss.client.handler;

import com.pla.pladailyboss.client.screen.BossScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "pladailyboss", value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (Minecraft.getInstance().player != null && ClientModHandler.OPEN_ENTITY_GUI_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(new BossScreen());
        }
    }
}
