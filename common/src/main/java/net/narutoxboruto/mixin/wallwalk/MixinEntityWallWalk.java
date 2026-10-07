package net.narutoxboruto.mixin.wallwalk;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.client.WallFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The mouse and the eye of the local player while they walk on a wall (see {@link WallFrame}): the mouse
 * turns the look inside the sideways world, and the eye, which the crosshair is cast from, sits out from
 * the wall instead of above the head. Both do nothing for every other entity and when no wall is walked.
 *
 * This is in its own optional mixin config: if a name here ever stops matching, the game still starts.
 */
@Mixin(Entity.class)
public abstract class MixinEntityWallWalk {

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void nxb$turn(double yRot, double xRot, CallbackInfo ci) {
        if (WallFrame.turn((Entity) (Object) this, yRot, xRot)) {
            ci.cancel();
        }
    }

    @Inject(method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void nxb$eyePosition(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        Vec3 vanilla = cir.getReturnValue();
        Vec3 moved = WallFrame.adjustEye((Entity) (Object) this, partialTick, vanilla);
        if (moved != vanilla) {
            cir.setReturnValue(moved);
        }
    }
}
