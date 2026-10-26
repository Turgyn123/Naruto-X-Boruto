package net.narutoxboruto.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.client.model.GolemModel;
import net.narutoxboruto.entities.jutsus.GolemEntity;
import net.narutoxboruto.main.Main;

/** Draws the Golem with its own model (see {@link GolemModel}): a hunched giant built of tan boulders. */
public class GolemRenderer extends MobRenderer<GolemEntity, GolemModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/golem.png");

    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context, new GolemModel(context.bakeLayer(GolemModel.LAYER_LOCATION)), 1.4F);
    }

    @Override
    protected void setupRotations(GolemEntity golem, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float scale) {
        super.setupRotations(golem, poseStack, ageInTicks, rotationYaw, partialTick, scale);
        // Walks with a heavy sway, like the iron golem
        if (!((double) golem.walkAnimation.speed() < 0.01D)) {
            float position = golem.walkAnimation.position(partialTick) + 6.0F;
            float sway = (Math.abs(position % 13.0F - 6.5F) - 3.25F) / 3.25F;
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(6.5F * sway));
        }
    }

    /** No name above it. */
    @Override
    protected boolean shouldShowName(GolemEntity entity) {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(GolemEntity golem) {
        return TEXTURE;
    }
}
