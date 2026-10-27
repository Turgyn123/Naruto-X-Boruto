package net.narutoxboruto.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Earth Release: Hiding in Rock, while it is on. The movement through the earth itself is in MixinPlayer and MixinLocalPlayer. */
public class HidingInRockEffect extends MobEffect {

    public HidingInRockEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x6B4A3A);
    }
}
