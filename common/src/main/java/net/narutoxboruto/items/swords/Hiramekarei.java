package net.narutoxboruto.items.swords;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.entities.effects.WeaponEffectEntity;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

import java.util.List;

/**
 * Hiramekarei, the twin blades of Mangetsu Hozuki, the swordsman of the Mist who stored chakra in his swords.
 *
 * <ul>
 *   <li><b>Passive, stored chakra:</b> like Samehada, the blades take in chakra with every hit, a little of your
 *       own and, from a player you hit, some of theirs, up to 150. Hold right click while sneaking to pour more of
 *       your chakra in. The stored chakra is shown in the tooltip.</li>
 *   <li><b>Ability 1, both blades</b>: the blades start combined, two handed (model {@code hiramekarei_two}), and
 *       every hit is a double strike. The Special Action key splits them: one blade (model
 *       {@code hiramekarei_one}) in the main hand and a copy of it in the off hand. The off hand blade is locked, it
 *       cannot be moved or dropped, and it is gone while the main blade is not in the main hand. If the main blade is
 *       thrown away the off hand blade vanishes and the thrown blade is combined again.</li>
 *   <li><b>Ability 2, chakra coating</b> (right click): the whole blade is covered in chakra and glows. While it
 *       is on, every hit uses stored chakra and deals more damage, with a crescent of chakra flying off the
 *       blade. It goes out when the stored chakra runs out, or with another right click.</li>
 * </ul>
 * Needs Kenjutsu like the other swordsmen's blades.
 */
public class Hiramekarei extends AbstractAbilitySword {

    private static final String STORED_KEY = "hiramekarei_chakra";
    private static final String COAT_KEY = "hiramekarei_coat";
    public static final int MAX_STORED = 150;
    /** Chakra the blades take in with a hit: from a player that is hit, and from the wielder. */
    private static final int ABSORB_FROM_TARGET = 4;
    private static final int ABSORB_FROM_WIELDER = 3;
    private static final int COAT_MINIMUM = 10;
    private static final int COAT_PER_HIT = 4;
    private static final float COAT_DAMAGE = 5.0F;
    private static final float SECOND_STRIKE = 0.6F;
    /** Chakra poured into the blades every few ticks while charging. */
    private static final int CHARGE_PER_STEP = 2;
    private static final int CHARGE_INTERVAL = 4;
    private static final int MAX_USE_TICKS = 72000;
    private static final int CRESCENT_COLOR = 0xB06FD8FF;
    /** Marks the locked copy of the blade that sits in the off hand while the blades are split. */
    private static final String OFFHAND_KEY = "hiramekarei_offhand";

    public Hiramekarei(Properties pProperties) {
        super(SwordCustomTiers.HIRAMEKAREI, pProperties);
    }

    @Override
    public int getChakraCost() {
        return 1;
    }

    // ---------------------------------------------------------------- the stored chakra and the coat

    public int getStored(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(STORED_KEY);
    }

    private void setStored(ItemStack stack, int value) {
        int clamped = Math.max(0, Math.min(MAX_STORED, value));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(STORED_KEY, clamped));
    }

    public boolean isCoated(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(COAT_KEY);
    }

    /** The coat is shown with the glow of an enchanted item. */
    private void setCoated(ItemStack stack, boolean coated) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(COAT_KEY, coated));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, coated ? Boolean.TRUE : null);
    }

    // ---------------------------------------------------------------- ability 1: both blades

    /**
     * The blades start combined, so "active" (both blades, double strike) is the default and the stored flag means
     * the opposite: one blade drawn. The Special Action key switches between the two.
     */
    @Override
    public boolean isActive(ItemStack pStack) {
        return !super.isActive(pStack);
    }

    /** The off hand blade is only a copy, it has no ability of its own. */
    @Override
    public void toggleAbility(Player pPlayer, ItemStack pStack) {
        if (isOffhandBlade(pStack)) return;
        super.toggleAbility(pPlayer, pStack);
    }

    /** Keeps the model in step with the state: 0 is the combined model, 1 the single blade. */
    @Override
    protected void setActive(ItemStack pStack, boolean pActive) {
        super.setActive(pStack, !pActive);
        pStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(pActive ? 0 : 1));
    }

    // ---------------------------------------------------------------- the split blades

    /** The locked copy of the blade in the off hand. */
    public static boolean isOffhandBlade(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof Hiramekarei
                && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(OFFHAND_KEY);
    }

    /** The real blade while the blades are split: one blade in the main hand, a copy in the off hand. */
    private static boolean isSplitBlade(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof Hiramekarei blade && !isOffhandBlade(stack)
                && !blade.isActive(stack);
    }

    private ItemStack makeOffhandBlade(ItemStack main) {
        ItemStack copy = main.copy();
        copy.setCount(1);
        CustomData.update(DataComponents.CUSTOM_DATA, copy, tag -> tag.putBoolean(OFFHAND_KEY, true));
        copy.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(1));
        return copy;
    }

    /**
     * Every tick, for every player: keeps the off hand blade there while the split blade is in the main hand, and
     * nowhere else. Swapping hands puts it back, and a copy found in any other slot is removed.
     */
    public static void tickSplitBlades(ServerPlayer player) {
        ItemStack carried = player.containerMenu.getCarried();
        if (isOffhandBlade(carried)) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        // The swap hands key: swap them back
        if (isOffhandBlade(main) && isSplitBlade(off)) {
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

        boolean splitInMainHand = isSplitBlade(main);
        if (isOffhandBlade(off)) {
            if (!splitInMainHand) {
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            } else {
                // The copy follows the real blade: stored chakra and the glow of the coat
                Hiramekarei blade = (Hiramekarei) main.getItem();
                if (blade.getStored(off) != blade.getStored(main)) {
                    blade.setStored(off, blade.getStored(main));
                }
                if (!java.util.Objects.equals(off.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE),
                        main.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE))) {
                    off.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, main.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE));
                }
            }
        } else if (splitInMainHand) {
            // Whatever was in the off hand goes to the inventory, or to the floor when it is full
            if (!off.isEmpty()) {
                ItemStack displaced = off.copy();
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                if (!player.getInventory().add(displaced)) {
                    player.drop(displaced, false);
                }
            }
            player.setItemInHand(InteractionHand.OFF_HAND, ((Hiramekarei) main.getItem()).makeOffhandBlade(main));
        }
    }

    /**
     * Called when a stack is about to be dropped by a player. The off hand blade is never dropped. When the real
     * split blade is thrown away the off hand blade is gone and the thrown blade is combined again.
     *
     * @return true when the drop must be cancelled
     */
    public static boolean onDrop(ServerPlayer player, ItemStack dropped) {
        if (isOffhandBlade(dropped)) {
            return true;
        }
        if (isSplitBlade(dropped)) {
            ((Hiramekarei) dropped.getItem()).setActive(dropped, true);
            if (isOffhandBlade(player.getOffhandItem())) {
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
        }
        return false;
    }

    /** Both blades drawn: the second blade strikes right after the first. */
    @Override
    protected void doSpecialAbility(LivingEntity pTarget, ServerPlayer serverPlayer) {
        float damage = (float) serverPlayer.getAttributeValue(Attributes.ATTACK_DAMAGE) * SECOND_STRIKE;
        pTarget.invulnerableTime = 0;
        pTarget.hurt(serverPlayer.damageSources().playerAttack(serverPlayer), damage);
    }

    // ---------------------------------------------------------------- ability 2: the chakra coat

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        // Like Samehada, the blades take in chakra with every hit: a little of the wielder's own, and more from a
        // player they hit
        if (attacker instanceof ServerPlayer wielder && getStored(stack) < MAX_STORED) {
            int gained = 0;
            if (target instanceof ServerPlayer victim) {
                Chakra victimChakra = Services.PLATFORM.getChakra(victim);
                int taken = Math.min(victimChakra.getValue(), ABSORB_FROM_TARGET);
                if (taken > 0) {
                    victimChakra.subValue(taken, victim);
                    gained += taken;
                }
            }
            Chakra ownChakra = Services.PLATFORM.getChakra(wielder);
            int given = Math.min(ownChakra.getValue(), ABSORB_FROM_WIELDER);
            if (given > 0) {
                ownChakra.subValue(given, wielder);
                gained += given;
            }
            if (gained > 0) {
                setStored(stack, getStored(stack) + gained);
                wielder.displayClientMessage(Component.translatable("hiramekarei.stored", getStored(stack), MAX_STORED).withStyle(ChatFormatting.AQUA), true);
            }
        }

        if (isCoated(stack) && attacker instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
            if (getStored(stack) < COAT_PER_HIT) {
                setCoated(stack, false);
                player.displayClientMessage(Component.translatable("hiramekarei.coat_off").withStyle(ChatFormatting.GRAY), true);
                return result;
            }
            setStored(stack, getStored(stack) - COAT_PER_HIT);

            target.invulnerableTime = 0;
            target.hurt(player.damageSources().playerAttack(player), COAT_DAMAGE);

            // A crescent of chakra comes off the blade, the way the swing goes
            Vec3 look = player.getLookAngle();
            Vec3 at = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            WeaponEffectEntity.spawnCrescent(level, at.subtract(look.scale(0.4D)), look, 0.8F, CRESCENT_COLOR, 6, 0.25D);
            level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.9F, 1.5F);
        }
        return result;
    }

    // ---------------------------------------------------------------- using it

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isOffhandBlade(stack)) {
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.consume(stack);
        }
        if (!meetsKenjutsuRequirement(serverPlayer)) {
            serverPlayer.displayClientMessage(Component.translatable("sword_ability.kenjutsu_required",
                    this.getDescription(), getRequiredKenjutsu(), Services.PLATFORM.getKenjutsu(serverPlayer).getValue()).withStyle(ChatFormatting.RED), false);
            return InteractionResultHolder.fail(stack);
        }

        // Sneaking: hold to pour your chakra into the blades
        if (serverPlayer.isShiftKeyDown()) {
            if (getStored(stack) >= MAX_STORED) {
                serverPlayer.displayClientMessage(Component.translatable("hiramekarei.full").withStyle(ChatFormatting.AQUA), true);
                return InteractionResultHolder.fail(stack);
            }
            serverPlayer.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        // Otherwise: the coat of chakra on, or off
        if (isCoated(stack)) {
            setCoated(stack, false);
            serverPlayer.displayClientMessage(Component.translatable("hiramekarei.coat_off").withStyle(ChatFormatting.GRAY), true);
        } else if (getStored(stack) >= COAT_MINIMUM) {
            setCoated(stack, true);
            serverPlayer.displayClientMessage(Component.translatable("hiramekarei.coat_on").withStyle(ChatFormatting.AQUA), true);
            level.playSound(null, serverPlayer.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6F, 1.8F);
        } else {
            serverPlayer.displayClientMessage(Component.translatable("hiramekarei.not_enough", COAT_MINIMUM).withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return MAX_USE_TICKS;
    }

    /** Charging: your chakra flows into the blades, a little every few ticks. */
    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(user instanceof ServerPlayer player)) return;

        int used = MAX_USE_TICKS - remainingUseDuration;
        if (used % CHARGE_INTERVAL != 0) return;

        Chakra chakra = Services.PLATFORM.getChakra(player);
        int stored = getStored(stack);
        if (stored >= MAX_STORED) {
            player.displayClientMessage(Component.translatable("hiramekarei.full").withStyle(ChatFormatting.AQUA), true);
            player.stopUsingItem();
            return;
        }
        if (chakra.getValue() < CHARGE_PER_STEP) {
            ModUtil.displayColoredMessage(player, "msg.no_chakra", ChatFormatting.RED);
            player.stopUsingItem();
            return;
        }

        int step = Math.min(CHARGE_PER_STEP, MAX_STORED - stored);
        chakra.subValue(step, player);
        setStored(stack, stored + step);
        player.displayClientMessage(Component.translatable("hiramekarei.stored", getStored(stack), MAX_STORED).withStyle(ChatFormatting.AQUA), true);
        if (used % 20 == 0) {
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5F, 0.8F + stored / (float) MAX_STORED);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("hiramekarei.stored", getStored(stack), MAX_STORED).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("hiramekarei.tip_store").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("hiramekarei.tip_both").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("hiramekarei.tip_coat").withStyle(ChatFormatting.GRAY));
    }
}
