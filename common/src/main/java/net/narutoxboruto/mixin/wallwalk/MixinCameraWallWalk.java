package net.narutoxboruto.mixin.wallwalk;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.client.WallFrame;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Turns the camera with the player while they walk on a wall (see {@link WallFrame}). It runs after the
 * game has set the camera up, so nothing changes while the player is not on a wall.
 *
 * This is in its own optional mixin config: if a name here ever stops matching, the game still starts,
 * only the wall walking looks wrong.
 */
@Mixin(Camera.class)
public abstract class MixinCameraWallWalk {

    @Shadow private Vec3 position;
    @Shadow @Final private Quaternionf rotation;
    @Shadow @Final private Vector3f forwards;
    @Shadow @Final private Vector3f up;
    @Shadow @Final private Vector3f left;

    @Inject(method = "setup", at = @At("RETURN"))
    private void nxb$wallWalk(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse,
                              float partialTick, CallbackInfo ci) {
        Vec3 moved = WallFrame.adjustCamera(entity, detached && thirdPersonReverse, partialTick,
                this.position, this.rotation, this.forwards, this.up, this.left);
        if (moved != null) {
            this.position = moved;
        }
    }
}
