package com.pla.pladailyboss;

import com.mojang.logging.LogUtils;
import com.pla.pladailyboss.config.PlaDailyBossConfig;
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
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
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
//        modEventBus.addListener(DailyBossReloadListener::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, PlaDailyBossConfig.SPEC);

        NeoForge.EVENT_BUS.register(EntityInit.class);
        NeoForge.EVENT_BUS.register(BlockInit.class);
        NeoForge.EVENT_BUS.register(ItemInit.class);
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
