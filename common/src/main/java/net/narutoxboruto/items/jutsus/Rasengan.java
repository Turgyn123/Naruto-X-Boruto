package net.narutoxboruto.items.jutsus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.narutoxboruto.entities.effects.WeaponEffectEntity;

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
    protected int sphereKind() {
        return WeaponEffectEntity.RASENGAN;
    }

    @Override
    protected WeaponEffectEntity spawnSphere(ServerLevel level, ServerPlayer player) {
        return WeaponEffectEntity.spawnRasengan(level, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: A").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Hold to charge, let go to strike").withStyle(ChatFormatting.GRAY));
    }
}
