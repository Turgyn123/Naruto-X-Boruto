package net.narutoxboruto.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.capabilities.info.*;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.items.jutsus.AbstractJutsuItem;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.util.JutsuGrantHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;


public class AttachmentEvents {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            //INFO
            serverPlayer.getData(NeoForgeCapabilities.AFFILIATION).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.CLAN).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.CHAKRA).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.MAX_CHAKRA).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.SHINOBI_POINTS).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.RANK).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.RELEASE_LIST).syncValue(serverPlayer);

            //STATS
            serverPlayer.getData(NeoForgeCapabilities.GENJUTSU).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.KENJUTSU).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.KINJUTSU).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.MEDICAL).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.NINJUTSU).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.SENJUTSU).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.SHURIKENJUTSU).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.SPEED).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.SUMMONING).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.TAIJUTSU).syncValue(serverPlayer);
            
            // Dojutsu
            serverPlayer.getData(NeoForgeCapabilities.DOJUTSU).syncValue(serverPlayer);

            // Clean up any duplicate jutsus and sync jutsu storage
            JutsuGrantHelper.cleanupDuplicateJutsus(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.JUTSU_STORAGE).syncToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onJoinWorldSyncCap(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer serverPlayer) {

            //INFO
            serverPlayer.getData(NeoForgeCapabilities.AFFILIATION).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.CLAN).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.CHAKRA).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.MAX_CHAKRA).syncValue(serverPlayer);

            serverPlayer.getData(NeoForgeCapabilities.SHINOBI_POINTS).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.RANK).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.RELEASE_LIST).syncValue(serverPlayer);

            //STATS
            serverPlayer.getData(NeoForgeCapabilities.GENJUTSU).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.KENJUTSU).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.KINJUTSU).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.MEDICAL).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.NINJUTSU).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.SENJUTSU).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.SHURIKENJUTSU).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.SPEED).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.SUMMONING).syncValue(serverPlayer);
            serverPlayer.getData(NeoForgeCapabilities.TAIJUTSU).syncValue(serverPlayer);

            //JUTSU_LIST (commented out)
            // serverPlayer.getData(NeoForgeCapabilities.FIRELIST).syncValue(serverPlayer);
            // serverPlayer.getData(NeoForgeCapabilities.EARTHLIST).syncValue(serverPlayer);
            // serverPlayer.getData(NeoForgeCapabilities.WATERLIST).syncValue(serverPlayer);
            // serverPlayer.getData(NeoForgeCapabilities.WINDLIST).syncValue(serverPlayer);
            // serverPlayer.getData(NeoForgeCapabilities.LIGHTINGLIST).syncValue(serverPlayer);
            // serverPlayer.getData(NeoForgeCapabilities.YANGLIST).syncValue(serverPlayer);
            // serverPlayer.getData(NeoForgeCapabilities.YINLIST).syncValue(serverPlayer);
            
            // Dojutsu
            serverPlayer.getData(NeoForgeCapabilities.DOJUTSU).syncValue(serverPlayer);

            // Sync jutsu storage
            serverPlayer.getData(NeoForgeCapabilities.JUTSU_STORAGE).syncToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newPlayer
                && event.getOriginal() instanceof ServerPlayer original) {
            // No need to reviveCaps() with attachments

            //INFO
            newPlayer.setData(NeoForgeCapabilities.AFFILIATION, original.getData(NeoForgeCapabilities.AFFILIATION));
            newPlayer.setData(NeoForgeCapabilities.CLAN, original.getData(NeoForgeCapabilities.CLAN));
            newPlayer.setData(NeoForgeCapabilities.MAX_CHAKRA, original.getData(NeoForgeCapabilities.MAX_CHAKRA));
            newPlayer.setData(NeoForgeCapabilities.RELEASE_LIST, original.getData(NeoForgeCapabilities.RELEASE_LIST));
            newPlayer.setData(NeoForgeCapabilities.SHINOBI_POINTS, original.getData(NeoForgeCapabilities.SHINOBI_POINTS));

            newPlayer.setData(NeoForgeCapabilities.CHAKRA, original.getData(NeoForgeCapabilities.CHAKRA));
            newPlayer.getData(NeoForgeCapabilities.CHAKRA).reset(newPlayer);

            //STATS
            newPlayer.setData(NeoForgeCapabilities.RANK, original.getData(NeoForgeCapabilities.RANK));
            newPlayer.setData(NeoForgeCapabilities.TAIJUTSU, original.getData(NeoForgeCapabilities.TAIJUTSU));
            newPlayer.setData(NeoForgeCapabilities.SUMMONING, original.getData(NeoForgeCapabilities.SUMMONING));
            newPlayer.setData(NeoForgeCapabilities.GENJUTSU, original.getData(NeoForgeCapabilities.GENJUTSU));
            newPlayer.setData(NeoForgeCapabilities.KENJUTSU, original.getData(NeoForgeCapabilities.KENJUTSU));
            newPlayer.setData(NeoForgeCapabilities.KINJUTSU, original.getData(NeoForgeCapabilities.KINJUTSU));
            newPlayer.setData(NeoForgeCapabilities.MEDICAL, original.getData(NeoForgeCapabilities.MEDICAL));
            newPlayer.setData(NeoForgeCapabilities.SPEED, original.getData(NeoForgeCapabilities.SPEED));
            newPlayer.setData(NeoForgeCapabilities.SHURIKENJUTSU, original.getData(NeoForgeCapabilities.SHURIKENJUTSU));
            newPlayer.setData(NeoForgeCapabilities.NINJUTSU, original.getData(NeoForgeCapabilities.NINJUTSU));
            newPlayer.setData(NeoForgeCapabilities.SENJUTSU, original.getData(NeoForgeCapabilities.SENJUTSU));
            
            // Dojutsu
            newPlayer.setData(NeoForgeCapabilities.DOJUTSU, original.getData(NeoForgeCapabilities.DOJUTSU));

            // Preserve Jutsu Storage across death
            newPlayer.setData(NeoForgeCapabilities.JUTSU_STORAGE, original.getData(NeoForgeCapabilities.JUTSU_STORAGE));
            
            // Preserve jutsu items from inventory - they should NEVER drop on death
            if (event.isWasDeath()) {
                preserveJutsuItemsOnDeath(original, newPlayer);
            }
        }
    }
    
    /**
     * Preserve jutsu items from the original player's inventory to the new player.
     * Jutsu items are permanently bound and should never be lost.
     */
    private static void preserveJutsuItemsOnDeath(ServerPlayer original, ServerPlayer newPlayer) {
        Inventory originalInv = original.getInventory();
        List<ItemStack> jutsuItems = new ArrayList<>();
        
        // Collect all jutsu items from original inventory
        for (int i = 0; i < originalInv.getContainerSize(); i++) {
            ItemStack stack = originalInv.getItem(i);
            if (stack.getItem() instanceof AbstractJutsuItem) {
                jutsuItems.add(stack.copy());
            }
        }
        
        // Add them to new player's inventory
        for (ItemStack jutsu : jutsuItems) {
            if (!newPlayer.getInventory().add(jutsu)) {
                // If inventory is full, put back in jutsu storage
                JutsuStorage storage = newPlayer.getData(NeoForgeCapabilities.JUTSU_STORAGE);
                storage.addJutsu(jutsu);
            }
        }
    }

    // Add this method to sync all stats to the client
    public static void syncAllStatsToClient(ServerPlayer player) {
        // Sync each individual stat attachment
        player.getData(NeoForgeCapabilities.NINJUTSU).syncValue(player);
        player.getData(NeoForgeCapabilities.SHURIKENJUTSU).syncValue(player);
        player.getData(NeoForgeCapabilities.KINJUTSU).syncValue(player);
        player.getData(NeoForgeCapabilities.SUMMONING).syncValue(player);
        player.getData(NeoForgeCapabilities.MEDICAL).syncValue(player);
        player.getData(NeoForgeCapabilities.KENJUTSU).syncValue(player);
        player.getData(NeoForgeCapabilities.TAIJUTSU).syncValue(player);
        player.getData(NeoForgeCapabilities.SENJUTSU).syncValue(player);
        player.getData(NeoForgeCapabilities.SPEED).syncValue(player);
        player.getData(NeoForgeCapabilities.GENJUTSU).syncValue(player);

        // Sync clan as well
        player.getData(NeoForgeCapabilities.CLAN).syncValue(player);
        player.getData(NeoForgeCapabilities.SHINOBI_POINTS).syncValue(player);
    }

    @SubscribeEvent
    public static void onReplenishChakra(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && serverPlayer.isSleepingLongEnough()) {
            Chakra chakra = serverPlayer.getData(NeoForgeCapabilities.CHAKRA.get());
            chakra.replenish(serverPlayer);
        }
    }
}
