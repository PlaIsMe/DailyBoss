package com.pla.pladailyboss.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RewardEvent {
    public static void dropLoot(ServerLevel level, ResourceLocation lootTableRL, BlockPos pos, int rolls) {
        ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableRL);
        LootTable table = level.getServer()
                .reloadableRegistries()
                .getLootTable(lootTableKey);
        LootParams.Builder ctxBuilder = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));

        for (int i = 0; i < rolls; i++) {
            List<ItemStack> items = table.getRandomItems(ctxBuilder.create(LootContextParamSets.CHEST));
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack.copy());

                    entity.setDeltaMovement(randomVelocity(), 0.4 + level.random.nextDouble() * 0.4, randomVelocity());
                    level.addFreshEntity(entity);
                }
            }
        }
    }

    private static double randomVelocity() {
        return (Math.random() - 0.5) * 0.6;
    }
}
