package net.narutoxboruto.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.items.swords.Hiramekarei;
import net.narutoxboruto.items.swords.Kiba;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class MixinSlot {

    @Shadow
    public abstract ItemStack getItem();

    /** The Hiramekarei or Kiba copy in the off hand is locked: it cannot be picked up, moved or thrown from its slot. */
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void narutoxboruto$lockOffhandBlade(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (Hiramekarei.isOffhandBlade(this.getItem()) || Kiba.isOffhandBlade(this.getItem())) {
            cir.setReturnValue(false);
        }
    }
}
