package com.pla.pladailyboss.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class CodecUtil {
    public static <E extends Enum<E>> StreamCodec<ByteBuf, E> enumCodec(Class<E> enumClass) {
        return StreamCodec.of(
                (buf, value) -> buf.writeInt(value.ordinal()),
                buf -> {
                    int ordinal = buf.readInt();
                    E[] values = enumClass.getEnumConstants();
                    if (ordinal < 0 || ordinal >= values.length) {
                        throw new IllegalArgumentException("Invalid enum ordinal " + ordinal + " for " + enumClass.getName());
                    }
                    return values[ordinal];
                }
        );
    }
}

