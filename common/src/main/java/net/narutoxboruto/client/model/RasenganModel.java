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
 * The ball of the Rasengan and of the Rasenshuriken: eight see-through cubes of the same size, each turned a different
 * way and each spinning on its own axes. Where they overlap the ball is solid, and where only one is it is soft, so
 * together they make a round, soft ball with a jagged, glowing edge. A small bright core sits in the middle.
 */
public class RasenganModel {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "rasengan"), "main");

    private static final int CUBES = 8;
    /** How each cube is turned to begin with, in eighths of a quarter turn. */
    private static final float[][] TURNS = {
            {0, 0, 0}, {1, 0, 0}, {0, 1, 0}, {0, 0, 1}, {1, 1, 0}, {1, 0, 1}, {0, 1, 1}, {1, 1, 1}};

    private final ModelPart[] cubes = new ModelPart[CUBES];
    private final ModelPart core;

    public RasenganModel(ModelPart root) {
        for (int i = 0; i < CUBES; i++) cubes[i] = root.getChild("cube_" + i);
        this.core = root.getChild("core");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (int i = 0; i < CUBES; i++) {
            root.addOrReplaceChild("cube_" + i, CubeListBuilder.create().texOffs(0, 0)
                    .addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        }
        root.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 26)
                .addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** Draws the ball turned to {@code age} ticks of spinning, every cube see-through by {@code cubeAlpha}. */
    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay, float age, float cubeAlpha, float coreAlpha) {
        int cubeColor = color(cubeAlpha);
        for (int i = 0; i < CUBES; i++) {
            float quarter = (float) (Math.PI / 4.0D);
            float speed = 0.18F + 0.07F * i;
            float sign = i % 2 == 0 ? 1.0F : -1.0F;
            cubes[i].xRot = TURNS[i][0] * quarter + age * speed * sign * 0.8F;
            cubes[i].yRot = TURNS[i][1] * quarter + age * speed * 0.6F;
            cubes[i].zRot = TURNS[i][2] * quarter - age * speed * sign * 0.4F;
            cubes[i].render(poseStack, vertexConsumer, light, overlay, cubeColor);
        }
        core.xRot = age * 0.3F;
        core.yRot = age * 0.45F;
        core.render(poseStack, vertexConsumer, light, overlay, color(coreAlpha));
    }

    private static int color(float alpha) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255.0F)));
        return (a << 24) | 0xFFFFFF;
    }
}
