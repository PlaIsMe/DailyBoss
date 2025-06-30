package com.pla.pladailyboss.init;

import com.pla.pladailyboss.PlaDailyBoss;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, PlaDailyBoss.MOD_ID);

    public static final Supplier<BlockItem> SPINNING_BLOCK_ITEM = ITEMS.register("spinning_block",
            () -> new BlockItem(BlockInit.SPINNING_BLOCK.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Supplier<BlockItem> UNBREAKABLE_STONE_ITEM = ITEMS.register("unbreakable_stone",
            () -> new BlockItem(BlockInit.UNBREAKABLE_STONE.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Supplier<BlockItem> UNBREAKABLE_GLOWSTONE_ITEM = ITEMS.register("unbreakable_glowstone",
            () -> new BlockItem(BlockInit.UNBREAKABLE_GLOWSTONE.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Supplier<BlockItem> UNBREAKABLE_ANDESITE_ITEM = ITEMS.register("unbreakable_andesite",
            () -> new BlockItem(BlockInit.UNBREAKABLE_ANDESITE.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Supplier<BlockItem> UNBREAKABLE_POLISHED_ANDESITE_ITEM = ITEMS.register("unbreakable_polished_andesite",
            () -> new BlockItem(BlockInit.UNBREAKABLE_POLISHED_ANDESITE.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
}