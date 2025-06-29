package com.pla.pladailyboss.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class PlaDailyBossConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<Long> COOL_DOWN;

    static {
        COOL_DOWN = BUILDER.comment(
                        "Cooldown for daily boss, in milliseconds.",
                        "Default: 1 day",
                        "Minimum: 10 seconds",
                        "Maximum: 7 days")
                .defineInRange("coolDown", 86400000L, 10000L, 604800000L);
        SPEC = BUILDER.build();
    }
}
