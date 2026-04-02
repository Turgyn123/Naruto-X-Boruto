package net.narutoxboruto.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.capabilities.info.Affiliation;
import net.narutoxboruto.capabilities.info.Clan;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.capabilities.info.Rank;
import net.narutoxboruto.items.ModItems;
import net.narutoxboruto.util.ModUtil;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import static net.narutoxboruto.capabilities.NeoForgeCapabilities.*;
import static net.narutoxboruto.util.ModUtil.*;

public class Events {


    @SubscribeEvent
    public static void onPlayerFirstJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer serverPlayer
                && ModUtil.getPlayerStatistics(serverPlayer, Stats.PLAY_TIME) == 0) {

            Clan clan = serverPlayer.getData(CLAN);
            Affiliation affiliation = serverPlayer.getData(AFFILIATION);
            Rank rank = serverPlayer.getData(RANK);

            String randomClan = getWeightedRandomClan();
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
