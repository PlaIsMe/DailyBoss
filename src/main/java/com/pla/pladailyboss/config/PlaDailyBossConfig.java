package com.pla.pladailyboss.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class PlaDailyBossConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<Integer> TICK_RESET;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BOSSES;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> GATES;

    static {
        TICK_RESET = BUILDER.comment("Tick resetting the summon block")
                .defineInRange("tickReset", 100, 60, 72000);

        BOSSES = BUILDER.comment("A list of bosses to be spawned")
                .defineList("bosses", List.of("minecraft:zombie", "minecraft:skeleton", "minecraft:creeper"),
                        obj -> obj instanceof String);
        SPEC = BUILDER.build();
    }
}
