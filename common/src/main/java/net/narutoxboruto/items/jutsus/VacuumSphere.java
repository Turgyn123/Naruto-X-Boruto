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
 * Wind Release: Vacuum Sphere. Rank B, 40 chakra, range 50 meters. The caster takes a deep breath and blows out 20 small
 * bullets of wind, spread over a wide cone so they are hard to avoid. They go through whatever they hit, hurting each
 * thing once, like bullets, and stop at blocks. Requires Wind release affinity.
 */
public class VacuumSphere extends AbstractJutsuItem {

    public VacuumSphere(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Vacuum Sphere";
    }

    @Override
    public String getRequiredRelease() {
        return "wind";
    }

    @Override
    public int getChakraCost() {
        return 40;
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 20; // 20 seconds
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        WeaponEffectEntity.spawnVacuumSphere(serverLevel, serverPlayer);
        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.4F, 0.7F);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: B").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 50 meters").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("20 bullets of wind that go through what they hit").withStyle(ChatFormatting.GRAY));
    }
}
