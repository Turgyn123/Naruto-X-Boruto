package net.narutoxboruto.mixin;

import net.minecraft.world.entity.Entity;
import net.narutoxboruto.dojutsu.ByakuganVision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Client-side: forces {@link Entity#isCurrentlyGlowing()} to return true for any entity
 * within Byakugan range when the local player has Byakugan active. Visible only on the
 * local client (server-side glowing flags are untouched), so other players are unaffected.
 */
@Mixin(Entity.class)
public abstract class MixinEntityGlowing {

    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    private void nxb$byakuganGlow(CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue())) return;
        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide) return;
        if (ByakuganVision.shouldHighlight(self)) {
            cir.setReturnValue(true);
        }
    }
}
