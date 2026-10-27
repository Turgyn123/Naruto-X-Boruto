package net.narutoxboruto.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.narutoxboruto.client.WallFrame;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.client.IronSkinSkin;
import net.narutoxboruto.items.jutsus.IronSkin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
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

    /** Earth Release: Iron Skin: the skin of the player is the dirt texture while it is on. */
    @Inject(method = "getTextureLocation(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"), cancellable = true)
    private void narutoxboruto$ironSkinTexture(AbstractClientPlayer player, CallbackInfoReturnable<ResourceLocation> cir) {
        if (IronSkin.isActive(player)) {
            cir.setReturnValue(IronSkinSkin.textureFor(player));
        }
    }

    /** The arm in first person too (it takes its texture straight from the skin of the player). */
    @Inject(method = "renderHand", at = @At("TAIL"))
    private void narutoxboruto$ironSkinHand(PoseStack poseStack, MultiBufferSource buffer, int light, AbstractClientPlayer player,
                                            ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        if (IronSkin.isActive(player)) {
            arm.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(IronSkinSkin.textureFor(player))), light, OverlayTexture.NO_OVERLAY);
        }
    }
}
