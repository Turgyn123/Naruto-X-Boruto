package net.narutoxboruto.items.jutsus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.effect.ModEffects;
import net.narutoxboruto.main.platform.Services;

import java.util.List;

/**
 * Earth Release: Iron Skin. Rank B, 40 chakra and 25 chakra per second while it is on, on the caster only. Earth chakra flows
 * through the body, which turns darker (an earthen skin of dirt is drawn over the player) and hardens: it gives Resistance II. Use
 * it again to stop. Requires Earth release affinity.
 */
public class IronSkin extends AbstractJutsuItem {

    public static final int UPKEEP_COST = 25;
    /** Upkeep is paid every second, and the effect is renewed each time, so it can never get stuck on a player. */
    public static final int UPKEEP_INTERVAL = 20;
    private static final int EFFECT_TICKS = UPKEEP_INTERVAL * 3;

    public IronSkin(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Iron Skin";
    }

    @Override
    public String getRequiredRelease() {
        return "earth";
    }

    @Override
    public int getChakraCost() {
        return 40;
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 3;
    }

    @Override
    protected boolean executeJutsu(ServerPlayer player, Level level) {
        if (player.hasEffect(ModEffects.IRON_SKIN)) {
            // Already on: this use turns it off (and the cost is given back)
            player.removeEffect(ModEffects.IRON_SKIN);
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            player.displayClientMessage(Component.literal("Iron Skin released"), true);
            return false;
        }
        applyEffects(player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 1.6F, 0.6F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 1.2F, 0.5F);
        return true;
    }

    /** Every second for every player: while Iron Skin is on it costs chakra, and it stops when the chakra runs out. */
    public static void tickUpkeep(ServerPlayer player) {
        if (ModEffects.IRON_SKIN == null || !player.hasEffect(ModEffects.IRON_SKIN)) return;
        Chakra chakra = Services.PLATFORM.getChakra(player);
        if (chakra.getValue() < UPKEEP_COST) {
            player.removeEffect(ModEffects.IRON_SKIN);
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            player.displayClientMessage(Component.translatable("msg.no_chakra"), true);
            return;
        }
        chakra.subValue(UPKEEP_COST, player);
        applyEffects(player);
    }

    /** The earthen skin, and Resistance II (without particles). */
    private static void applyEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(ModEffects.IRON_SKIN, EFFECT_TICKS, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, EFFECT_TICKS, 1, false, false, false));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: B").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: self").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Upkeep: " + UPKEEP_COST + " chakra per second").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("Your skin turns to hardened earth (Resistance II). Use again to stop.").withStyle(ChatFormatting.GRAY));
    }
}
