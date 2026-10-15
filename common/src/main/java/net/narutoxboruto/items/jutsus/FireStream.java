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
 * Fire Release Stream. Rank A, 50 chakra, range 70 meters. The caster gathers chakra in their lungs and blows out a
 * considerable stream of flames that travels across far distances. It is blown for 2.5 seconds in the way the caster
 * looks, and everything in it is burnt. Requires Fire release affinity.
 */
public class FireStream extends AbstractJutsuItem {

    public FireStream(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Fire Release Stream";
    }

    @Override
    public String getRequiredRelease() {
        return "fire";
    }

    @Override
    public int getChakraCost() {
        return 50;
    }

    @Override
    public int getCooldownTicks() {
        return 200; // 10 seconds
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        WeaponEffectEntity.spawnFlameStream(serverLevel, serverPlayer);
        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5F, 0.5F);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: A").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 70 meters").withStyle(ChatFormatting.GRAY));
    }
}
