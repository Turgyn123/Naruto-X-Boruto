package net.narutoxboruto.items.jutsus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.narutoxboruto.entities.jutsus.ChakraSphereEntity;

import java.util.List;

/**
 * Rasengan. Rank A, 50 chakra. A sphere of violently spinning chakra is formed in the hand while the button is held.
 * Let go and the caster dashes forward and drives it into the first thing in the way, which it hits hard and throws
 * away. It needs no nature release. It is drawn as a sphere with textures of the mod, there are no particles.
 */
public class Rasengan extends ChargedSphereJutsu {

    public Rasengan(Properties properties) {
        super(properties);
    }

    @Override
    public String getJutsuName() {
        return "Rasengan";
    }

    @Override
    public String getRequiredRelease() {
        return "";
    }

    @Override
    public int getChakraCost() {
        return 50;
    }

    @Override
    public int getCooldownTicks() {
        return 160; // 8 seconds
    }

    @Override
    public int sphereKind() {
        return ChakraSphereEntity.RASENGAN;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: A").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right click to form it, left click to strike").withStyle(ChatFormatting.GRAY));
    }
}
