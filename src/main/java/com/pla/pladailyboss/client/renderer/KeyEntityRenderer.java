package com.pla.pladailyboss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.pla.pladailyboss.PlaDailyBoss;
import com.pla.pladailyboss.entity.KeyEntity;
import com.pla.pladailyboss.enums.KeyEntityState;
import com.pla.pladailyboss.init.BlockInit;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class KeyEntityRenderer extends MobRenderer<KeyEntity, KeyEntityModel<KeyEntity>> {
    private final EntityRendererProvider.Context context;
    private static final Logger LOGGER = LogManager.getLogger();

    public KeyEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new KeyEntityModel<>(pContext.bakeLayer(ModModelLayers.KEY_ENTITY_LAYER)), 0.0f);
        this.context = pContext;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull KeyEntity pEntity) {
        return ResourceLocation.fromNamespaceAndPath(PlaDailyBoss.MOD_ID, "textures/entity/key_entity.png");
    }

    @Override
    protected int getBlockLightLevel(KeyEntity pEntity, BlockPos pPos) {
        return 15;
    }

    @Override
    public void render(@NotNull KeyEntity pEntity, float pEntityYaw, float pPartialTicks, @NotNull PoseStack pPoseStack,
                       @NotNull MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);

        if (pEntity.getState() == KeyEntityState.NORMAL) {
            Level level = pEntity.level();
            int packedLight = LightTexture.pack(15, 15);
            double relativeGameTime = level.getGameTime() + pPartialTicks;
            double offset = Math.sin(relativeGameTime / 10.0) / 20.0;

            pPoseStack.pushPose();
            pPoseStack.translate(0, 3.5 + offset, 0);
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(45.0f));

            float spin = (float) ((relativeGameTime * 4) % 360);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(spin));
            this.context.getItemRenderer().renderStatic(
                    new ItemStack(BlockInit.SPINNING_BLOCK.get()),
                    ItemDisplayContext.FIXED,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    pPoseStack,
                    pBuffer,
                    level,
                    0
            );

            pPoseStack.popPose();
        } else {
            long updatedTime = pEntity.getUpdatedStateTime();
            long remaining = pEntity.getRechargeCooldown() - (System.currentTimeMillis() - updatedTime);

            if (remaining > 0) {
                long seconds = (remaining / 1000) % 60;
                long minutes = (remaining / (1000 * 60)) % 60;
                long hours = remaining / (1000 * 60 * 60);

                String timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                pPoseStack.pushPose();
                pPoseStack.translate(0, 3.5, 0);
                Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

                pPoseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
                pPoseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
                pPoseStack.scale(-0.02f, -0.02f, 0.02f);

                context.getFont().drawInBatch(
                        timerText,
                        -context.getFont().width(timerText) / 2f,
                        0,
                        0x00FF00,
                        false,
                        pPoseStack.last().pose(),
                        pBuffer,
                        Font.DisplayMode.SEE_THROUGH,
                        0,
                        LightTexture.FULL_BRIGHT
                );

                pPoseStack.popPose();
            }
        }
    }
}
