package com.pla.pladailyboss.client.handler;

import com.pla.pladailyboss.PlaDailyBoss;
import com.pla.pladailyboss.client.renderer.KeyEntityRenderer;
import com.pla.pladailyboss.init.EntityInit;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = PlaDailyBoss.MOD_ID, value = Dist.CLIENT)
public class ClientModHandler {
    public static final KeyMapping OPEN_ENTITY_GUI_KEY = new KeyMapping(
            "key.pladailyboss.open_entity_gui",
            GLFW.GLFW_KEY_O,
            "key.categories.misc"
    );

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
        });
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_ENTITY_GUI_KEY);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.KEY_ENTITY.get(), KeyEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }
}