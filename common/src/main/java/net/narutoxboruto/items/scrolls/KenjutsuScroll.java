package net.narutoxboruto.items.scrolls;

import net.narutoxboruto.main.platform.Services;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.narutoxboruto.capabilities.stats.Kenjutsu;

public class KenjutsuScroll extends Item {

    public KenjutsuScroll(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        if (!pLevel.isClientSide() && pPlayer instanceof ServerPlayer serverPlayer) {
            Kenjutsu kenjutsu = Services.PLATFORM.getKenjutsu(serverPlayer);
            kenjutsu.incrementValue(10, serverPlayer);
            Services.PLATFORM.getShinobiPoints(serverPlayer).incrementValue(10, serverPlayer);

            // Consume the item
            if (!pPlayer.getAbilities().instabuild) {
                stack.shrink(1);
            }

            // Trigger advancement and stats
            pPlayer.awardStat(Stats.ITEM_USED.get(this));
            pPlayer.gameEvent(GameEvent.ITEM_INTERACT_FINISH);

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }
}
