package net.narutoxboruto.mixin;

import net.minecraft.client.Minecraft;
import net.narutoxboruto.items.jutsus.GolemTechnique;
import net.narutoxboruto.items.jutsus.WaterClone;
import net.narutoxboruto.main.platform.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A left click with the Water Clone or Golem Technique in hand tells the server to dismiss the caster's clones or golem. */
@Mixin(Minecraft.class)
public class MixinMinecraftClick {

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void narutoxboruto$dismissClones(CallbackInfoReturnable<Boolean> cir) {
        Minecraft minecraft = (Minecraft) (Object) this;
        if (minecraft.player != null && minecraft.screen == null && (minecraft.player.getMainHandItem().getItem() instanceof WaterClone
                || minecraft.player.getMainHandItem().getItem() instanceof GolemTechnique)) {
            Services.PLATFORM.sendEquipDojutsu("dismiss_clones", "");
        }
    }
}
