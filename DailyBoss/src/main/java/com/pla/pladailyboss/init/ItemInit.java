package com.pla.pladailyboss.init;

import com.pla.pladailyboss.PlaDailyBoss;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PlaDailyBoss.MOD_ID);

    public static final RegistryObject<BlockItem> SPINNING_BLOCK_ITEM = ITEMS.register("spinning_block",
            () -> new BlockItem(BlockInit.SPINNING_BLOCK.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<BlockItem> UNBREAKABLE_STONE_ITEM = ITEMS.register("unbreakable_stone",
            () -> new BlockItem(BlockInit.UNBREAKABLE_STONE.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<BlockItem> UNBREAKABLE_GLOWSTONE_ITEM = ITEMS.register("unbreakable_glowstone",
            () -> new BlockItem(BlockInit.UNBREAKABLE_GLOWSTONE.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<BlockItem> UNBREAKABLE_ANDESITE_ITEM = ITEMS.register("unbreakable_andesite",
            () -> new BlockItem(BlockInit.UNBREAKABLE_ANDESITE.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<BlockItem> UNBREAKABLE_POLISHED_ANDESITE_ITEM = ITEMS.register("unbreakable_polished_andesite",
            () -> new BlockItem(BlockInit.UNBREAKABLE_POLISHED_ANDESITE.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
}