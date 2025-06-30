package com.pla.pladailyboss.config;


import com.pla.pladailyboss.PlaDailyBoss;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = PlaDailyBoss.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PlaDailyBossConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.ConfigValue<Long> COOL_DOWN;

    static {
        COOL_DOWN = BUILDER.comment(
                        "Cooldown for daily boss, in milliseconds.",
                        "Default: 1 day = 86400000",
                        "Minimum: 10 seconds = 10000",
                        "Maximum: 7 days = 86400000 * 7 = 604800000")
                .defineInRange("coolDown", 86400000L, 10000L, 604800000L);
        SPEC = BUILDER.build();
    }
}
