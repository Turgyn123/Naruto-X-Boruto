package net.narutoxboruto.mixin;

import net.narutoxboruto.main.platform.Services;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.climber.ClimberComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to override climbing-related checks for wall-running players.
 * Prevents vanilla climbing interference and adjusts ground position detection.
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityClimbing {
    
    /**
     * Disable vanilla climbing when using custom wall-running.
     */
    @Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
    private void onOnClimbable(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return;
        }
        
        ClimberComponent climber = Services.PLATFORM.getClimberComponent(player);
        if (climber.isClimbing()) {
            cir.setReturnValue(false);  // Not on vanilla climbable (ladders, vines)
        }
    }
}
