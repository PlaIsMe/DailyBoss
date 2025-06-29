package com.pla.pladailyboss.init;

import com.pla.pladailyboss.PlaDailyBoss;
import com.pla.pladailyboss.block.SpinningBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PlaDailyBoss.MOD_ID);

    public static final RegistryObject<SpinningBlock> SPINNING_BLOCK = registerBlock(
            () -> new SpinningBlock(BlockBehaviour.Properties
                    .copy(Blocks.BEDROCK)
                    .lightLevel(state -> 15)
                    .sound(SoundType.AMETHYST)));

    private static <T extends Block> RegistryObject<T> registerBlock(Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register("spinning_block", block);
        registerBlockItem(toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(RegistryObject<T> block) {
        ItemInit.ITEMS.register("spinning_block", () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}