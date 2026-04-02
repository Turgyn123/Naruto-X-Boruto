package net.narutoxboruto.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.capabilities.PlayerCapData;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.info.Affiliation;
import net.narutoxboruto.capabilities.info.Clan;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.capabilities.info.Rank;
import net.narutoxboruto.items.ModItems;
import net.narutoxboruto.util.ModUtil;

import static net.narutoxboruto.util.ModUtil.*;

public class FabricEvents {

    public static void register() {
        // First join detection - check on tick for new players
        // Uses <= 1 because END_SERVER_TICK fires after doTick() increments PLAY_TIME from 0 to 1
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                if (ModUtil.getPlayerStatistics(serverPlayer, Stats.PLAY_TIME) <= 1) {
                    PlayerCapData data = PlayerDataManager.get(serverPlayer);
                    Clan clan = data.getClan();
                    // Guard: skip if clan was already assigned (prevents double-init)
                    if (!"clan".equals(clan.getValue())) continue;
                    Affiliation affiliation = data.getAffiliation();
                    Rank rank = data.getRank();

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
        });
    }
}
