package com.pla.pladailyboss.ftb;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbchunks.data.ClaimedChunkManager;
import dev.ftb.mods.ftbchunks.data.FTBChunksTeamData;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import static dev.ftb.mods.ftbchunks.data.FTBChunksAPI.getManager;

public class ClaimChunkHelper {
    private static ClaimChunkHelper instance;
    private final ClaimedChunkManager claimedChunkManager;

    public ClaimChunkHelper(ClaimedChunkManager claimedChunkManager) throws CommandSyntaxException {
        this.claimedChunkManager = claimedChunkManager;
    }

    public static ClaimChunkHelper getInstance(MinecraftServer server) throws CommandSyntaxException {
        if (instance == null) {
            instance = new ClaimChunkHelper(getManager());
        }
        return instance;
    }

    public void claimChunk(CommandSourceStack source, ServerPlayer pPlayer, BlockPos pos) throws CommandSyntaxException {
        ChunkPos chunkPos = new ChunkPos(pos);
        ResourceKey<Level> dimension = pPlayer.level.dimension();
        ChunkDimPos chunkDimPos = new ChunkDimPos(dimension, chunkPos.x, chunkPos.z);

        FTBChunksTeamData teamData = claimedChunkManager.getData(pPlayer);
        teamData.claim(source, chunkDimPos, false);
        teamData.load(source, chunkDimPos, false);
    }
}

