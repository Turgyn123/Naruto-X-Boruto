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
 * The four blades of wind of the Rasenshuriken. Each blade is a long thin plate with a wider plate on its inside, a
 * little twisted like the blade of a propeller. The blades lie in the plane of x and y and the model spins round the
 * z axis, which points the way the jutsu is aimed.
 */
public class RasenshurikenBladesModel {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "rasenshuriken_blades"), "main");

    private static final int BLADES = 4;
    private final ModelPart[] blades = new ModelPart[BLADES];

    public RasenshurikenBladesModel(ModelPart root) {
        for (int i = 0; i < BLADES; i++) blades[i] = root.getChild("blade_" + i);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (int i = 0; i < BLADES; i++) {
            PartDefinition blade = root.addOrReplaceChild("blade_" + i, CubeListBuilder.create(),
                    PartPose.rotation(0.0F, 0.0F, i * (float) (Math.PI / 2.0D)));
            blade.addOrReplaceChild("long", CubeListBuilder.create().texOffs(0, 0)
                    .addBox(3.0F, -2.0F, -1.0F, 22.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.rotation(0.35F, 0.0F, 0.0F));
            blade.addOrReplaceChild("wide", CubeListBuilder.create().texOffs(0, 8)
                    .addBox(2.0F, -4.5F, -0.5F, 12.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.rotation(0.35F, 0.0F, 0.0F));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** Draws the blades turned by {@code spin} radians round the aim. */
    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay, float spin, float alpha) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255.0F)));
        int color = (a << 24) | 0xFFFFFF;
        for (int i = 0; i < BLADES; i++) {
            blades[i].zRot = i * (float) (Math.PI / 2.0D) + spin;
            blades[i].render(poseStack, vertexConsumer, light, overlay, color);
        }
    }
}
