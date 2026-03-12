package net.narutoxboruto.client.renderer.throwables;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.entities.throwables.AbstractThrowableWeapon;

public class ThrowableWeaponRenderer <T extends AbstractThrowableWeapon> extends EntityRenderer<T> {
    ItemRenderer itemRenderer;

    public ThrowableWeaponRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.itemRenderer = pContext.getItemRenderer();
    }

    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        ItemStack itemstack = getRenderItem(pEntity);
        BakedModel bakedmodel = this.itemRenderer.getModel(itemstack, pEntity.level(), null, pEntity.getId());
        this.setupRotations(pEntity, pPartialTicks, pMatrixStack);
        this.itemRenderer.render(itemstack, ItemDisplayContext.FIXED, false, pMatrixStack, pBuffer,
                pPackedLight, OverlayTexture.NO_OVERLAY, bakedmodel);
        pMatrixStack.popPose();
    }

    protected ItemStack getRenderItem(T pEntity) {
        return pEntity.getItem();
    }

    protected void setupRotations(T pEntity, float pPartialTicks, PoseStack pMatrixStack) {
        pMatrixStack.mulPose(
                Axis.YP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.yRotO, pEntity.getYRot()) - 90.0F));
        pMatrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot())));
        pMatrixStack.mulPose(Axis.XP.rotationDegrees(90));
    }

    @Override
    public ResourceLocation getTextureLocation(T pEntity) {
        return null;
    }
}
