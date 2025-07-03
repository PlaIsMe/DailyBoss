package com.pla.pladailyboss.client.handler;

import com.pla.pladailyboss.PlaDailyBoss;
import com.pla.pladailyboss.client.renderer.KeyEntityRenderer;
import com.pla.pladailyboss.init.EntityInit;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = PlaDailyBoss.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModHandler {
    public static final KeyMapping OPEN_ENTITY_GUI_KEY = new KeyMapping(
            "key.pladailyboss.open_entity_gui",
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            "key.categories.misc"
    );

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientRegistry.registerKeyBinding(OPEN_ENTITY_GUI_KEY);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.KEY_ENTITY.get(), KeyEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }
}