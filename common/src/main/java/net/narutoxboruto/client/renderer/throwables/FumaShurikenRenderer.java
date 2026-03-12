package net.narutoxboruto.client.renderer.throwables;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.narutoxboruto.entities.throwables.AbstractThrowableWeapon;

public class FumaShurikenRenderer <T extends AbstractThrowableWeapon> extends ThrowableWeaponRenderer<T> {

    public FumaShurikenRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    protected void setupRotations(T pEntity, float pPartialTicks, PoseStack pMatrixStack) {
        float spin = pEntity.getFumaSpin(pPartialTicks);

        // Start with basic entity rotations
        pMatrixStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-pEntity.getYRot()));
        pMatrixStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(pEntity.getXRot()));

        // Spin horizontally around the Y axis
        pMatrixStack.mulPose(com.mojang.math.Axis.YP.rotation(spin));

        // Rotate to make shuriken lay flat
        pMatrixStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(0));
    }
}
