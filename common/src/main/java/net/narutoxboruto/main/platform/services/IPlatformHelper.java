package net.narutoxboruto.main.platform.services;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.info.*;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.capabilities.release.*;

public interface IPlatformHelper {

    String getPlatformName();
    boolean isModLoaded(String modId);
    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }

    //Capability
    Affiliation getAffiliation(Player player);
    Chakra getChakra(Player player);
    MaxChakra getMaxChakra(Player player);
    Clan getClan(Player player);
    Rank getRank(Player player);
    ReleaseList getReleaseList(Player player);
    ShinobiPoints getShinobiPoints(Player player);
    EarthList getEarthList(Player player);
    FireList getFireList(Player player);
    LightningList getLightningList(Player player);
    WaterList getWaterList(Player player);
    WindList getWindList(Player player);
    YangList getYangList(Player player);
    YinList getYinList(Player player);
    JutsuStorage getJutsuStorage(Player player);

    //Setter
    void setChakra(ServerPlayer player, Chakra chakra);
    void setMaxChakra(ServerPlayer player, MaxChakra maxChakra);
    void setJutsuStorage(ServerPlayer player, JutsuStorage storage);

    //Sync
    void syncAffiliation(ServerPlayer player, String value);
    void syncChakra(ServerPlayer player, int value);
    void syncMaxChakra(ServerPlayer player, int value);
    void syncClan(ServerPlayer player, String value);
    void syncRank(ServerPlayer player, String value);
    void syncReleaseList(ServerPlayer player, String value);
    void syncShinobiPoints(ServerPlayer player, int value);
    void syncEarthList(ServerPlayer player, String value);
    void syncFireList(ServerPlayer player, String value);
    void syncLightningList(ServerPlayer player, String value);
    void syncWaterList(ServerPlayer player, String value);
    void syncWindList(ServerPlayer player, String value);
    void syncYangList(ServerPlayer player, String value);
    void syncYinList(ServerPlayer player, String value);
    void syncJutsuStorage(ServerPlayer player, CompoundTag nbt);

}
