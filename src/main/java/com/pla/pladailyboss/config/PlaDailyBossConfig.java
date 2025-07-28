package com.pla.pladailyboss.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class PlaDailyBossConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<Long> COOL_DOWN;
    public static ForgeConfigSpec.ConfigValue<String> FORCE_SPAWN;
    public static ForgeConfigSpec.ConfigValue<Boolean> FORCE_UNLOCK;

    static {
        COOL_DOWN = BUILDER.comment(
                        "Cooldown for daily boss, in milliseconds.",
                        "Default: 1 day = 86400000",
                        "Minimum: 10 seconds = 10000",
                        "Maximum: 7 days = 86400000 * 7 = 604800000")
                .defineInRange("coolDown", 86400000L, 10000L, 604800000L);
        FORCE_SPAWN = BUILDER.comment(
                        "Force spawning any boss without killing it first.",
                        "This mode is only for mod reviewers that need to review some bosses.",
                        "Remember adding the boss id to datapack or else it will crash when you killed the boss.",
                        "Example: minecraft:wither")
                .define("forceSpawn", "");
        FORCE_UNLOCK = BUILDER.comment(
                        "Force unlock all available bosses.",
                        "This will break your gameplay.")
                .define("forceUnlock", false);
        SPEC = BUILDER.build();
    }
}
