package com.pla.pladailyboss.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pla.pladailyboss.entity.KeyEntity;
import com.pla.pladailyboss.enums.KeyEntityState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class CommandInit {

    private static final double HALF_BOX = 15.0D; // 30 x 30 centered on caster
    private static final double Y_RANGE = 16.0D;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands
                .literal("pladailyboss")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("kill")
                        .executes(context -> killNearbyKeyEntity(
                                context.getSource(),
                                context.getSource().getPosition()
                        )))

                .then(Commands.literal("spawn")
                        .then(Commands.argument("boss_id", ResourceLocationArgument.id())
                                .executes(context -> spawnNearbyKeyEntityBoss(
                                        context.getSource(),
                                        context.getSource().getPosition(),
                                        ResourceLocationArgument.getId(context, "boss_id")
                                ))))

                .then(Commands.literal("reset")
                        .executes(context -> resetNearbyKeyEntity(
                                context.getSource(),
                                context.getSource().getPosition()
                        )))
        );
    }

    private static int killNearbyKeyEntity(CommandSourceStack source, Vec3 pos) throws CommandSyntaxException {
        ServerLevel level = source.getLevel();

        List<KeyEntity> keys = findNearbyKeys(level, pos);
        if (keys.isEmpty()) {
            source.sendFailure(Component.literal("No KeyEntity found within 30x30 blocks."));
            return 0;
        }

        int count = 0;
        for (KeyEntity key : keys) {
            key.deletePermanently();
            count++;
        }

        int finalCount = count;
        source.sendSuccess(() -> Component.literal("Killed " + finalCount + " KeyEntity instance(s)."), true);
        return count;
    }

    private static int spawnNearbyKeyEntityBoss(CommandSourceStack source, Vec3 pos, ResourceLocation bossId) throws CommandSyntaxException {
        ServerLevel level = source.getLevel();
        ServerPlayer player = source.getPlayerOrException();

        KeyEntity key = findNearestUsableKey(level, pos);
        if (key == null) {
            source.sendFailure(Component.literal("No usable KeyEntity found nearby."));
            return 0;
        }

        boolean ok = key.spawnBoss(player, bossId.toString());
        if (!ok) {
            source.sendFailure(Component.literal("Failed to spawn boss '" + bossId + "'."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Forced KeyEntity to spawn boss '" + bossId + "'."), true);
        return 1;
    }

    private static int resetNearbyKeyEntity(CommandSourceStack source, Vec3 pos) {
        ServerLevel level = source.getLevel();

        List<KeyEntity> keys = findNearbyKeys(level, pos);
        if (keys.isEmpty()) {
            source.sendFailure(Component.literal("No KeyEntity found within 30x30 blocks."));
            return 0;
        }

        int count = 0;
        for (KeyEntity key : keys) {
            key.setSummonedMobId(null);
            key.setSummonedMobRL("");
            key.setPhaseChain(List.of());
            key.setPhaseIndex(-1);
            key.setMultiPhaseBoss(false);
            key.stopAllWaterOps();
            key.setState(KeyEntityState.NORMAL);
            count++;
        }

        int finalCount = count;
        source.sendSuccess(() -> Component.literal("Reset " + finalCount + " KeyEntity instance(s) to NORMAL."), true);
        return count;
    }

    private static List<KeyEntity> findNearbyKeys(ServerLevel level, Vec3 pos) {
        AABB box = new AABB(
                pos.x - HALF_BOX, pos.y - Y_RANGE, pos.z - HALF_BOX,
                pos.x + HALF_BOX, pos.y + Y_RANGE, pos.z + HALF_BOX
        );

        return level.getEntitiesOfClass(KeyEntity.class, box, Entity::isAlive);
    }

    private static KeyEntity findNearestUsableKey(ServerLevel level, Vec3 pos) {
        return findNearbyKeys(level, pos).stream()
                .filter(key -> key.getState() != KeyEntityState.DISAPPEARED)
                .min(Comparator.comparingDouble(key -> key.distanceToSqr(pos)))
                .orElse(null);
    }
}