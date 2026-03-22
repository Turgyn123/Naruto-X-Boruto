package net.narutoxboruto.items.misc;

import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import static net.narutoxboruto.util.ModUtil.*;

public class ClanReroll extends Item {

    public ClanReroll(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            ItemStack stack = serverPlayer.getItemInHand(pUsedHand);

            // Get the clan attachment
            var clanAttachment = Services.PLATFORM.getClan(serverPlayer);

            removeClanStatBonuses(serverPlayer);

            String currentClan = clanAttachment.getValue();
            String newClan = getWeightedRandomClan();

            // Ensure we get a different clan
            while (newClan.equals(currentClan)) {
                newClan = getWeightedRandomClan();
            }

            // Set the new clan value
            clanAttachment.setValue(newClan, serverPlayer);
            Component message = Component.translatable("clan_reroll.success",
                    Component.translatable("clan." + newClan));
            serverPlayer.displayClientMessage(message, false);

            giveClanStatBonuses(serverPlayer);

            // Ensure current chakra doesn't exceed new max chakra
            capChakraToMax(serverPlayer);

            // SYNC ALL STATS TO CLIENT
            syncAllStatsToClient(serverPlayer);

            this.consume(stack, serverPlayer);
            return InteractionResultHolder.success(stack);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    // Sync all stat changes to the client
    private void syncAllStatsToClient(ServerPlayer serverPlayer) {
        ModUtil.syncAllStatsToClient(serverPlayer);
    }

    public ItemStack consume(ItemStack stack, ServerPlayer serverPlayer) {
        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        serverPlayer.gameEvent(GameEvent.DRINK);
        return stack;
    }
}
