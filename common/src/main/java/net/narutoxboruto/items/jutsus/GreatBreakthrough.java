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
import net.narutoxboruto.entities.effects.WeaponEffectEntity;

import java.util.List;

/**
 * Wind Release: Great Breakthrough. Rank C, 30 chakra, range 50 meters. The caster breathes out a blast of wind that
 * blows away whatever is in the cone in front of them: living things are thrown on and hurt, and soft blocks (leaves,
 * grass, flowers) are blown away. Requires Wind release affinity.
 */
public class GreatBreakthrough extends AbstractJutsuItem {

    public GreatBreakthrough(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Great Breakthrough";
    }

    @Override
    public String getRequiredRelease() {
        return "wind";
    }

    @Override
    public int getChakraCost() {
        return 30;
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 15; // 15 seconds
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        WeaponEffectEntity.spawnGreatBreakthrough(serverLevel, serverPlayer);
        // Short sounds only: a long one (like the elytra) goes on playing after the blast is gone
        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.WIND_CHARGE_THROW, SoundSource.PLAYERS, 1.8F, 0.6F);
        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.4F, 0.5F);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: C").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 50 meters").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("A blast of wind that blows away everything in front of you").withStyle(ChatFormatting.GRAY));
    }
}
