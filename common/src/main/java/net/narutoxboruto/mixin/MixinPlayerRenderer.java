package net.narutoxboruto.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.narutoxboruto.client.WallFrame;
import net.narutoxboruto.client.renderer.IronSkinLayer;
import net.narutoxboruto.effect.ModEffects;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to adjust player model for special poses.
 */
@Mixin(PlayerRenderer.class)
public class MixinPlayerRenderer {

    @Inject(method = "setupRotations", at = @At("TAIL"))
    private void onSetupRotations(AbstractClientPlayer player, PoseStack poseStack, 
            float ageInTicks, float rotationYaw, float partialTicks, float scale, CallbackInfo ci) {
        // Walking on a wall with Chakra Control: the model turns with the player (the local player only)
        WallFrame.transformModel(poseStack, player, rotationYaw, partialTicks);
    }

    /** Earth Release: Iron Skin: the arm in first person is covered in hardened earth too. */
    @Inject(method = "renderHand", at = @At("TAIL"))
    private void narutoxboruto$ironSkinHand(PoseStack poseStack, MultiBufferSource buffer, int light, AbstractClientPlayer player,
                                            ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        if (ModEffects.IRON_SKIN != null && player.hasEffect(ModEffects.IRON_SKIN)) {
            arm.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(IronSkinLayer.TEXTURE)),
                    net.minecraft.client.renderer.LightTexture.pack(Math.max(net.minecraft.client.renderer.LightTexture.block(light), 7), Math.max(net.minecraft.client.renderer.LightTexture.sky(light), 7)),
                    OverlayTexture.NO_OVERLAY, IronSkinLayer.COLOR);
        }
    }
}
