package net.narutoxboruto.mixin;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.items.jutsus.HidingInRock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Earth Release: Hiding in Rock: the player is drawn see-through, like a ghost, and not fully visible (the renderer draws a body that is not visible, but is not invisible, faintly). */
@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {

    @Inject(method = "isBodyVisible", at = @At("HEAD"), cancellable = true)
    private void narutoxboruto$seeThrough(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && HidingInRock.isActive(player)) {
            cir.setReturnValue(false);
        }
    }
}
