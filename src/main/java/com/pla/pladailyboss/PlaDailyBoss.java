package com.pla.pladailyboss;

import com.mojang.logging.LogUtils;
import com.pla.pladailyboss.config.PlaDailyBossConfig;
import com.pla.pladailyboss.data.DailyBossLoader;
import com.pla.pladailyboss.entity.KeyEntity;
import com.pla.pladailyboss.init.BlockInit;
import com.pla.pladailyboss.init.EntityInit;
import com.pla.pladailyboss.init.ItemInit;
import com.pla.pladailyboss.network.NetworkRegister;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

@Mod(PlaDailyBoss.MOD_ID)
public class PlaDailyBoss
{
    public static final String MOD_ID = "pladailyboss";
    private static final Logger LOGGER = LogUtils.getLogger();

    public PlaDailyBoss(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(NetworkRegister::register);
        modEventBus.addListener(this::registerAttributes);

        modContainer.registerConfig(ModConfig.Type.COMMON, PlaDailyBossConfig.SPEC);

        EntityInit.register(modEventBus);
        BlockInit.register(modEventBus);
        ItemInit.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new DailyBossLoader());
    }

    public void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityInit.KEY_ENTITY.get(), KeyEntity.createAttributes().build());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Client setup loaded. Logged in as: {}", Minecraft.getInstance().getUser().getName());
        }
    }

}
