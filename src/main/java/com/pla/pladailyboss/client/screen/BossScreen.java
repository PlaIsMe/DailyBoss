package com.pla.pladailyboss.client.screen;

import com.pla.pladailyboss.PlaDailyBoss;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class BossScreen extends Screen {
    private static final Component TITLE =
            Component.translatable("gui." + PlaDailyBoss.MOD_ID + ".boss_screen");

    private static final String posterPath = "textures/gui/entity_posters/";

    private static final ResourceLocation BACKGROUND =
            new ResourceLocation(PlaDailyBoss.MOD_ID, "textures/gui/screen_background.png");

    private int currentPage;
    private int totalPages;
    private Button pageButton;

    private static final Logger LOGGER = LogManager.getLogger();

    private final List<String> entityIdStrings = Arrays.asList(
            "minecraft:wither",
            "minecraft:warden",
            "minecraft:wither",
            "minecraft:warden",
            "minecraft:wither",
            "minecraft:warden",
            "minecraft:wither",
            "minecraft:warden",
            "minecraft:wither",
            "minecraft:warden",
            "minecraft:wither",
            "minecraft:wither",
            "minecraft:wither",
            "minecraft:wither",
            "minecraft:wither",
            "minecraft:wither",
            "minecraft:wither",
            "minecraft:wither",
            "minecraft:wither",
            "minecraft:wither",
            "minecraft:warden",
            "minecraft:warden",
            "minecraft:warden",
            "minecraft:warden",
            "minecraft:warden",
            "minecraft:warden",
            "minecraft:warden",
            "minecraft:warden",
            "minecraft:warden",
            "minecraft:abcd"
    );

    private static final int BOX_WIDTH = 60;
    private static final int BOX_HEIGHT = 80;
    private static final int PADDING_HORIZONTAL = 10;
    private static final int PADDING_VERTICAL = 8;
    private static final int BUTTON_SIZE = 20;

    public BossScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        super.init();

        int bgLeft = width / 10;
        int bgRight = width - width / 10;
        int bgTop = height / 10;
        int bgBottom = height - height / 10;
        int bgWidth = bgRight - bgLeft;
        int bgHeight = bgBottom - bgTop;

        int maxColumns = Math.max(1, (bgWidth + PADDING_HORIZONTAL) / (BOX_WIDTH + PADDING_HORIZONTAL));
        int maxRows = Math.max(1, (bgHeight + PADDING_VERTICAL) / (BOX_HEIGHT + PADDING_VERTICAL));

        int entitiesPerPage = maxRows * maxColumns;
        totalPages = Math.max(1, (int) Math.ceil(entityIdStrings.size() / (double) entitiesPerPage));
        currentPage = Math.max(1, Math.min(currentPage, totalPages));

        addRenderableWidget(
                Button.builder(
                                Component.literal("Daily Boss List"),
                                this::doNothing)
                        .bounds(bgLeft + (bgWidth - BUTTON_SIZE * 5) / 2, bgTop - BUTTON_SIZE, BUTTON_SIZE * 5, BUTTON_SIZE)
                        .build());

        addRenderableWidget(
                Button.builder(
                                Component.literal("<"),
                                this::handlePrevPage)
                        .bounds(bgRight - BUTTON_SIZE * 4 - PADDING_HORIZONTAL, bgBottom + PADDING_VERTICAL / 2, BUTTON_SIZE, BUTTON_SIZE)
                        .build());

        pageButton = addRenderableWidget(
                Button.builder(
                                Component.literal(currentPage + "/" + totalPages),
                                this::doNothing)
                        .bounds(bgRight - BUTTON_SIZE * 3 - PADDING_HORIZONTAL, bgBottom + PADDING_VERTICAL / 2, BUTTON_SIZE * 2, BUTTON_SIZE)
                        .build());

        addRenderableWidget(
                Button.builder(
                                Component.literal(">"),
                                this::handleNextPage)
                        .bounds(bgRight - BUTTON_SIZE - PADDING_HORIZONTAL, bgBottom + PADDING_VERTICAL / 2, BUTTON_SIZE, BUTTON_SIZE)
                        .build());
    }

    public boolean textureExists(ResourceLocation location) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        return resourceManager.getResource(location).isPresent();
    }

    private void drawEntityCard(GuiGraphics guiGraphics, int x, int y, String entityId) {
        String entityPoster = posterPath + entityId.replace(":", "/") + "/" + entityId.split(":")[1] + "_enabled.png";
        ResourceLocation entityCardTexture = new ResourceLocation(PlaDailyBoss.MOD_ID, entityPoster);
        if (!textureExists(entityCardTexture)) {
            entityCardTexture = new ResourceLocation(PlaDailyBoss.MOD_ID, posterPath + "not_found/not_found" + "_enabled.png");
        }
        guiGraphics.blit(
                entityCardTexture,
                x, y,
                0, 0,
                BOX_WIDTH, BOX_HEIGHT,
                BOX_WIDTH, BOX_HEIGHT
        );
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics);
        int bgLeft = width / 10;
        int bgRight = width - width / 10;
        int bgTop = height / 10;
        int bgBottom = height - height / 10;
        int bgWidth = bgRight - bgLeft;
        int bgHeight = bgBottom - bgTop;
        guiGraphics.blit(BACKGROUND, bgLeft, bgTop, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        int maxColumns = Math.max(1, (bgWidth + PADDING_HORIZONTAL) / (BOX_WIDTH + PADDING_HORIZONTAL));
        int totalWidth = maxColumns * BOX_WIDTH + (maxColumns - 1) * PADDING_HORIZONTAL;
        int startX = bgLeft + (bgWidth - totalWidth) / 2;

        int maxRows = Math.max(1, (bgHeight + PADDING_VERTICAL) / (BOX_HEIGHT + PADDING_VERTICAL));
        int totalHeight = maxRows * BOX_HEIGHT + (maxRows - 1) * PADDING_VERTICAL;
        int startY = bgTop + (bgHeight - totalHeight) / 2;

        int entitiesPerPage = maxRows * maxColumns;
        totalPages = Math.max(1, (int) Math.ceil(entityIdStrings.size() / (double) entitiesPerPage));
        currentPage = Math.max(1, Math.min(currentPage, totalPages));
        int startIndex = (currentPage - 1) * entitiesPerPage;
        int endIndex = Math.min(startIndex + entitiesPerPage, entityIdStrings.size());
        List<String> entitiesToRender = entityIdStrings.subList(startIndex, endIndex);

        for (int i = 0; i < entitiesToRender.size(); i++) {
            int col = i % maxColumns;
            int row = i / maxColumns;

            int x = startX + col * (BOX_WIDTH + PADDING_HORIZONTAL);
            int y = startY + row * (BOX_HEIGHT + PADDING_HORIZONTAL);

            drawEntityCard(guiGraphics, x, y, entitiesToRender.get(i));
        }
    }

    private void handlePrevPage(Button button) {
        if (currentPage > 1) {
            currentPage--;
        }
        pageButton.setMessage(Component.literal(currentPage + "/" + totalPages));
    }

    private void handleNextPage(Button button) {
        if (currentPage < totalPages) {
            currentPage++;
        }
        pageButton.setMessage(Component.literal(currentPage + "/" + totalPages));
    }

    private void doNothing(Button button) {
    }

    private ResourceLocation getEntityIcon(String idStr) {
        String[] parts = idStr.split(":");
        String iconName = parts.length == 2 ? parts[1] : idStr;
        return new ResourceLocation(PlaDailyBoss.MOD_ID, "textures/gui/entity_icons/test.png");
    }
}
