package net.narutoxboruto.main.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.info.*;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.capabilities.release.*;
import net.narutoxboruto.main.platform.services.IPlatformHelper;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.networking.info.*;
import net.narutoxboruto.networking.jutsu.SyncJutsuStorage;
import net.narutoxboruto.networking.release.*;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    public static ItemStackHandler toItemStackHandler(JutsuStorage storage) {
        ItemStackHandler handler = new ItemStackHandler(JutsuStorage.STORAGE_SIZE);
        for (int i = 0; i < JutsuStorage.STORAGE_SIZE; i++) {
            handler.setStackInSlot(i, storage.getItem(i).copy());
        }
        return handler;
    }

    //Get data
    @Override public Affiliation getAffiliation(Player player) {return player.getData(NeoForgeCapabilities.AFFILIATION);}
    @Override public Chakra getChakra(Player player) { return player.getData(NeoForgeCapabilities.CHAKRA); }
    @Override public MaxChakra getMaxChakra(Player player) { return player.getData(NeoForgeCapabilities.MAX_CHAKRA); }
    @Override public Clan getClan(Player player) { return player.getData(NeoForgeCapabilities.CLAN); }
    @Override public Rank getRank(Player player) { return player.getData(NeoForgeCapabilities.RANK); }
    @Override public ReleaseList getReleaseList(Player player) { return player.getData(NeoForgeCapabilities.RELEASE_LIST); }
    @Override public ShinobiPoints getShinobiPoints(Player player) { return player.getData(NeoForgeCapabilities.SHINOBI_POINTS); }
    @Override public EarthList getEarthList(Player player) { return player.getData(NeoForgeCapabilities.EARTHLIST); }
    @Override public FireList getFireList(Player player) { return player.getData(NeoForgeCapabilities.FIRELIST); }
    @Override public LightningList getLightningList(Player player) { return player.getData(NeoForgeCapabilities.LIGHTINGLIST); }
    @Override public WaterList getWaterList(Player player) { return player.getData(NeoForgeCapabilities.WATERLIST); }
    @Override public WindList getWindList(Player player) { return player.getData(NeoForgeCapabilities.WINDLIST); }
    @Override public YangList getYangList(Player player) { return player.getData(NeoForgeCapabilities.YANGLIST); }
    @Override public YinList getYinList(Player player) { return player.getData(NeoForgeCapabilities.YINLIST); }
    @Override public JutsuStorage getJutsuStorage(Player player) {return player.getData(NeoForgeCapabilities.JUTSU_STORAGE);}

    //Set data
    @Override public void setChakra(ServerPlayer player, Chakra chakra) { player.setData(NeoForgeCapabilities.CHAKRA, chakra); }
    @Override public void setMaxChakra(ServerPlayer player, MaxChakra maxChakra) { player.setData(NeoForgeCapabilities.MAX_CHAKRA, maxChakra); }
    @Override public void setJutsuStorage(ServerPlayer player, JutsuStorage storage) {player.setData(NeoForgeCapabilities.JUTSU_STORAGE, storage);}

    //Sync data
    @Override public void syncAffiliation(ServerPlayer player, String value) {PacketDistributor.sendToPlayer(player, new SyncAffiliation(value));}
    @Override public void syncChakra(ServerPlayer player, int value) { PacketDistributor.sendToPlayer(player, new SyncChakra(value)); }
    @Override public void syncMaxChakra(ServerPlayer player, int value) { PacketDistributor.sendToPlayer(player, new SyncMaxChakra(value)); }
    @Override public void syncClan(ServerPlayer player, String value) { PacketDistributor.sendToPlayer(player, new SyncClan(value)); }
    @Override public void syncRank(ServerPlayer player, String value) { PacketDistributor.sendToPlayer(player, new SyncRank(value)); }
    @Override public void syncReleaseList(ServerPlayer player, String value) { PacketDistributor.sendToPlayer(player, new SyncReleaseList(value)); }
    @Override public void syncShinobiPoints(ServerPlayer player, int value) { PacketDistributor.sendToPlayer(player, new SyncShinobiPoints(value)); }
    @Override public void syncEarthList(ServerPlayer player, String value) { PacketDistributor.sendToPlayer(player, new SyncEarthList(value)); }
    @Override public void syncFireList(ServerPlayer player, String value) { PacketDistributor.sendToPlayer(player, new SyncFireList(value)); }
    @Override public void syncLightningList(ServerPlayer player, String value) { PacketDistributor.sendToPlayer(player, new SyncLightningList(value)); }
    @Override public void syncWaterList(ServerPlayer player, String value) { PacketDistributor.sendToPlayer(player, new SyncWaterList(value)); }
    @Override public void syncWindList(ServerPlayer player, String value) { PacketDistributor.sendToPlayer(player, new SyncWindList(value)); }
    @Override public void syncYangList(ServerPlayer player, String value) { PacketDistributor.sendToPlayer(player, new SyncYangList(value)); }
    @Override public void syncYinList(ServerPlayer player, String value) { PacketDistributor.sendToPlayer(player, new SyncYinList(value)); }
    @Override public void syncJutsuStorage(ServerPlayer player, CompoundTag nbt) {PacketDistributor.sendToPlayer(player, new SyncJutsuStorage(nbt));}
}