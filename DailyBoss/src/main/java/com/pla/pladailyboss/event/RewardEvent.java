package com.pla.pladailyboss.event;

import com.pla.pladailyboss.data.CustomLootEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class RewardEvent {
    private static void spawnStack(ServerLevel level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) return;

        ItemEntity entity = new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 1,
                pos.getZ() + 0.5,
                stack.copy()
        );

        entity.setDeltaMovement(randomVelocity(), 0.4 + level.random.nextDouble() * 0.4, randomVelocity());
        level.addFreshEntity(entity);
    }

    public static void dropLoot(ServerLevel level, ResourceLocation lootTableRL, BlockPos pos, int rolls) {
        LootTable table = level.getServer().getLootData().getLootTable(lootTableRL);
        LootParams.Builder ctxBuilder = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));

        for (int i = 0; i < rolls; i++) {
            List<ItemStack> items = table.getRandomItems(ctxBuilder.create(LootContextParamSets.CHEST));
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    spawnStack(level, pos, stack);
                }
            }
        }
    }

    public static void dropCustomLoot(ServerLevel level, BlockPos pos, List<CustomLootEntry> customLoot, int rolls) {
        if (customLoot == null || customLoot.isEmpty() || rolls <= 0) return;

        for (int i = 0; i < rolls; i++) {
            CustomLootEntry entry = customLoot.get(level.random.nextInt(customLoot.size()));
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.itemId));
            if (item == null || item == Items.AIR) {
                continue;
            }

            spawnStack(level, pos, new ItemStack(item, entry.count));
        }
    }

    private static double randomVelocity() {
        return (Math.random() - 0.5) * 0.6;
    }
}
