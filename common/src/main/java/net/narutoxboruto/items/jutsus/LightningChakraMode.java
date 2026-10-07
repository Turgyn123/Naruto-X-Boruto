package net.narutoxboruto.items.jutsus;

import net.narutoxboruto.capabilities.stats.Speed;
import net.narutoxboruto.main.platform.Services;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.capabilities.info.ReleaseList;
import net.narutoxboruto.capabilities.info.LightningChakraModeActive;
import net.narutoxboruto.entities.effects.LightningArcEntity;
import net.narutoxboruto.util.ModUtil;

import java.util.List;

/**
 * Lightning Chakra Mode - A powerful lightning release jutsu that envelops
 * the user in lightning chakra, granting enhanced speed and strength.
 * 
 * Effects when active:
 * - Strength II
 * - Speed III  
 * - Lightning particle effects around the player
 * 
 * Costs:
 * - 15 chakra for initial activation
 * - 5 chakra every 5 seconds while active
 * 
 * Toggle: Right-click to activate/deactivate
 * Auto-deactivates when:
 * - Player runs out of chakra
 * - Item is removed from hotbar
 */
public class LightningChakraMode extends Item {

    private static final int ACTIVATION_COST = 15;
    private static final int DRAIN_COST = 5;
    private static final int DRAIN_INTERVAL_SECONDS = 5;

    private static final int TOGGLE_COOLDOWN_TICKS = 15;

    /**
     * Effects are re-applied on every drain tick, so they only need to outlast one drain interval with some margin.
     * Using a finite duration (instead of infinite) means a buff can never get stuck on a player if the mode state is
     * ever lost (death, relog, crash): it simply runs out. 20 s also keeps the HUD icon from blinking, which vanilla
     * does once an effect has 10 s or less left.
     */
    private static final int EFFECT_DURATION_TICKS = (DRAIN_INTERVAL_SECONDS + 15) * 20;
    private static final int STRENGTH_AMPLIFIER = 1; // Strength II (0-indexed)
    private static final int SPEED_AMPLIFIER = 2;    // Speed III (0-indexed)

    public LightningChakraMode(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.consume(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        // Check if player has lightning release
        ReleaseList releaseList = Services.PLATFORM.getReleaseList(serverPlayer);
        List<String> releases = releaseList.getReleasesAsList();

        boolean hasLightning = releases.stream()
                .anyMatch(r -> r.equalsIgnoreCase("lightning"));

        if (!hasLightning) {
            ModUtil.displayColoredMessage(serverPlayer, "msg.no_release", ChatFormatting.RED);
            return InteractionResultHolder.fail(stack);
        }

        LightningChakraModeActive modeActive = Services.PLATFORM.getLightningChakraModeActive(serverPlayer);
        Chakra chakra = Services.PLATFORM.getChakra(serverPlayer);

        if (!modeActive.isActive()) {
            // Activating - check initial chakra cost
            if (chakra.getValue() < ACTIVATION_COST) {
                ModUtil.displayColoredMessage(serverPlayer, "msg.no_chakra", ChatFormatting.RED);
                return InteractionResultHolder.fail(stack);
            }

            chakra.subValue(ACTIVATION_COST, serverPlayer);
            modeActive.setActive(true, serverPlayer);
            applyEffects(serverPlayer);

            // No sound effect - owner will provide custom sounds later
            // The activation flare is drawn client-side by CloakLightningRenderer (burst).

            serverPlayer.displayClientMessage(
                    Component.translatable("jutsu.activate", Component.translatable("item.narutoxboruto.lightning_chakra_mode")), true);
        } else {
            modeActive.setActive(false, serverPlayer);
            removeOwnedEffects(serverPlayer);

            // No sound effect - owner will provide custom sounds later

            serverPlayer.displayClientMessage(
                    Component.translatable("jutsu.deactivate", Component.translatable("item.narutoxboruto.lightning_chakra_mode")), true);
        }

        serverPlayer.getCooldowns().addCooldown(this, TOGGLE_COOLDOWN_TICKS);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * Apply Strength II and Speed III. Potion particles are hidden because the cloak renderer draws the lightning.
     */
    public static void applyEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_BOOST,
                EFFECT_DURATION_TICKS,
                STRENGTH_AMPLIFIER,
                false, // not ambient
                false, // hide particles
                true   // show icon
        ));

        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED,
                EFFECT_DURATION_TICKS,
                SPEED_AMPLIFIER,
                false,
                false,
                true
        ));
    }

    /**
     * Remove only the effects this jutsu applied. A Strength/Speed potion the player drank themselves is left alone
     * (ours are the ones with hidden particles and our exact amplifier).
     */
    private static void removeOwnedEffects(ServerPlayer player) {
        MobEffectInstance strength = player.getEffect(MobEffects.DAMAGE_BOOST);
        if (strength != null && strength.getAmplifier() == STRENGTH_AMPLIFIER && !strength.isVisible()) {
            player.removeEffect(MobEffects.DAMAGE_BOOST);
        }

        // (stat-based speed is an attribute modifier, unaffected by removeEffect)
        MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
        if (speed != null && speed.getAmplifier() == SPEED_AMPLIFIER && !speed.isVisible()) {
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
        }
    }

    /**
     * Called by StatEvents every DRAIN_INTERVAL_SECONDS to drain chakra while the mode is active.
     * Also handles auto-deactivation and effect refresh.
     */
    public static void tickChakraDrain(ServerPlayer serverPlayer) {
        LightningChakraModeActive modeActive = Services.PLATFORM.getLightningChakraModeActive(serverPlayer);

        if (modeActive.isActive()) {
            boolean hasJutsuInInventory = false;
            for (int i = 0; i < serverPlayer.getInventory().getContainerSize(); i++) {
                ItemStack stack = serverPlayer.getInventory().getItem(i);
                if (stack.getItem() instanceof LightningChakraMode) {
                    hasJutsuInInventory = true;
                    break;
                }
            }

            if (!hasJutsuInInventory) {
                deactivate(serverPlayer);
                return;
            }

            Chakra chakra = Services.PLATFORM.getChakra(serverPlayer);

            if (chakra.getValue() >= DRAIN_COST) {
                chakra.subValue(DRAIN_COST, serverPlayer);
                applyEffects(serverPlayer); // refresh
            } else {
                deactivate(serverPlayer);
                serverPlayer.displayClientMessage(Component.translatable("msg.no_chakra"), true);
            }
        }
    }

    /**
     * Called by StatEvents. Visuals are handled entirely client-side (CloakLightningRenderer);
     * the method is kept so existing callers keep compiling.
     */
    public static void tickVisualEffects(ServerPlayer serverPlayer) {
    }

    private static void deactivate(ServerPlayer serverPlayer) {
        LightningChakraModeActive modeActive = Services.PLATFORM.getLightningChakraModeActive(serverPlayer);
        modeActive.setActive(false, serverPlayer);
        removeOwnedEffects(serverPlayer);

        // No sound effect - owner will provide custom sounds later
    }

    public static int getDrainCost() {
        return DRAIN_COST;
    }

    public static int getDrainIntervalSeconds() {
        return DRAIN_INTERVAL_SECONDS;
    }

    /**
     * Check if Lightning Chakra Mode is active for a player.
     * Used by StatEvents to avoid overwriting speed effects.
     */
    public static boolean isActive(ServerPlayer player) {
        return Services.PLATFORM.getLightningChakraModeActive(player).isActive();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Show enchantment glint when mode is active (client-side visual indicator)
        return net.narutoxboruto.client.PlayerData.isLightningChakraModeActive();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.lightning_chakra_mode.desc").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.lightning_chakra_mode.effects").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("  ").append(Component.translatable("tooltip.lightning_chakra_mode.strength")).withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.literal("  ").append(Component.translatable("tooltip.lightning_chakra_mode.speed")).withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("tooltip.chakra_cost", ACTIVATION_COST).withStyle(ChatFormatting.BLUE));
        tooltipComponents.add(Component.translatable("tooltip.chakra_drain", DRAIN_COST, DRAIN_INTERVAL_SECONDS).withStyle(ChatFormatting.DARK_BLUE));
        tooltipComponents.add(Component.translatable("tooltip.requires_release", "Lightning").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    private static final int LIGHTNING_AURA_COLOR = 0xE040E0FF;

    /** @deprecated the cloak is rendered client-side by CloakLightningRenderer. */
    @Deprecated
    public static void spawnLightningParticles(ServerLevel level, ServerPlayer player, int count) {
        LightningArcEntity.spawnArcsAroundEntity(level, player, count, LIGHTNING_AURA_COLOR, 6);
        spawnVerticalAuraArcs(level, player, 3);
    }

    private static void spawnVerticalAuraArcs(ServerLevel level, ServerPlayer player, int count) {
        double radius = player.getBbWidth() * 0.4;
        double height = player.getBbHeight();

        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;

            double startY = player.getY() + Math.random() * 0.3;
            double endY = player.getY() + height * (0.7 + Math.random() * 0.3);
            double endX = x + (Math.random() - 0.5) * 0.3;
            double endZ = z + (Math.random() - 0.5) * 0.3;

            LightningArcEntity.spawnArcBetween(
                    level,
                    new Vec3(x, startY, z),
                    new Vec3(endX, endY, endZ),
                    LIGHTNING_AURA_COLOR,
                    5,
                    0.02f
            );
        }
    }
}
