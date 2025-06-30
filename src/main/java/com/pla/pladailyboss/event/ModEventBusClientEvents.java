package com.pla.pladailyboss.event;


import com.pla.pladailyboss.PlaDailyBoss;
import com.pla.pladailyboss.client.renderer.KeyEntityModel;
import com.pla.pladailyboss.client.renderer.ModModelLayers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = PlaDailyBoss.MOD_ID, value = Dist.CLIENT)
public class ModEventBusClientEvents {
    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.KEY_ENTITY_LAYER, KeyEntityModel::createBodyLayer);
    }
}