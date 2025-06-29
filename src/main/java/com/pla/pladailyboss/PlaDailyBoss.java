package com.pla.pladailyboss;

import com.mojang.logging.LogUtils;
import com.pla.pladailyboss.config.PlaDailyBossConfig;
import com.pla.pladailyboss.data.DailyBossLoader;
import com.pla.pladailyboss.data.DailyBossReloadListener;
import com.pla.pladailyboss.init.BlockInit;
import com.pla.pladailyboss.init.EntityInit;
import com.pla.pladailyboss.init.ItemInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(PlaDailyBoss.MOD_ID)
public class PlaDailyBoss
{
    public static final String MOD_ID = "pladailyboss";
    private static final Logger LOGGER = LogUtils.getLogger();

    public PlaDailyBoss() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        EntityInit.ENTITY_TYPES.register(modEventBus);
        BlockInit.BLOCKS.register(modEventBus);
        ItemInit.ITEMS.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PlaDailyBossConfig.SPEC, "dailyboss-server.toml");
        MinecraftForge.EVENT_BUS.register(new DailyBossReloadListener());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}
