package net.narutoxboruto.items.jutsus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.entities.effects.WeaponEffectEntity;
import net.narutoxboruto.main.platform.Services;

import java.util.List;

/**
 * Fire Release: Great Fire Annihilation. Rank A, range 50 meters. The caster expels a massive wave of fire that
 * spreads out in front of them like a wall of flame, covering a wide area and burning everything in it. It costs 70
 * chakra to start, and as long as the right mouse button is held it keeps going, for 8 chakra a second, turning with
 * the caster. When it is let go of, or the chakra runs out, the fire dies down and the jutsu rests for 6 seconds. It
 * is drawn with pixel textures of the mod, there are no particles. Requires Fire release affinity.
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

    /** The rest comes when it is let go of, see {@link #releaseUsing}. */
    @Override
    public int getCooldownTicks() {
        return 0;
    }

    /** How much chakra it takes every second to keep it going, and how long it rests after it stops. */
    private static final int SUSTAIN_COST_PER_SECOND = 8;
    private static final int REST_TICKS = 120;

    /** Starts the jutsu, and keeps it going for as long as the button is held. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (!level.isClientSide() && result.getResult().consumesAction()) {
            player.startUsingItem(hand);
        }
        return result;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    /** Keeping it going costs chakra every second, and it stops when there is none left. */
    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(user instanceof ServerPlayer player)) return;
        int used = getUseDuration(stack, user) - remainingUseDuration;
        if (used > 0 && used % 20 == 0) {
            Chakra chakra = Services.PLATFORM.getChakra(player);
            if (chakra.getValue() < SUSTAIN_COST_PER_SECOND) {
                player.stopUsingItem();
                return;
            }
            chakra.subValue(SUSTAIN_COST_PER_SECOND, player);
        }
    }

    /** Let go of: the fire dies down and the jutsu rests. */
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof ServerPlayer player) {
            player.getCooldowns().addCooldown(this, REST_TICKS);
        }
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
        tooltip.add(Component.literal("Hold to keep it going: 8 chakra per second").withStyle(ChatFormatting.GRAY));
    }
}
