package net.narutoxboruto.items.swords;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;

public class Nuibari extends SwordItem {

    public Nuibari(Properties pProperties) {
        super(SwordCustomTiers.NUIBARI, pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            doSpecialAbility(pPlayer, serverPlayer);
        }
        return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
    }

    protected void doSpecialAbility(LivingEntity pTarget, ServerPlayer serverPlayer) {
    }
}