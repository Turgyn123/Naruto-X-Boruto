package net.narutoxboruto.items.swords;

import net.narutoxboruto.main.platform.Services;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.capabilities.info.ReleaseList;
import net.narutoxboruto.capabilities.info.KibaActive;
import net.narutoxboruto.entities.effects.LightningArcEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Kiba - The Lightning Blades (雷刀・牙)
 * One of the Seven Swords of the Mist.
 * 
 * Ability: When activated, the sword is imbued with lightning.
 * - Requires Lightning Release affinity to use
 * - Drains 5 chakra per second while active
 * - Each hit stuns the target for one second (it cannot move or hurt anyone) while the ability is active
 * - Right-click to toggle on/off
 *
 * Kiba is a pair of blades: while you hold it in your main hand a second Kiba is in your off hand. That copy is
 * locked, it cannot be moved or thrown, and it is gone as soon as the real Kiba is not in your main hand. Throwing the
 * real one away removes it too. It works the same way as the split Hiramekarei.
 */
public class Kiba extends SwordItem implements Vanishable {
    private static final int CHAKRA_DRAIN_PER_SECOND = 5;
    /** A hit with the lightning on stuns the target for one second: it cannot move and cannot hurt anyone. */
    private static final int STUN_DURATION_TICKS = 20;
    /** After a stun the target cannot be stunned again for this long, so a fast Kiba cannot keep it frozen. */
    private static final int STUN_COOLDOWN_TICKS = 40;
    /** Slowness this high stops all walking, and weakness this high takes away all of the target's damage. */
    private static final int STUN_SLOWNESS = 9;
    private static final int STUN_WEAKNESS = 9;
    /** When each target was last stunned, by the game time of its level. */
    private static final Map<UUID, Long> LAST_STUN = new HashMap<>();

    /** Marks the locked copy of the Kiba in the off hand. */
    private static final String OFFHAND_KEY = "kiba_offhand";
    /**
     * Saved on the blade itself while its lightning is on. The lightning is drawn from this and not from a flag of the
     * local player, so it shows on the blades of the player who turned it on, and on nobody else's.
     */
    private static final String LIGHTNING_KEY = "kiba_lightning";

    /** Whether the lightning is on for this blade, which is what the renderers draw. */
    public static boolean isLightningOn(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof Kiba
                && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(LIGHTNING_KEY);
    }

    private static void setLightning(ItemStack stack, boolean on) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(LIGHTNING_KEY, on));
    }

    public Kiba(Properties pProperties) {
        super(SwordCustomTiers.KIBA, pProperties);
    }

    // ---------------------------------------------------------------- the second blade

    /** Forge and NeoForge: only a different item or slot plays the re-equip animation, not the lightning turning on. */
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    /** Fabric's version of the same. */
    public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    /** The locked copy of the Kiba in the off hand. */
    public static boolean isOffhandBlade(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof Kiba
                && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(OFFHAND_KEY);
    }

    /** The real Kiba: not the copy. */
    private static boolean isRealKiba(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof Kiba && !isOffhandBlade(stack);
    }

    private static ItemStack makeOffhandBlade(ItemStack main) {
        ItemStack copy = main.copy();
        copy.setCount(1);
        CustomData.update(DataComponents.CUSTOM_DATA, copy, tag -> tag.putBoolean(OFFHAND_KEY, true));
        return copy;
    }

    /** Runs every tick for a Kiba in an inventory: the real one in the main hand, and the copy in the off hand. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) return;

        // The lightning is on for the blades in this player's hands while their ability is on, and for no other
        boolean held = stack == player.getMainHandItem() || (isOffhandBlade(stack) && stack == player.getOffhandItem());
        boolean on = held && Services.PLATFORM.getKibaActive(player).isActive();
        if (isLightningOn(stack) != on) {
            setLightning(stack, on);
        }

        if (isOffhandBlade(stack) || stack == player.getMainHandItem()) {
            tickTwinBlades(player);
        }
    }

    /**
     * Keeps the second Kiba in the off hand while the real one is in the main hand, and nowhere else. Swapping hands
     * puts them back, and a copy found in any other slot is removed.
     */
    public static void tickTwinBlades(ServerPlayer player) {
        ItemStack carried = player.containerMenu.getCarried();
        if (isOffhandBlade(carried)) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        // The swap hands key: swap them back
        if (isOffhandBlade(main) && isRealKiba(off)) {
            player.setItemInHand(InteractionHand.MAIN_HAND, off);
            player.setItemInHand(InteractionHand.OFF_HAND, main);
            ItemStack swapped = main;
            main = off;
            off = swapped;
        }

        // A copy in any other slot is not allowed
        for (int i = 0; i < 40; i++) {
            if (isOffhandBlade(player.getInventory().getItem(i))) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        boolean realInMainHand = isRealKiba(main);
        if (isOffhandBlade(off)) {
            if (!realInMainHand) {
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
        } else if (realInMainHand) {
            // Whatever was in the off hand goes to the inventory, or to the floor when it is full
            if (!off.isEmpty()) {
                ItemStack displaced = off.copy();
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                if (!player.getInventory().add(displaced)) {
                    player.drop(displaced, false);
                }
            }
            player.setItemInHand(InteractionHand.OFF_HAND, makeOffhandBlade(main));
        }
    }

    /**
     * Called when a stack is about to be dropped by a player. The copy is never dropped. When the real Kiba is thrown
     * away the copy in the off hand is gone as well.
     *
     * @return true when the drop must be cancelled
     */
    public static boolean onDrop(ServerPlayer player, ItemStack dropped) {
        if (isOffhandBlade(dropped)) {
            return true;
        }
        if (isRealKiba(dropped)) {
            // A Kiba that is thrown away has no lightning
            if (isLightningOn(dropped)) {
                setLightning(dropped, false);
            }
            if (isOffhandBlade(player.getOffhandItem())) {
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (isOffhandBlade(stack)) {
            return InteractionResultHolder.fail(stack);
        }
        
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            // Check if player has lightning release
            ReleaseList releaseList = Services.PLATFORM.getReleaseList(serverPlayer);
            List<String> releases = releaseList.getReleasesAsList();
            
            boolean hasLightning = releases.stream()
                    .anyMatch(r -> r.equalsIgnoreCase("lightning"));
            
            if (!hasLightning) {
                serverPlayer.displayClientMessage(
                        Component.translatable("msg.kiba.no_lightning_release"), true);
                return InteractionResultHolder.fail(stack);
            }
            
            // Check if player has chakra to activate
            Chakra chakra = Services.PLATFORM.getChakra(serverPlayer);
            KibaActive kibaActive = Services.PLATFORM.getKibaActive(serverPlayer);
            
            if (!kibaActive.isActive() && chakra.getValue() < CHAKRA_DRAIN_PER_SECOND) {
                serverPlayer.displayClientMessage(
                        Component.translatable("msg.no_chakra"), true);
                return InteractionResultHolder.fail(stack);
            }
            
            // Toggle the ability
            kibaActive.toggle(serverPlayer);
            setLightning(stack, kibaActive.isActive());
            
            // NOTE: Activation particles now handled by client-side burst system in MixinItemRenderer
            // which detects state change and renders intense lightning for 0.5 seconds
            
            String messageKey = kibaActive.isActive() ? "sword_ability.activate" : "sword_ability.deactivate";
            serverPlayer.displayClientMessage(
                    Component.translatable(messageKey, Component.translatable("item.narutoxboruto.kiba")), true);
        }
        
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        
        if (pAttacker instanceof ServerPlayer serverPlayer) {
            KibaActive kibaActive = Services.PLATFORM.getKibaActive(serverPlayer);
            
            if (kibaActive.isActive()) {
                // A short stun: for one second the target is frozen on the spot and cannot hurt anyone
                if (pTarget.level() instanceof ServerLevel stunLevel) {
                    stun(stunLevel, pTarget);
                }

                // Spawn lightning particles around the stunned target - electric shock effect
                if (serverPlayer.level() instanceof ServerLevel serverLevel) {
                    spawnElectricShockParticles(serverLevel, pTarget);
                }
            }
        }
        
        return true;
    }

    /** Stuns a target for a second, unless it was stunned a moment ago. */
    private static void stun(ServerLevel level, LivingEntity target) {
        long now = level.getGameTime();
        if (LAST_STUN.size() > 256) {
            LAST_STUN.values().removeIf(t -> now - t > STUN_DURATION_TICKS + STUN_COOLDOWN_TICKS);
        }
        Long last = LAST_STUN.get(target.getUUID());
        if (last != null && now >= last && now - last < STUN_DURATION_TICKS + STUN_COOLDOWN_TICKS) {
            return;
        }
        LAST_STUN.put(target.getUUID(), now);

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, STUN_DURATION_TICKS, STUN_SLOWNESS, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, STUN_DURATION_TICKS, STUN_WEAKNESS, false, false));
        // Stop it where it stands
        target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
        target.hurtMarked = true;
        if (target instanceof net.minecraft.world.entity.Mob mob) {
            mob.getNavigation().stop();
        }
        level.playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.4F, 1.8F);
    }

    /**
     * Called by StatEvents to drain chakra every second while Kiba is active.
     * Also handles automatic deactivation when chakra runs out.
     */
    public static void tickChakraDrain(ServerPlayer serverPlayer) {
        KibaActive kibaActive = Services.PLATFORM.getKibaActive(serverPlayer);
        
        if (kibaActive.isActive()) {
            Chakra chakra = Services.PLATFORM.getChakra(serverPlayer);
            
            // Check if player still has the sword equipped
            ItemStack mainHand = serverPlayer.getMainHandItem();
            ItemStack offHand = serverPlayer.getOffhandItem();
            boolean holdingKiba = mainHand.getItem() instanceof Kiba || offHand.getItem() instanceof Kiba;
            
            if (!holdingKiba) {
                // Auto-deactivate if sword is no longer held
                kibaActive.setActive(false, serverPlayer);
                return;
            }
            
            if (chakra.getValue() >= CHAKRA_DRAIN_PER_SECOND) {
                chakra.subValue(CHAKRA_DRAIN_PER_SECOND, serverPlayer);
            } else {
                // Not enough chakra - deactivate immediately
                kibaActive.setActive(false, serverPlayer);
                serverPlayer.displayClientMessage(
                        Component.translatable("msg.no_chakra"), true);
            }
        }
    }
    
    /**
     * Called by StatEvents every half second (10 ticks) to spawn visual lightning aura.
     * NOTE: Visual effects now handled entirely by client-side MixinItemRenderer
     * which renders lightning directly on the sword model with proper positioning.
     */
    public static void tickVisualEffects(ServerPlayer serverPlayer) {
        // Visual effects now handled client-side - see MixinItemRenderer
        // Keeping method signature for compatibility with StatEvents
    }

    public static int getChakraDrainPerSecond() {
        return CHAKRA_DRAIN_PER_SECOND;
    }

    // Lightning color for Kiba - bright electric blue/cyan (matching reference image)
    private static final int KIBA_LIGHTNING_COLOR = 0xE060FFFF; // Bright cyan-blue

    /**
     * Spawns lightning aura around the sword using the LightningArcEntity system.
     * Creates both arcs emanating from the blade AND an aura wrapping around it.
     */
    public static void spawnSwordArcParticles(ServerLevel serverLevel, ServerPlayer player, int arcCount) {
        // Get player facing direction (horizontal only)
        float yaw = player.getYRot() * ((float)Math.PI / 180F);
        
        // Right hand offset (sword held to the right of player)
        double rightX = -Math.cos(yaw) * 0.35;
        double rightZ = -Math.sin(yaw) * 0.35;
        
        // Sword base is at hand level (right side of player)
        double swordBaseX = player.getX() + rightX;
        double swordBaseY = player.getY() + 0.9; // Hand height
        double swordBaseZ = player.getZ() + rightZ;
        
        // Sword tip extends UPWARD (blade points up when held)
        // Slight forward tilt based on player look
        double swordTipX = swordBaseX + Math.sin(yaw) * 0.15; // Slight forward tilt
        double swordTipY = swordBaseY + 1.1; // Blade extends upward
        double swordTipZ = swordBaseZ - Math.cos(yaw) * 0.15;
        
        // Spawn small lightning sparks emanating FROM the blade
        for (int arc = 0; arc < arcCount; arc++) {
            // Random start point along the blade
            double t = Math.random();
            double startX = swordBaseX + (swordTipX - swordBaseX) * t;
            double startY = swordBaseY + (swordTipY - swordBaseY) * t;
            double startZ = swordBaseZ + (swordTipZ - swordBaseZ) * t;
            
            // Smaller arcs for subtle sparks
            double arcLength = 0.2 + Math.random() * 0.3;
            double arcAngle = Math.random() * Math.PI * 2;
            double arcPitch = (Math.random() - 0.3) * Math.PI * 0.5;
            
            double endX = startX + Math.cos(arcAngle) * Math.cos(arcPitch) * arcLength;
            double endY = startY + Math.sin(arcPitch) * arcLength;
            double endZ = startZ + Math.sin(arcAngle) * Math.cos(arcPitch) * arcLength;
            
            // Small sparks with moderate duration
            LightningArcEntity.spawnArcBetween(
                    serverLevel,
                    new Vec3(startX, startY, startZ),
                    new Vec3(endX, endY, endZ),
                    KIBA_LIGHTNING_COLOR,
                    8, // Longer duration for persistence
                    0.015f + (float)(Math.random() * 0.01f)
            );
        }
        
        // Spawn persistent aura arcs ALONG the blade (wrapping around the sword) - longer duration for glow effect
        spawnSwordAura(serverLevel, player, swordBaseX, swordBaseY, swordBaseZ, swordTipX, swordTipY, swordTipZ, 3);
    }
    
    /**
     * Spawn lightning aura that envelops and wraps around the sword blade.
     * Creates a coating effect with arcs running along the blade and wrapping around it.
     */
    private static void spawnSwordAura(ServerLevel serverLevel, ServerPlayer player, 
            double baseX, double baseY, double baseZ, double tipX, double tipY, double tipZ, int count) {
        
        // Blade direction vector
        double bladeX = tipX - baseX;
        double bladeY = tipY - baseY;
        double bladeZ = tipZ - baseZ;
        double bladeLength = Math.sqrt(bladeX * bladeX + bladeY * bladeY + bladeZ * bladeZ);
        
        // Normalize blade vector
        double bladeDirX = bladeX / bladeLength;
        double bladeDirY = bladeY / bladeLength;
        double bladeDirZ = bladeZ / bladeLength;
        
        // Get perpendicular vectors for wrapping around the blade
        Vec3 bladeDir = new Vec3(bladeDirX, bladeDirY, bladeDirZ);
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 perpA = bladeDir.cross(up).normalize();
        if (perpA.lengthSqr() < 0.01) {
            perpA = bladeDir.cross(new Vec3(1, 0, 0)).normalize();
        }
        Vec3 perpB = bladeDir.cross(perpA).normalize();
        
        double wrapRadius = 0.05; // Tight around the blade surface
        
        // === LONGITUDINAL ARCS - Run along the blade length ===
        for (int i = 0; i < count; i++) {
            // Start and end at different points along the blade
            double t1 = Math.random() * 0.4;
            double t2 = 0.6 + Math.random() * 0.4;
            
            // Random angle around the blade circumference
            double angle = Math.random() * Math.PI * 2;
            double offsetX = perpA.x * Math.cos(angle) * wrapRadius + perpB.x * Math.sin(angle) * wrapRadius;
            double offsetY = perpA.y * Math.cos(angle) * wrapRadius + perpB.y * Math.sin(angle) * wrapRadius;
            double offsetZ = perpA.z * Math.cos(angle) * wrapRadius + perpB.z * Math.sin(angle) * wrapRadius;
            
            double startX = baseX + bladeX * t1 + offsetX;
            double startY = baseY + bladeY * t1 + offsetY;
            double startZ = baseZ + bladeZ * t1 + offsetZ;
            
            double endX = baseX + bladeX * t2 + offsetX;
            double endY = baseY + bladeY * t2 + offsetY;
            double endZ = baseZ + bladeZ * t2 + offsetZ;
            
            LightningArcEntity.spawnArcBetween(
                    serverLevel,
                    new Vec3(startX, startY, startZ),
                    new Vec3(endX, endY, endZ),
                    KIBA_LIGHTNING_COLOR,
                    12, // Persistent glow
                    0.012f // Thin arcs for coating effect
            );
        }
        
        // === SPIRAL/WRAPPING ARCS - Wrap around the blade circumference ===
        for (int i = 0; i < count + 2; i++) {
            // Position along the blade
            double t = 0.1 + Math.random() * 0.8;
            
            // Start angle and end angle (partial wrap around blade)
            double startAngle = Math.random() * Math.PI * 2;
            double endAngle = startAngle + Math.PI * (0.5 + Math.random() * 1.0); // 90-270 degree wrap
            
            double startOffsetX = perpA.x * Math.cos(startAngle) * wrapRadius + perpB.x * Math.sin(startAngle) * wrapRadius;
            double startOffsetY = perpA.y * Math.cos(startAngle) * wrapRadius + perpB.y * Math.sin(startAngle) * wrapRadius;
            double startOffsetZ = perpA.z * Math.cos(startAngle) * wrapRadius + perpB.z * Math.sin(startAngle) * wrapRadius;
            
            double endOffsetX = perpA.x * Math.cos(endAngle) * wrapRadius + perpB.x * Math.sin(endAngle) * wrapRadius;
            double endOffsetY = perpA.y * Math.cos(endAngle) * wrapRadius + perpB.y * Math.sin(endAngle) * wrapRadius;
            double endOffsetZ = perpA.z * Math.cos(endAngle) * wrapRadius + perpB.z * Math.sin(endAngle) * wrapRadius;
            
            // Slight movement along blade as it wraps
            double tShift = 0.05 + Math.random() * 0.1;
            
            double startX = baseX + bladeX * t + startOffsetX;
            double startY = baseY + bladeY * t + startOffsetY;
            double startZ = baseZ + bladeZ * t + startOffsetZ;
            
            double endX = baseX + bladeX * (t + tShift) + endOffsetX;
            double endY = baseY + bladeY * (t + tShift) + endOffsetY;
            double endZ = baseZ + bladeZ * (t + tShift) + endOffsetZ;
            
            LightningArcEntity.spawnArcBetween(
                    serverLevel,
                    new Vec3(startX, startY, startZ),
                    new Vec3(endX, endY, endZ),
                    KIBA_LIGHTNING_COLOR,
                    10, // Slightly shorter for variety
                    0.010f // Very thin for wrapping effect
            );
        }
        
        // === TIP CORONA - Small arcs emanating from the sword tip ===
        for (int i = 0; i < 2; i++) {
            double angle = Math.random() * Math.PI * 2;
            double pitch = (Math.random() - 0.5) * Math.PI * 0.3;
            double length = 0.08 + Math.random() * 0.12;
            
            double endX = tipX + Math.cos(angle) * Math.cos(pitch) * length;
            double endY = tipY + Math.sin(pitch) * length + 0.02;
            double endZ = tipZ + Math.sin(angle) * Math.cos(pitch) * length;
            
            LightningArcEntity.spawnArcBetween(
                    serverLevel,
                    new Vec3(tipX, tipY, tipZ),
                    new Vec3(endX, endY, endZ),
                    KIBA_LIGHTNING_COLOR,
                    8,
                    0.015f
            );
        }
    }
    
    /**
     * Spawns electric shock effect on a stunned entity using lightning arc entities.
     * Creates lightning bolts that arc across the target's body.
     */
    public static void spawnElectricShockParticles(ServerLevel serverLevel, LivingEntity target) {
        double centerX = target.getX();
        double centerY = target.getY();
        double centerZ = target.getZ();
        double radius = target.getBbWidth() * 0.6;
        double height = target.getBbHeight();
        
        // Create lightning bolts arcing across the body
        int boltCount = 4;
        for (int i = 0; i < boltCount; i++) {
            // Random start point on body surface
            double startAngle = Math.random() * Math.PI * 2;
            double startHeight = Math.random() * height;
            double startX = centerX + Math.cos(startAngle) * radius;
            double startY = centerY + startHeight;
            double startZ = centerZ + Math.sin(startAngle) * radius;
            
            // End point on opposite side or different height
            double endAngle = startAngle + Math.PI * (0.5 + Math.random()); // Roughly opposite
            double endHeight = Math.random() * height;
            double endX = centerX + Math.cos(endAngle) * radius;
            double endY = centerY + endHeight;
            double endZ = centerZ + Math.sin(endAngle) * radius;
            
            // Spawn lightning arc entity
            LightningArcEntity.spawnArcBetween(
                    serverLevel,
                    new Vec3(startX, startY, startZ),
                    new Vec3(endX, endY, endZ),
                    KIBA_LIGHTNING_COLOR,
                    4, // Slightly longer duration for shock effect
                    0.015f
            );
        }
        
        // Add some vertical bolts running up/down
        for (int i = 0; i < 2; i++) {
            double angle = Math.random() * Math.PI * 2;
            double x = centerX + Math.cos(angle) * radius * 0.3;
            double z = centerZ + Math.sin(angle) * radius * 0.3;
            
            LightningArcEntity.spawnArcBetween(
                    serverLevel,
                    new Vec3(x, centerY, z),
                    new Vec3(x + (Math.random() - 0.5) * 0.3, centerY + height, z + (Math.random() - 0.5) * 0.3),
                    KIBA_LIGHTNING_COLOR,
                    4,
                    0.012f
            );
        }
    }
}
