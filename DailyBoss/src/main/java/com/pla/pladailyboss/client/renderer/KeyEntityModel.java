package com.pla.pladailyboss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class KeyEntityModel<T extends Entity> extends EntityModel<T>  {
    private final ModelPart board;
    private final ModelPart stick;

    public KeyEntityModel(ModelPart root) {
        this.board = root.getChild("board");
        this.stick = root.getChild("stick");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition board = partdefinition.addOrReplaceChild("board", CubeListBuilder.create().texOffs(0, 2).addBox(-19.0F, -51.0F, -1.0F, 39.0F, 23.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(52, 64).addBox(-13.0F, -11.0F, -1.0F, 13.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition stick = partdefinition.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 41).addBox(-1.0F, -23.0F, -2.0F, 3.0F, 35.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(2, 34).addBox(-1.0F, -28.0F, -1.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 80, 80);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        board.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        stick.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
