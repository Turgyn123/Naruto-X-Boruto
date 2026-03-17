package net.narutoxboruto.items.swords;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;

public class Kubikiribocho extends SwordItem {

    public Kubikiribocho(Properties pProperties) {
        super(SwordCustomTiers.KUBIKIRIBOCHO, pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // First, call the parent method to apply normal sword damage
        boolean result = super.hurtEnemy(stack, target, attacker);

        // Check if the target was killed by this attack and attacker is a player
        if (target.getHealth() <= 0 && attacker instanceof Player player) {
            healSwordOnKill(stack, player);
        }

        return result;
    }

    private void healSwordOnKill(ItemStack stack, Player player) {
        if (!stack.isDamaged()) return;

        Level level = player.level();
        if (!level.isClientSide()) {
            // Configurable repair values
            int minRepair = 1;
            int maxRepair = 5;
            int repairAmount = minRepair + level.random.nextInt(maxRepair - minRepair + 1);

            int currentDamage = stack.getDamageValue();
            int newDamage = Math.max(0, currentDamage - repairAmount);

            stack.setDamageValue(newDamage);
        }
    }
}
