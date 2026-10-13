package net.narutoxboruto.items.swords;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.capabilities.info.MaxChakra;
import net.narutoxboruto.main.platform.Services;

import java.util.List;

/**
 * Samehada, the sword that eats chakra.
 *
 * <ul>
 *   <li><b>Every hit</b> feeds it: the sword takes in chakra, and from a player that is hit it takes some of theirs.</li>
 *   <li><b>R (Special Action)</b> stores your own chakra in the sword, {@link #STORE_STEP} at a time. If that is not
 *       possible (the sword is full, or you have no chakra) nothing changes and a message says why.</li>
 *   <li><b>Right click</b> gives the stored chakra back to you, as much as you are missing.</li>
 * </ul>
 * Samehada has no switch on and off like Shibuki and Kiba. The chakra is stored on each sword itself. Needs the same
 * Kenjutsu as the other swordsmen's blades for storing and giving back.
 */
public class Samehada extends AbstractAbilitySword {
    private static final String STORED_KEY = "samehada_chakra";
    public static final int MAX_SWORD_CHAKRA = 250;
    /** Chakra the sword takes in with a hit, and from a player that is hit. */
    private static final int HIT_GAIN = 3;
    private static final int HIT_DRAIN = 3;
    /** Chakra stored in the sword with one press of R. */
    private static final int STORE_STEP = 25;

    public Samehada(Properties pProperties) {
        super(SwordCustomTiers.SAMEHADA, pProperties);
    }

    @Override
    public int getChakraCost() {
        return 0;
    }

    // ---------------------------------------------------------------- the stored chakra

    public int getStored(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(STORED_KEY);
    }

    private void setStored(ItemStack stack, int value) {
        int clamped = Math.max(0, Math.min(MAX_SWORD_CHAKRA, value));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(STORED_KEY, clamped));
    }

    private boolean checkKenjutsu(ServerPlayer player) {
        if (meetsKenjutsuRequirement(player)) return true;
        player.displayClientMessage(Component.translatable("sword_ability.kenjutsu_required", this.getDescription(),
                getRequiredKenjutsu(), Services.PLATFORM.getKenjutsu(player).getValue()).withStyle(ChatFormatting.RED), false);
        return false;
    }

    // ---------------------------------------------------------------- R: store your chakra

    /** R stores chakra in the sword. There is no switching the ability on and off for Samehada. */
    @Override
    public void toggleAbility(Player pPlayer, ItemStack pStack) {
        if (pPlayer.level().isClientSide() || !(pPlayer instanceof ServerPlayer player)) return;
        if (!checkKenjutsu(player)) return;

        int stored = getStored(pStack);
        if (stored >= MAX_SWORD_CHAKRA) {
            player.displayClientMessage(Component.translatable("samehada.full").withStyle(ChatFormatting.RED), true);
            return;
        }
        Chakra chakra = Services.PLATFORM.getChakra(player);
        if (chakra.getValue() <= 0) {
            player.displayClientMessage(Component.translatable("samehada.no_own_chakra").withStyle(ChatFormatting.RED), true);
            return;
        }

        int amount = Math.min(STORE_STEP, Math.min(chakra.getValue(), MAX_SWORD_CHAKRA - stored));
        chakra.subValue(amount, player);
        setStored(pStack, stored + amount);
        player.displayClientMessage(Component.translatable("samehada.stored", amount, getStored(pStack)).withStyle(ChatFormatting.AQUA), true);
    }

    // ---------------------------------------------------------------- right click: take it back

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (pLevel.isClientSide() || !(pPlayer instanceof ServerPlayer player)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!checkKenjutsu(player)) return InteractionResultHolder.fail(stack);

        int stored = getStored(stack);
        if (stored <= 0) {
            player.displayClientMessage(Component.translatable("samehada.no_chakra").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        Chakra chakra = Services.PLATFORM.getChakra(player);
        MaxChakra maxChakra = Services.PLATFORM.getMaxChakra(player);
        int missing = maxChakra.getValue() - chakra.getValue();
        if (missing <= 0) {
            player.displayClientMessage(Component.translatable("samehada.chakra_full").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        int amount = Math.min(stored, missing);
        chakra.addValue(amount, player);
        setStored(stack, stored - amount);
        player.displayClientMessage(Component.translatable("samehada.chakra_amount", getStored(stack)), true);
        return InteractionResultHolder.consume(stack);
    }

    // ---------------------------------------------------------------- hits feed it

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        int gained = HIT_GAIN;
        if (pTarget instanceof ServerPlayer victim) {
            Chakra victimChakra = Services.PLATFORM.getChakra(victim);
            int drained = Math.min(victimChakra.getValue(), HIT_DRAIN);
            if (drained > 0) {
                victimChakra.subValue(drained, victim);
                gained += drained;
            }
        }
        setStored(pStack, getStored(pStack) + gained);

        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("samehada.chakra_amount", getStored(stack)).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("samehada.tip_hit").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("samehada.tip_store").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("samehada.tip_release").withStyle(ChatFormatting.GRAY));
    }
}
