package net.narutoxboruto.main.platform.services;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.narutoxboruto.capabilities.climber.ClimberComponent;
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

    //Capability - Info
    Affiliation getAffiliation(Player player);
    Chakra getChakra(Player player);
    MaxChakra getMaxChakra(Player player);
    Clan getClan(Player player);
    Rank getRank(Player player);
    ReleaseList getReleaseList(Player player);
    ShinobiPoints getShinobiPoints(Player player);
    JutsuStorage getJutsuStorage(Player player);

    //Capability - Stats
    Genjutsu getGenjutsu(Player player);
    Kenjutsu getKenjutsu(Player player);
    Kinjutsu getKinjutsu(Player player);
    Medical getMedical(Player player);
    Ninjutsu getNinjutsu(Player player);
    Senjutsu getSenjutsu(Player player);
    Shurikenjutsu getShurikenjutsu(Player player);
    Speed getSpeed(Player player);
    Summoning getSummoning(Player player);
    Taijutsu getTaijutsu(Player player);

    //Capability - Modes
    ChakraControl getChakraControl(Player player);
    KibaActive getKibaActive(Player player);
    LightningChakraModeActive getLightningChakraModeActive(Player player);
    NarutoRun getNarutoRun(Player player);
    WallRunning getWallRunning(Player player);
    ClimberComponent getClimberComponent(Player player);

    //Capability - Release Lists
    EarthList getEarthList(Player player);
    FireList getFireList(Player player);
    LightningList getLightningList(Player player);
    WaterList getWaterList(Player player);
    WindList getWindList(Player player);
    YangList getYangList(Player player);
    YinList getYinList(Player player);

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
    void syncGenjutsu(ServerPlayer player, int value);
    void syncKenjutsu(ServerPlayer player, int value);
    void syncKinjutsu(ServerPlayer player, int value);
    void syncMedical(ServerPlayer player, int value);
    void syncNinjutsu(ServerPlayer player, int value);
    void syncSenjutsu(ServerPlayer player, int value);
    void syncShurikenjutsu(ServerPlayer player, int value);
    void syncSpeed(ServerPlayer player, int value);
    void syncSummoning(ServerPlayer player, int value);
    void syncTaijutsu(ServerPlayer player, int value);
    void syncChakraControl(ServerPlayer player, boolean value);
    void syncKibaActive(ServerPlayer player, boolean value);
    void syncLightningChakraModeActive(ServerPlayer player, boolean value);
    void syncNarutoRun(ServerPlayer player, boolean value);

    //Fluids
    default Block getStaticWaterBlock() { return Blocks.WATER; }
}
