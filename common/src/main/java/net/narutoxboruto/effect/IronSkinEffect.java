package net.narutoxboruto.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.narutoxboruto.main.Main;

/**
 * Earth Release: Iron Skin, while it is active. The damage reduction itself (almost no damage from anything but lightning)
 * is in MixinPlayer; this gives the stronger hits and the knockback resistance of skin as hard as rock.
 */
public class IronSkinEffect extends MobEffect {

    public IronSkinEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x96704A);
    }

    /** The effect with its attribute changes: hits 60 percent harder and a hard to move body. */
    public static MobEffect create() {
        return new IronSkinEffect()
                .addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "iron_skin_strength"),
                        0.6, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "iron_skin_stability"),
                        0.6, AttributeModifier.Operation.ADD_VALUE);
    }
}
