package net.narutoxboruto.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.main.Main;

/**
 * The ball of the Rasengan (and the heart of the Rasenshuriken): a small bright core inside three cubes of growing
 * size, each turned a different way and each spinning on its own axes, so that together they make a spiky ball of
 * spinning chakra. The outer cubes are see-through.
 */
public class RasenganModel {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "rasengan"), "main");

    private final ModelPart core;
    private final ModelPart shellOne;
    private final ModelPart shellTwo;
    private final ModelPart shellThree;

    public RasenganModel(ModelPart root) {
        this.core = root.getChild("core");
        this.shellOne = root.getChild("shell_one");
        this.shellTwo = root.getChild("shell_two");
        this.shellThree = root.getChild("shell_three");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        root.addOrReplaceChild("shell_one", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        root.addOrReplaceChild("shell_two", CubeListBuilder.create().texOffs(0, 40)
                .addBox(-7.0F, -7.0F, -7.0F, 14.0F, 14.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        root.addOrReplaceChild("shell_three", CubeListBuilder.create().texOffs(0, 72)
                .addBox(-9.0F, -9.0F, -9.0F, 18.0F, 18.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Draws the ball turned to {@code age} ticks of spinning, all the layers see-through by {@code alpha} more. */
    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay, float age, float alpha) {
        core.xRot = age * 0.20F;
        core.yRot = age * 0.30F;
        shellOne.xRot = age * 0.35F + 0.785F;
        shellOne.yRot = age * 0.50F;
        shellTwo.yRot = -age * 0.45F + 0.785F;
        shellTwo.zRot = age * 0.30F + 0.6F;
        shellThree.xRot = age * 0.25F;
        shellThree.yRot = age * 0.60F + 0.4F;
        shellThree.zRot = 0.785F;
        core.render(poseStack, vertexConsumer, light, overlay, color(1.00F * alpha));
        shellOne.render(poseStack, vertexConsumer, light, overlay, color(0.90F * alpha));
        shellTwo.render(poseStack, vertexConsumer, light, overlay, color(0.65F * alpha));
        shellThree.render(poseStack, vertexConsumer, light, overlay, color(0.40F * alpha));
    }

    private static int color(float alpha) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255.0F)));
        return (a << 24) | 0xFFFFFF;
    }
}
