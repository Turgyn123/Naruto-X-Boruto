package net.narutoxboruto.items.jutsus;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.effect.ModEffects;
import net.narutoxboruto.main.platform.Services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Earth Release: Hiding in Rock. Rank C, 30 chakra and 15 chakra per second while it is on, up to 60 meters from where the
 * caster slipped into the earth. The caster sinks into the earth, becomes invisible and can move freely through earth, at their
 * own speed (jump to rise, sneak to sink). Out of the earth they fall again. Use it again to come out on the surface. Requires
 * Earth release affinity.
 */
public class HidingInRock extends AbstractJutsuItem {

    public static final int UPKEEP_COST = 15;
    public static final int UPKEEP_INTERVAL = 20;
    private static final int EFFECT_TICKS = UPKEEP_INTERVAL * 3;
    public static final double RANGE = 60.0D;
    /** Where each player slipped into the earth (only the server touches it). */
    private static final Map<UUID, Vec3> ENTRY = new HashMap<>();

    public HidingInRock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Hiding in Rock";
    }

    @Override
    public String getRequiredRelease() {
        return "earth";
    }

    @Override
    public int getChakraCost() {
        return 30;
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 3;
    }

    @Override
    protected boolean executeJutsu(ServerPlayer player, Level level) {
        if (player.hasEffect(ModEffects.HIDING_IN_ROCK)) {
            // Already on: this use comes out of the earth (and the cost is given back)
            leave(player);
            return false;
        }
        if (!isOnEarth(player) && !isEmbedded(player)) {
            player.displayClientMessage(Component.literal("There is no earth here to hide in"), true);
            return false;
        }
        applyEffects(player);
        ENTRY.put(player.getUUID(), player.position());
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 1.4F, 0.6F);
        return true;
    }

    private static void applyEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(ModEffects.HIDING_IN_ROCK, EFFECT_TICKS, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, EFFECT_TICKS, 0, false, false, false));
    }

    /** Every second for every player: while hiding it costs chakra, and it ends when the chakra runs out or the caster is too far. */
    public static void tickUpkeep(ServerPlayer player) {
        if (ModEffects.HIDING_IN_ROCK == null || !player.hasEffect(ModEffects.HIDING_IN_ROCK)) {
            ENTRY.remove(player.getUUID());
            return;
        }
        Chakra chakra = Services.PLATFORM.getChakra(player);
        if (chakra.getValue() < UPKEEP_COST) {
            player.displayClientMessage(Component.translatable("msg.no_chakra"), true);
            leave(player);
            return;
        }
        Vec3 entry = ENTRY.computeIfAbsent(player.getUUID(), id -> player.position());
        if (player.position().distanceToSqr(entry) > RANGE * RANGE) {
            player.displayClientMessage(Component.literal("Too far from where you went into the earth"), true);
            leave(player);
            return;
        }
        chakra.subValue(UPKEEP_COST, player);
        applyEffects(player);
    }

    /** Comes out of the earth: the effects end, and if the caster is inside earth they are put on the surface above. */
    public static void leave(ServerPlayer player) {
        player.removeEffect(ModEffects.HIDING_IN_ROCK);
        player.removeEffect(MobEffects.INVISIBILITY);
        ENTRY.remove(player.getUUID());
        Level level = player.level();
        BlockPos pos = player.blockPosition();
        for (int up = 0; up < 120; up++) {
            BlockPos feet = pos.above(up);
            if (feet.getY() >= level.getMaxBuildHeight() - 1) break;
            if (level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                    && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()) {
                if (up > 0) player.teleportTo(player.getX(), feet.getY(), player.getZ());
                break;
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 1.2F, 0.8F);
    }

    /** Whether a block is earth the caster can hide in: soil, sand, clay and mud, and the stones of the ground. */
    public static boolean isEarth(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getCollisionShape(level, pos).isEmpty()) return false;
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.TERRACOTTA)
                || state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER)
                || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    /** The feet or the head of the player are in earth. */
    public static boolean isEmbedded(Player player) {
        Level level = player.level();
        return isEarth(level, player.blockPosition()) || isEarth(level, BlockPos.containing(player.getEyePosition()));
    }

    /** The player stands on earth. */
    public static boolean isOnEarth(Player player) {
        return isEarth(player.level(), BlockPos.containing(player.getX(), player.getY() - 0.3D, player.getZ()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: C").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 60 meters").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Upkeep: " + UPKEEP_COST + " chakra per second").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("Sink into the earth, invisible, and move through it. Jump to rise, sneak to sink. Use again to come out.").withStyle(ChatFormatting.GRAY));
    }
}
