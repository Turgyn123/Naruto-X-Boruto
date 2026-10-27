package net.narutoxboruto.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Earth Release: Iron Skin, while it is on. It marks the body that is drawn in hardened earth (see IronSkinLayer); the toughness comes from Resistance II. */
public class IronSkinEffect extends MobEffect {

    public IronSkinEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x96704A);
    }

    public static MobEffect create() {
        return new IronSkinEffect();
    }
}
