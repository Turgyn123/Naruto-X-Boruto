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
 * Fire Release: Great Fire Annihilation. Rank A, 70 chakra, range 50 meters. The caster expels a massive wave of fire
 * that spreads out in front of them like a wall of flame, covering a wide area and burning everything in it. It is
 * drawn with textures of the mod, there are no particles. Requires Fire release affinity.
 */
public class GreatFireAnnihilation extends AbstractJutsuItem {

    public GreatFireAnnihilation(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Fire Release: Great Fire Annihilation";
    }

    @Override
    public String getRequiredRelease() {
        return "fire";
    }

    @Override
    public int getChakraCost() {
        return 70;
    }

    @Override
    public int getCooldownTicks() {
        return 400; // 20 seconds
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        WeaponEffectEntity.spawnFireWave(serverLevel, serverPlayer);
        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 2.0F, 0.4F);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: A").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 50 meters").withStyle(ChatFormatting.GRAY));
    }
}
