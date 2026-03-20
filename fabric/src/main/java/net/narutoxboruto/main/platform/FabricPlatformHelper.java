package net.narutoxboruto.main.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.climber.ClimberComponent;
import net.narutoxboruto.capabilities.info.*;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.capabilities.release.*;
import net.narutoxboruto.capabilities.stats.*;
import net.narutoxboruto.main.platform.services.IPlatformHelper;
import net.narutoxboruto.networking.FabricPacketHandler;
import net.narutoxboruto.networking.SyncBoolData;
import net.narutoxboruto.networking.SyncIntData;
import net.narutoxboruto.networking.SyncNbtData;
import net.narutoxboruto.networking.SyncStringData;
import net.minecraft.world.level.block.Block;
import net.fabricmc.loader.api.FabricLoader;
import net.narutoxboruto.fluids.FabricBlocks;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    // Get data
    @Override public Affiliation getAffiliation(Player player) { return PlayerDataManager.get(player).getAffiliation(); }
    @Override public Chakra getChakra(Player player) { return PlayerDataManager.get(player).getChakra(); }
    @Override public MaxChakra getMaxChakra(Player player) { return PlayerDataManager.get(player).getMaxChakra(); }
    @Override public Clan getClan(Player player) { return PlayerDataManager.get(player).getClan(); }
    @Override public Rank getRank(Player player) { return PlayerDataManager.get(player).getRank(); }
    @Override public ReleaseList getReleaseList(Player player) { return PlayerDataManager.get(player).getReleaseList(); }
    @Override public ShinobiPoints getShinobiPoints(Player player) { return PlayerDataManager.get(player).getShinobiPoints(); }
    @Override public EarthList getEarthList(Player player) { return PlayerDataManager.get(player).getEarthList(); }
    @Override public FireList getFireList(Player player) { return PlayerDataManager.get(player).getFireList(); }
    @Override public LightningList getLightningList(Player player) { return PlayerDataManager.get(player).getLightningList(); }
    @Override public WaterList getWaterList(Player player) { return PlayerDataManager.get(player).getWaterList(); }
    @Override public WindList getWindList(Player player) { return PlayerDataManager.get(player).getWindList(); }
    @Override public YangList getYangList(Player player) { return PlayerDataManager.get(player).getYangList(); }
    @Override public YinList getYinList(Player player) { return PlayerDataManager.get(player).getYinList(); }
    @Override public JutsuStorage getJutsuStorage(Player player) { return PlayerDataManager.get(player).getJutsuStorage(); }

    // Get stats
    @Override public Genjutsu getGenjutsu(Player player) { return PlayerDataManager.get(player).getGenjutsu(); }
    @Override public Kenjutsu getKenjutsu(Player player) { return PlayerDataManager.get(player).getKenjutsu(); }
    @Override public Kinjutsu getKinjutsu(Player player) { return PlayerDataManager.get(player).getKinjutsu(); }
    @Override public Medical getMedical(Player player) { return PlayerDataManager.get(player).getMedical(); }
    @Override public Ninjutsu getNinjutsu(Player player) { return PlayerDataManager.get(player).getNinjutsu(); }
    @Override public Senjutsu getSenjutsu(Player player) { return PlayerDataManager.get(player).getSenjutsu(); }
    @Override public Shurikenjutsu getShurikenjutsu(Player player) { return PlayerDataManager.get(player).getShurikenjutsu(); }
    @Override public Speed getSpeed(Player player) { return PlayerDataManager.get(player).getSpeed(); }
    @Override public Summoning getSummoning(Player player) { return PlayerDataManager.get(player).getSummoning(); }
    @Override public Taijutsu getTaijutsu(Player player) { return PlayerDataManager.get(player).getTaijutsu(); }

    // Get modes
    @Override public ChakraControl getChakraControl(Player player) { return PlayerDataManager.get(player).getChakraControl(); }
    @Override public KibaActive getKibaActive(Player player) { return PlayerDataManager.get(player).getKibaActive(); }
    @Override public LightningChakraModeActive getLightningChakraModeActive(Player player) { return PlayerDataManager.get(player).getLightningChakraModeActive(); }
    @Override public NarutoRun getNarutoRun(Player player) { return PlayerDataManager.get(player).getNarutoRun(); }
    @Override public WallRunning getWallRunning(Player player) { return PlayerDataManager.get(player).getWallRunning(); }
    @Override public ClimberComponent getClimberComponent(Player player) { return PlayerDataManager.get(player).getClimberComponent(); }

    // Set data
    @Override public void setChakra(ServerPlayer player, Chakra chakra) {
        PlayerDataManager.get(player).setChakra(chakra);
    }
    @Override public void setMaxChakra(ServerPlayer player, MaxChakra maxChakra) {
        PlayerDataManager.get(player).setMaxChakra(maxChakra);
    }
    @Override public void setJutsuStorage(ServerPlayer player, JutsuStorage storage) {
        PlayerDataManager.get(player).setJutsuStorage(storage);
    }

    // Sync data (server → client)
    @Override public void syncAffiliation(ServerPlayer player, String value) { FabricPacketHandler.sendToPlayer(player, new SyncStringData("affiliation", value)); }
    @Override public void syncChakra(ServerPlayer player, int value) { FabricPacketHandler.sendToPlayer(player, new SyncIntData("chakra", value)); }
    @Override public void syncMaxChakra(ServerPlayer player, int value) { FabricPacketHandler.sendToPlayer(player, new SyncIntData("max_chakra", value)); }
    @Override public void syncClan(ServerPlayer player, String value) { FabricPacketHandler.sendToPlayer(player, new SyncStringData("clan", value)); }
    @Override public void syncRank(ServerPlayer player, String value) { FabricPacketHandler.sendToPlayer(player, new SyncStringData("rank", value)); }
    @Override public void syncReleaseList(ServerPlayer player, String value) { FabricPacketHandler.sendToPlayer(player, new SyncStringData("release_list", value)); }
    @Override public void syncShinobiPoints(ServerPlayer player, int value) { FabricPacketHandler.sendToPlayer(player, new SyncIntData("shinobi_points", value)); }
    @Override public void syncEarthList(ServerPlayer player, String value) { FabricPacketHandler.sendToPlayer(player, new SyncStringData("earth_list", value)); }
    @Override public void syncFireList(ServerPlayer player, String value) { FabricPacketHandler.sendToPlayer(player, new SyncStringData("fire_list", value)); }
    @Override public void syncLightningList(ServerPlayer player, String value) { FabricPacketHandler.sendToPlayer(player, new SyncStringData("lightning_list", value)); }
    @Override public void syncWaterList(ServerPlayer player, String value) { FabricPacketHandler.sendToPlayer(player, new SyncStringData("water_list", value)); }
    @Override public void syncWindList(ServerPlayer player, String value) { FabricPacketHandler.sendToPlayer(player, new SyncStringData("wind_list", value)); }
    @Override public void syncYangList(ServerPlayer player, String value) { FabricPacketHandler.sendToPlayer(player, new SyncStringData("yang_list", value)); }
    @Override public void syncYinList(ServerPlayer player, String value) { FabricPacketHandler.sendToPlayer(player, new SyncStringData("yin_list", value)); }
    @Override public void syncJutsuStorage(ServerPlayer player, CompoundTag nbt) { FabricPacketHandler.sendToPlayer(player, new SyncNbtData("jutsu_storage", nbt)); }
    @Override public void syncGenjutsu(ServerPlayer player, int value) { FabricPacketHandler.sendToPlayer(player, new SyncIntData("genjutsu", value)); }
    @Override public void syncKenjutsu(ServerPlayer player, int value) { FabricPacketHandler.sendToPlayer(player, new SyncIntData("kenjutsu", value)); }
    @Override public void syncKinjutsu(ServerPlayer player, int value) { FabricPacketHandler.sendToPlayer(player, new SyncIntData("kinjutsu", value)); }
    @Override public void syncMedical(ServerPlayer player, int value) { FabricPacketHandler.sendToPlayer(player, new SyncIntData("medical", value)); }
    @Override public void syncNinjutsu(ServerPlayer player, int value) { FabricPacketHandler.sendToPlayer(player, new SyncIntData("ninjutsu", value)); }
    @Override public void syncSenjutsu(ServerPlayer player, int value) { FabricPacketHandler.sendToPlayer(player, new SyncIntData("senjutsu", value)); }
    @Override public void syncShurikenjutsu(ServerPlayer player, int value) { FabricPacketHandler.sendToPlayer(player, new SyncIntData("shurikenjutsu", value)); }
    @Override public void syncSpeed(ServerPlayer player, int value) { FabricPacketHandler.sendToPlayer(player, new SyncIntData("speed", value)); }
    @Override public void syncSummoning(ServerPlayer player, int value) { FabricPacketHandler.sendToPlayer(player, new SyncIntData("summoning", value)); }
    @Override public void syncTaijutsu(ServerPlayer player, int value) { FabricPacketHandler.sendToPlayer(player, new SyncIntData("taijutsu", value)); }
    @Override public void syncChakraControl(ServerPlayer player, boolean value) { FabricPacketHandler.sendToPlayer(player, new SyncBoolData("chakra_control", value)); }
    @Override public void syncKibaActive(ServerPlayer player, boolean value) { FabricPacketHandler.sendToPlayer(player, new SyncBoolData("kiba_active", value)); }
    @Override public void syncLightningChakraModeActive(ServerPlayer player, boolean value) { FabricPacketHandler.sendToPlayer(player, new SyncBoolData("lightning_chakra_mode_active", value)); }
    @Override public void syncNarutoRun(ServerPlayer player, boolean value) { FabricPacketHandler.sendToPlayer(player, new SyncBoolData("naruto_run", value)); }

    @Override public Block getStaticWaterBlock() { return FabricBlocks.STATIC_WATER_BLOCK; }
}
