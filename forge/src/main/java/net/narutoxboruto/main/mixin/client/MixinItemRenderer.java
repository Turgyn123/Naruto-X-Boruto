package net.narutoxboruto.main.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.client.renderer.item.KibaLightningRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the lightning to the Kiba sword while its ability is active. All of the drawing is in
 * {@link KibaLightningRenderer}, shared by every loader.
 */
@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Inject(method = "render", at = @At("TAIL"))
    private void narutoxboruto$renderKibaLightningOverlay(
            ItemStack itemStack,
            ItemDisplayContext displayContext,
            boolean leftHand,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay,
            BakedModel model,
            CallbackInfo ci) {
        KibaLightningRenderer.render(itemStack, displayContext, poseStack, buffer, model);
    }
}
