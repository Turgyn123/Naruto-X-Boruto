package net.narutoxboruto.items.swords;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.narutoxboruto.main.platform.Services;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.narutoxboruto.capabilities.info.Chakra;

public class AbstractAbilitySword extends SwordItem implements Vanishable {
    /**
     * Key used to store the toggle on the ItemStack itself. An Item object is a single shared
     * instance for the whole server, so state must live on the stack (one per player's sword),
     * not in a field on the item.
     */
    private static final String ACTIVE_KEY = "sword_ability_active";

    /** Kenjutsu a player needs before they can use the ability of a Seven Swordsmen sword. */
    private static final int REQUIRED_KENJUTSU = 350;

    protected int cooldown;

    public AbstractAbilitySword(Tier tier, Item.Properties pProperties) {
        super(tier, pProperties);
        this.cooldown = (int) (20 / (4 + tier.getSpeed()));
    }

    public int getChakraCost() {
        return 1;
    }

    /** Kenjutsu level needed to use this sword's ability. Override in a sword to change it. */
    public int getRequiredKenjutsu() {
        return REQUIRED_KENJUTSU;
    }

    /** The player's current Kenjutsu value. This is the only place that touches the Kenjutsu capability. */
    private int getPlayerKenjutsu(ServerPlayer pPlayer) {
        return Services.PLATFORM.getKenjutsu(pPlayer).getValue();
    }

    public boolean meetsKenjutsuRequirement(ServerPlayer pPlayer) {
        return getPlayerKenjutsu(pPlayer) >= getRequiredKenjutsu();
    }

    /** Tells the player in chat (not the action bar) that they are not skilled enough yet. */
    private void sendRequirementMessage(ServerPlayer pPlayer) {
        pPlayer.displayClientMessage(
                Component.translatable("sword_ability.kenjutsu_required",
                                this.getDescription(), getRequiredKenjutsu(), getPlayerKenjutsu(pPlayer))
                        .withStyle(ChatFormatting.RED),
                false);
    }

    /** Whether the ability is switched on for this specific sword. */
    public boolean isActive(ItemStack pStack) {
        return pStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(ACTIVE_KEY);
    }

    protected void setActive(ItemStack pStack, boolean pActive) {
        CustomData.update(DataComponents.CUSTOM_DATA, pStack, tag -> tag.putBoolean(ACTIVE_KEY, pActive));
    }

    /**
     * Toggles the sword the player is holding (main hand first, then off hand).
     * Existing callers of toggleAbility(Player) keep working unchanged.
     */
    public void toggleAbility(Player pPlayer) {
        ItemStack stack = pPlayer.getMainHandItem();
        if (stack.getItem() != this) {
            stack = pPlayer.getOffhandItem();
            if (stack.getItem() != this) {
                return;
            }
        }
        toggleAbility(pPlayer, stack);
    }

    public void toggleAbility(Player pPlayer, ItemStack pStack) {
        if (pPlayer.level().isClientSide || !(pPlayer instanceof ServerPlayer serverPlayer)) {
            return;
        }

        boolean nowActive = !isActive(pStack);

        if (nowActive && !meetsKenjutsuRequirement(serverPlayer)) {
            sendRequirementMessage(serverPlayer);
            return;
        }

        setActive(pStack, nowActive);
        String s = nowActive ? "activate" : "deactivate";
        serverPlayer.displayClientMessage(Component.translatable("sword_ability." + s, this.getDescription()), true);
    }

    protected void doSpecialAbility(LivingEntity pTarget, ServerPlayer serverPlayer) {
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        boolean active = isActive(pStack);

        if (active && pAttacker instanceof ServerPlayer serverPlayer && !meetsKenjutsuRequirement(serverPlayer)) {
            setActive(pStack, false);
            active = false;
            sendRequirementMessage(serverPlayer);
        }

        pStack.hurtAndBreak(1, pAttacker, LivingEntity.getSlotForHand(pAttacker.getUsedItemHand()));
        if (pAttacker instanceof ServerPlayer serverPlayer && active && !serverPlayer.level().isClientSide()) {
            ItemCooldowns cooldowns = serverPlayer.getCooldowns();
            Chakra chakra = Services.PLATFORM.getChakra(serverPlayer);
            if (!cooldowns.isOnCooldown(pStack.getItem()) && chakra.getValue() >= getChakraCost()) {
                // Consume chakra FIRST, then execute ability
                chakra.subValue(getChakraCost(), serverPlayer);
                doSpecialAbility(pTarget, serverPlayer);
                // Only add cooldown if ability was successfully used
                cooldowns.addCooldown(this, cooldown);
            }
        }
        return true;
    }
}
