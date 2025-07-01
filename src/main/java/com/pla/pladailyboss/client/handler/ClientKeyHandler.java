package com.pla.pladailyboss.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ClientKeyHandler {
    public static final KeyMapping OPEN_ENTITY_GUI_KEY = new KeyMapping(
            "key.pladailyboss.open_entity_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            "key.categories.misc"
    );
}
