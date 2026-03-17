package net.narutoxboruto.mixin;

import net.narutoxboruto.main.platform.Services;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.narutoxboruto.capabilities.info.NarutoRun;
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
        
        // Handle Naruto Run pose
        NarutoRun narutoRun = Services.PLATFORM.getNarutoRun(player);
        if (narutoRun != null && narutoRun.isActive()) {
            poseStack.translate(0, 0.2, 1);
            poseStack.mulPose(Axis.XP.rotation(-0.7854F));
        }
    }
}

