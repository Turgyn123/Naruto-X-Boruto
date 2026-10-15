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
 * Fire Release: Flame Bullet. Rank D, 10 chakra, range 25 meters. The caster spits a small, fast bullet of flame that
 * flies straight, burns the first thing it hits and bursts. It is drawn with pixel textures of the mod, there are no
 * particles. Requires Fire release affinity.
 */
public class FlameBullet extends AbstractJutsuItem {

    public FlameBullet(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Flame Bullet";
    }

    @Override
    public String getRequiredRelease() {
        return "fire";
    }

    @Override
    public int getChakraCost() {
        return 10;
    }

    @Override
    public int getCooldownTicks() {
        return 20; // 1 second
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        WeaponEffectEntity.spawnFlameBullet(serverLevel, serverPlayer);
        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.4F);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: D").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 25 meters").withStyle(ChatFormatting.GRAY));
    }
}
