package net.narutoxboruto.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.narutoxboruto.effect.ModEffects;
import net.narutoxboruto.items.PreventSlow;
import net.narutoxboruto.items.jutsus.HidingInRock;
import net.narutoxboruto.client.WallFrame;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer {
    @Shadow
    public Input input;

    public MixinLocalPlayer(ClientLevel p_234112_, GameProfile p_234113_, @Nullable ProfilePublicKey p_234114_) {
        super(p_234112_, p_234113_);
    }

    @Inject(method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal = 0))
    public void s(CallbackInfo ci) {
        if (this.useItem.getItem() instanceof PreventSlow) {
            input.leftImpulse *= 5;
            input.forwardImpulse *= 5;
        }
        // Hiding in Rock: inside the earth, jump rises and sneak sinks
        if (ModEffects.HIDING_IN_ROCK != null && this.hasEffect(ModEffects.HIDING_IN_ROCK) && HidingInRock.isEmbedded(this)) {
            double rise = input.jumping ? 0.3D : (input.shiftKeyDown ? -0.3D : 0.0D);
            this.setDeltaMovement(this.getDeltaMovement().x, rise, this.getDeltaMovement().z);
        }
        // Walking on walls with Chakra Control: needs the input of this tick, and runs before the player moves
        WallFrame.tick((LocalPlayer) (Object) this, input);
    }
}
