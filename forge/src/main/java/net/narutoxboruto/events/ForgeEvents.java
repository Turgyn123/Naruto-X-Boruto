package net.narutoxboruto.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.capabilities.PlayerCapData;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.info.Affiliation;
import net.narutoxboruto.capabilities.info.Clan;
import net.narutoxboruto.capabilities.info.Rank;
import net.narutoxboruto.items.ModItems;
import net.narutoxboruto.util.ModUtil;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static net.narutoxboruto.util.ModUtil.*;

public class ForgeEvents {

    @SubscribeEvent
    public static void onPlayerFirstJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer serverPlayer
                && ModUtil.getPlayerStatistics(serverPlayer, Stats.PLAY_TIME) == 0) {

            PlayerCapData data = PlayerDataManager.get(serverPlayer);
            Clan clan = data.getClan();
            Affiliation affiliation = data.getAffiliation();
            Rank rank = data.getRank();

            String randomClan = getRandomIndex(CLAN_LIST);
            clan.setValue(randomClan, serverPlayer);
            affiliation.setValue(getRandomIndex(AFF_LIST));

            rank.setValue("student");

            clan.syncValue(serverPlayer);
            affiliation.syncValue(serverPlayer);
            rank.syncValue(serverPlayer);

            giveClanStatBonuses(serverPlayer);
            serverPlayer.addItem(new ItemStack(ModItems.CHAKRA_PAPER_ITEM));
        }
    }
}
