package net.narutoxboruto.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ChakraControlEffect extends MobEffect {
    protected ChakraControlEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x93D1F5);
    }

    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
