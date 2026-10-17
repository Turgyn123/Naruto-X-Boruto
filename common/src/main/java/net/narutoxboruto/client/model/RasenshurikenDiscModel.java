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
 * The flat disc of swirling wind of the Rasenshuriken while it is charged: four thin square plates lying on top of each
 * other, each turned a little further than the one below it, so together they make a round disc. The plates turn
 * against each other, so the swirl on them is never the same twice.
 */
public class RasenshurikenDiscModel {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "rasenshuriken_disc"), "main");

    private static final int PLATES = 4;
    private final ModelPart[] plates = new ModelPart[PLATES];

    public RasenshurikenDiscModel(ModelPart root) {
        for (int i = 0; i < PLATES; i++) plates[i] = root.getChild("plate_" + i);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (int i = 0; i < PLATES; i++) {
            root.addOrReplaceChild("plate_" + i, CubeListBuilder.create().texOffs(0, 0)
                    .addBox(-14.0F, -0.5F + i * 0.4F, -14.0F, 28.0F, 1.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** Draws the disc, spun by {@code spin} radians, every plate see-through by {@code alpha}. */
    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay, float spin, float alpha) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255.0F)));
        int color = (a << 24) | 0xFFFFFF;
        for (int i = 0; i < PLATES; i++) {
            plates[i].yRot = i * (float) (Math.PI / 8.0D) + spin * (i % 2 == 0 ? 1.0F : -0.8F);
            plates[i].render(poseStack, vertexConsumer, light, overlay, color);
        }
    }
}
