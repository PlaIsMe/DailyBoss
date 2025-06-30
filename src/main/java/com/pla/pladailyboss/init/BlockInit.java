package com.pla.pladailyboss.init;

import com.pla.pladailyboss.PlaDailyBoss;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, PlaDailyBoss.MOD_ID);

    public static final RegistryObject<Block> SPINNING_BLOCK = BLOCKS.register("spinning_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BEDROCK)));

    public static final RegistryObject<Block> UNBREAKABLE_STONE = BLOCKS.register("unbreakable_stone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BEDROCK)));

    public static final RegistryObject<Block> UNBREAKABLE_GLOWSTONE = BLOCKS.register("unbreakable_glowstone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BEDROCK).lightLevel(state -> 15)));

    public static final RegistryObject<Block> UNBREAKABLE_ANDESITE = BLOCKS.register("unbreakable_andesite",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BEDROCK)));

    public static final RegistryObject<Block> UNBREAKABLE_POLISHED_ANDESITE = BLOCKS.register("unbreakable_polished_andesite",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BEDROCK)));
}