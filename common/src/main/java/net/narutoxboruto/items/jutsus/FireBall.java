package net.narutoxboruto.items.jutsus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.narutoxboruto.entities.jutsus.FireBallEntity;

import java.util.List;

/**
 * Fire Release: Great Fireball Technique. Rank C, 30 chakra, range 30 meters. Launches a massive orb of flame that
 * flies straight, engulfs whatever it hits and leaves a crater in the ground. Requires Fire release affinity.
 */
public class FireBall extends AbstractJutsuItem {

    public FireBall(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Great Fireball Technique";
    }

    @Override
    public String getRequiredRelease() {
        return "fire";
    }

    @Override
    public int getChakraCost() {
        return 30;
    }

    @Override
    public int getCooldownTicks() {
        return 100; // 5 seconds cooldown
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel)) {
            return false;
        }

        // Create and spawn the fireball entity
        FireBallEntity fireBall = new FireBallEntity(level, serverPlayer);
        level.addFreshEntity(fireBall);

        // Play casting sound
        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5F, 0.6F);

        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: C").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 30 meters").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Handseals: Snake, Ram, Monkey, Boar, Horse, Tiger").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("A massive orb of roaring flame. It engulfs its target and leaves a crater.")
                .withStyle(ChatFormatting.GRAY));
    }
}
