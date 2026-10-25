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
 * Wind Release: Boruto Stream. Rank C, 30 chakra, range 20 meters. A shadow clone of the caster appears at their feet
 * and propels them forward, the way they look, 20 meters in half a second. It gives no speed boost, it only launches
 * the caster. Requires Wind release affinity.
 */
public class BorutoStream extends AbstractJutsuItem {

    public BorutoStream(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Boruto Stream";
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
        return 20 * 10; // 10 seconds
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        WeaponEffectEntity.spawnShadowClone(serverLevel, serverPlayer);
        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.8F);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: C").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 20 meters").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("A shadow clone launches you forward").withStyle(ChatFormatting.GRAY));
    }
}
