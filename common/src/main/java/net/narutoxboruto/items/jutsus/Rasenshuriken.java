package net.narutoxboruto.items.jutsus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.narutoxboruto.entities.jutsus.ChakraSphereEntity;

import java.util.List;

/**
 * Wind Release: Rasenshuriken. Rank S, 100 chakra, range 45 meters. A Rasengan with the blades of the wind spinning
 * round it is charged in the hand while the button is held. Let go and it is thrown. Where it hits it grows into a
 * huge sphere of blades that cuts everything in it for 2.5 seconds, pulls it in and slows it, and bursts at the end.
 * Requires Wind release affinity. It is drawn with textures of the mod, there are no particles.
 */
public class Rasenshuriken extends ChargedSphereJutsu {

    public Rasenshuriken(Properties properties) {
        super(properties);
    }

    @Override
    public String getJutsuName() {
        return "Rasenshuriken";
    }

    @Override
    public String getRequiredRelease() {
        return "wind";
    }

    @Override
    public int getChakraCost() {
        return 100;
    }

    @Override
    public int getCooldownTicks() {
        return 800; // 40 seconds
    }

    @Override
    protected int sphereKind() {
        return ChakraSphereEntity.RASENSHURIKEN;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: S").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 45 meters").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Hold to charge, let go to throw").withStyle(ChatFormatting.GRAY));
    }
}
