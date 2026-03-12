package net.narutoxboruto.client.renderer.throwables;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.narutoxboruto.entities.throwables.AbstractThrowableWeapon;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

public class ShurikenRenderer <T extends AbstractThrowableWeapon> extends ThrowableWeaponRenderer<T> {
    public ShurikenRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    protected void setupRotations(T pEntity, float pPartialTicks, PoseStack pMatrixStack) {
        float spin = pEntity.getSpin(pPartialTicks);

        if (pEntity.isInWater()) {
            Quaternionf qx = new Quaternionf(new AxisAngle4f(spin, 1.0f, 0.0f, 0.0f));
            Quaternionf qy = new Quaternionf(new AxisAngle4f(spin, 0.0f, 1.0f, 0.0f));
            Quaternionf qz = new Quaternionf(new AxisAngle4f(spin, 0.0f, 0.0f, 1.0f));

            pMatrixStack.mulPose(qx);
            pMatrixStack.mulPose(qy);
            pMatrixStack.mulPose(qz);
        } else {
            super.setupRotations(pEntity, pPartialTicks, pMatrixStack);

            Quaternionf qNegY = new Quaternionf(new AxisAngle4f(-spin, 0.0f, 1.0f, 0.0f));
            pMatrixStack.mulPose(qNegY);
        }
    }
}
