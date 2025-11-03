package com.pla.pladailyboss.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("pladailyboss", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(
                packetId++,
                BossListMessage.class,
                BossListMessage::encode,
                BossListMessage::decode,
                BossListMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(
                packetId++,
                AskForDataMessage.class,
                AskForDataMessage::encode,
                AskForDataMessage::decode,
                AskForDataMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }
}