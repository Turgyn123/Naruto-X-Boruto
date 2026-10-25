package net.narutoxboruto.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.entities.jutsus.GolemEntity;
import net.narutoxboruto.main.Main;

/** Draws the Golem as a big humanoid of tan rock: the iron golem's body, in the stone of the technique and a third bigger. */
public class GolemRenderer extends MobRenderer<GolemEntity, IronGolemModel<GolemEntity>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/golem.png");
    private static final float SCALE = 1.3F;

    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context, new IronGolemModel<>(context.bakeLayer(ModelLayers.IRON_GOLEM)), 1.0F);
    }

    @Override
    protected void scale(GolemEntity golem, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
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

    @Override
    public ResourceLocation getTextureLocation(GolemEntity golem) {
        return TEXTURE;
    }
}
