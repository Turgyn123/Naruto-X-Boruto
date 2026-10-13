package net.narutoxboruto.items.swords;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.narutoxboruto.entities.effects.WeaponEffectEntity;

import java.util.List;

/**
 * Nuibari, the needle sword of Kushimaru Kuriarare, that stitches its enemies with chakra thread.
 *
 * With the ability switched on (the Special Action key), a hit ties the target to the sword with a thread of
 * chakra. The target cannot move for up to 30 seconds. The thread breaks, and the target is free, when someone hits
 * the wielder, when the wielder dies or goes away, or when the target is pulled more than 24 blocks away. Whichever
 * way it ends, the ability rests for a minute. Needs the same Kenjutsu as the other swordsmen's blades.
 *
 * The thread itself (and holding the target) is the {@link WeaponEffectEntity} of kind THREAD.
 */
public class Nuibari extends AbstractAbilitySword {

    public Nuibari(Properties pProperties) {
        super(SwordCustomTiers.NUIBARI, pProperties);
        // The base class puts the sword on this cooldown after every use of the ability: the 30 seconds the thread
        // holds and then the minute of rest. The thread shortens it to the minute when it ends.
        this.cooldown = WeaponEffectEntity.THREAD_TICKS + WeaponEffectEntity.THREAD_COOLDOWN_TICKS;
    }

    @Override
    public int getChakraCost() {
        return 3;
    }

    @Override
    protected void doSpecialAbility(LivingEntity pTarget, ServerPlayer serverPlayer) {
        if (!(serverPlayer.level() instanceof ServerLevel level)) return;

        WeaponEffectEntity.spawnThread(level, serverPlayer, pTarget, this);

        serverPlayer.displayClientMessage(Component.translatable("nuibari.bound").withStyle(ChatFormatting.DARK_RED), true);
        level.playSound(null, pTarget.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.0F, 0.7F);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("nuibari.tip_bind").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("nuibari.tip_break").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("nuibari.tip_cooldown").withStyle(ChatFormatting.GRAY));
    }
}
