package net.narutoxboruto.items.jutsus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.main.platform.Services;

import java.util.List;

/**
 * Earth Release: Iron Skin. Rank B, 40 chakra and 25 chakra per second while it is on, on the caster only. Earth chakra flows
 * through the body: the skin of the player turns into the dirt texture of the game, and it hardens, which gives Resistance II and
 * a body that is hard to knock back. Use it again to stop. Requires Earth release affinity.
 *
 * <p>Whether it is on is a small bonus of armor with its own name. Armor is sent to the clients, which is how they know to draw
 * the earthen skin (see MixinPlayerRenderer), and it is not saved, so it can never stay on a player after a relog or a death.</p>
 */
public class IronSkin extends AbstractJutsuItem {

    public static final int UPKEEP_COST = 25;
    /** Upkeep is paid every second, and Resistance is renewed each time, so it can never get stuck on a player. */
    public static final int UPKEEP_INTERVAL = 20;
    private static final int EFFECT_TICKS = UPKEEP_INTERVAL * 3;
    private static final ResourceLocation MARKER = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "iron_skin_marker");
    private static final ResourceLocation STABILITY = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "iron_skin_stability");

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

    /** Whether the player has Iron Skin on. Works on the client too. */
    public static boolean isActive(Player player) {
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        return armor != null && armor.hasModifier(MARKER);
    }

    @Override
    protected boolean executeJutsu(ServerPlayer player, Level level) {
        if (isActive(player)) {
            // Already on: this use turns it off (and the cost is given back)
            deactivate(player);
            player.displayClientMessage(Component.literal("Iron Skin released"), true);
            return false;
        }
        player.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(MARKER, 2.0D, AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addTransientModifier(new AttributeModifier(STABILITY, 0.6D, AttributeModifier.Operation.ADD_VALUE));
        applyResistance(player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 1.6F, 0.6F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 1.2F, 0.5F);
        return true;
    }

    private static void deactivate(ServerPlayer player) {
        player.getAttribute(Attributes.ARMOR).removeModifier(MARKER);
        player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifier(STABILITY);
        player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
    }

    /** Resistance II, without particles. */
    private static void applyResistance(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, EFFECT_TICKS, 1, false, false, false));
    }

    /** Every second for every player: while Iron Skin is on it costs chakra, and it stops when the chakra runs out. */
    public static void tickUpkeep(ServerPlayer player) {
        if (!isActive(player)) return;
        Chakra chakra = Services.PLATFORM.getChakra(player);
        if (chakra.getValue() < UPKEEP_COST) {
            deactivate(player);
            player.displayClientMessage(Component.translatable("msg.no_chakra"), true);
            return;
        }
        chakra.subValue(UPKEEP_COST, player);
        applyResistance(player);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: B").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: self").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Upkeep: " + UPKEEP_COST + " chakra per second").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("Your skin turns to dirt (Resistance II, hard to knock back). Use again to stop.").withStyle(ChatFormatting.GRAY));
    }
}
