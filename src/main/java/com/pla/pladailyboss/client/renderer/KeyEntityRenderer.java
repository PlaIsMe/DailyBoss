package com.pla.pladailyboss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.pla.pladailyboss.PlaDailyBoss;
import com.pla.pladailyboss.entity.KeyEntity;
import com.pla.pladailyboss.init.BlockInit;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class KeyEntityRenderer extends MobRenderer<KeyEntity, KeyEntityModel<KeyEntity>> {
    private final EntityRendererProvider.Context context;

    public KeyEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new KeyEntityModel<>(pContext.bakeLayer(ModModelLayers.KEY_ENTITY_LAYER)), 0.0f);
        this.context = pContext;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull KeyEntity pEntity) {
        return new ResourceLocation(PlaDailyBoss.MOD_ID, "textures/entity/key_entity.png");
    }

    @Override
    public void render(@NotNull KeyEntity pEntity, float pEntityYaw, float pPartialTicks, @NotNull PoseStack pPoseStack,
                       @NotNull MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);

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
    }
}
