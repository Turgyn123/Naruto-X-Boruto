package net.narutoxboruto.items.swords;

import net.narutoxboruto.main.platform.Services;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EquipmentSlot;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.capabilities.info.MaxChakra;

public class Samehada extends AbstractAbilitySword {
    private static final int MAX_SWORD_CHAKRA = 250;
    private int chakraDiff;
    private int swordChakra;

    public Samehada(Properties pProperties) {
        super(SwordCustomTiers.SAMEHADA, pProperties);
    }

  //  @Override
  //  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
  //      components.add(Component.literal("Req: 300 Ken").withStyle(ChatFormatting.GOLD));
  //      super.appendHoverText(stack, context, components, flag);
  //  }

    @Override
    public int getChakraCost() {
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            doSpecialAbility(pPlayer, serverPlayer);
        }
        return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
    }

    public void abilityChakra(LivingEntity pPlayer) {
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            // Use attachment system instead of capabilities
            Chakra chakra = Services.PLATFORM.getChakra(serverPlayer);
            MaxChakra maxChakra = Services.PLATFORM.getMaxChakra(serverPlayer);

            this.chakraDiff = maxChakra.getValue() - chakra.getValue();
        }
    }

    @Override
    protected void doSpecialAbility(LivingEntity pTarget, ServerPlayer serverPlayer) {
        abilityChakra(pTarget);
        if (pTarget instanceof ServerPlayer && swordChakra > 0) {
            // Use attachment system instead of capabilities
            Chakra targetChakra = Services.PLATFORM.getChakra((ServerPlayer) pTarget);
            targetChakra.addValue(swordChakra, (ServerPlayer) pTarget);
            swordChakra = Math.max(swordChakra - this.chakraDiff, 0);
        }
        serverPlayer.sendSystemMessage(Component.translatable("samehada.chakra_amount", swordChakra));
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        if (pTarget instanceof ServerPlayer serverPlayer) {
            // Use attachment system instead of capabilities
            Chakra targetChakra = Services.PLATFORM.getChakra(serverPlayer);
            if (swordChakra >= MAX_SWORD_CHAKRA) {
                targetChakra.subValue(3, serverPlayer);
            }
            swordChakra = Math.min(swordChakra + 3, MAX_SWORD_CHAKRA);
        }
        swordChakra = Math.min(swordChakra + 3, MAX_SWORD_CHAKRA);

        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        return true;
    }
}