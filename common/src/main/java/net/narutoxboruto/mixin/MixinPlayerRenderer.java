package net.narutoxboruto.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import com.mojang.math.Axis;
import net.narutoxboruto.client.BorutoDash;
import net.narutoxboruto.client.WallFrame;
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
        // Being launched by the shadow clone of the Boruto Stream: lying flat, face down, like swimming
        if (!player.isVisuallySwimming() && BorutoDash.applies(player)) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.translate(0.0F, -1.0F, 0.3F);
        }
    }
}

