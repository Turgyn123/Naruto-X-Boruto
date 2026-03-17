package net.narutoxboruto.main.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.capabilities.PlayerCapData;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.climber.ClimberComponent;
import net.narutoxboruto.capabilities.info.*;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.capabilities.release.*;
import net.narutoxboruto.main.platform.services.IPlatformHelper;
import net.narutoxboruto.networking.ForgePacketHandler;
import net.narutoxboruto.networking.SyncBoolData;
import net.narutoxboruto.networking.SyncIntData;
import net.narutoxboruto.networking.SyncNbtData;
import net.narutoxboruto.networking.SyncStringData;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.items.ItemStackHandler;
import net.narutoxboruto.fluids.ForgeBlocks;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
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
    @Override public void syncAffiliation(ServerPlayer player, String value) { ForgePacketHandler.sendToPlayer(new SyncStringData("affiliation", value), player); }
    @Override public void syncChakra(ServerPlayer player, int value) { ForgePacketHandler.sendToPlayer(new SyncIntData("chakra", value), player); }
    @Override public void syncMaxChakra(ServerPlayer player, int value) { ForgePacketHandler.sendToPlayer(new SyncIntData("max_chakra", value), player); }
    @Override public void syncClan(ServerPlayer player, String value) { ForgePacketHandler.sendToPlayer(new SyncStringData("clan", value), player); }
    @Override public void syncRank(ServerPlayer player, String value) { ForgePacketHandler.sendToPlayer(new SyncStringData("rank", value), player); }
    @Override public void syncReleaseList(ServerPlayer player, String value) { ForgePacketHandler.sendToPlayer(new SyncStringData("release_list", value), player); }
    @Override public void syncShinobiPoints(ServerPlayer player, int value) { ForgePacketHandler.sendToPlayer(new SyncIntData("shinobi_points", value), player); }
    @Override public void syncEarthList(ServerPlayer player, String value) { ForgePacketHandler.sendToPlayer(new SyncStringData("earth_list", value), player); }
    @Override public void syncFireList(ServerPlayer player, String value) { ForgePacketHandler.sendToPlayer(new SyncStringData("fire_list", value), player); }
    @Override public void syncLightningList(ServerPlayer player, String value) { ForgePacketHandler.sendToPlayer(new SyncStringData("lightning_list", value), player); }
    @Override public void syncWaterList(ServerPlayer player, String value) { ForgePacketHandler.sendToPlayer(new SyncStringData("water_list", value), player); }
    @Override public void syncWindList(ServerPlayer player, String value) { ForgePacketHandler.sendToPlayer(new SyncStringData("wind_list", value), player); }
    @Override public void syncYangList(ServerPlayer player, String value) { ForgePacketHandler.sendToPlayer(new SyncStringData("yang_list", value), player); }
    @Override public void syncYinList(ServerPlayer player, String value) { ForgePacketHandler.sendToPlayer(new SyncStringData("yin_list", value), player); }
    @Override public void syncJutsuStorage(ServerPlayer player, CompoundTag nbt) { ForgePacketHandler.sendToPlayer(new SyncNbtData("jutsu_storage", nbt), player); }
    @Override public void syncGenjutsu(ServerPlayer player, int value) { ForgePacketHandler.sendToPlayer(new SyncIntData("genjutsu", value), player); }
    @Override public void syncKenjutsu(ServerPlayer player, int value) { ForgePacketHandler.sendToPlayer(new SyncIntData("kenjutsu", value), player); }
    @Override public void syncKinjutsu(ServerPlayer player, int value) { ForgePacketHandler.sendToPlayer(new SyncIntData("kinjutsu", value), player); }
    @Override public void syncMedical(ServerPlayer player, int value) { ForgePacketHandler.sendToPlayer(new SyncIntData("medical", value), player); }
    @Override public void syncNinjutsu(ServerPlayer player, int value) { ForgePacketHandler.sendToPlayer(new SyncIntData("ninjutsu", value), player); }
    @Override public void syncSenjutsu(ServerPlayer player, int value) { ForgePacketHandler.sendToPlayer(new SyncIntData("senjutsu", value), player); }
    @Override public void syncShurikenjutsu(ServerPlayer player, int value) { ForgePacketHandler.sendToPlayer(new SyncIntData("shurikenjutsu", value), player); }
    @Override public void syncSpeed(ServerPlayer player, int value) { ForgePacketHandler.sendToPlayer(new SyncIntData("speed", value), player); }
    @Override public void syncSummoning(ServerPlayer player, int value) { ForgePacketHandler.sendToPlayer(new SyncIntData("summoning", value), player); }
    @Override public void syncTaijutsu(ServerPlayer player, int value) { ForgePacketHandler.sendToPlayer(new SyncIntData("taijutsu", value), player); }
    @Override public void syncChakraControl(ServerPlayer player, boolean value) { ForgePacketHandler.sendToPlayer(new SyncBoolData("chakra_control", value), player); }
    @Override public void syncKibaActive(ServerPlayer player, boolean value) { ForgePacketHandler.sendToPlayer(new SyncBoolData("kiba_active", value), player); }
    @Override public void syncLightningChakraModeActive(ServerPlayer player, boolean value) { ForgePacketHandler.sendToPlayer(new SyncBoolData("lightning_chakra_mode_active", value), player); }
    @Override public void syncNarutoRun(ServerPlayer player, boolean value) { ForgePacketHandler.sendToPlayer(new SyncBoolData("naruto_run", value), player); }

    @Override public Block getStaticWaterBlock() { return ForgeBlocks.STATIC_WATER_BLOCK.get(); }

    public static ItemStackHandler toItemStackHandler(JutsuStorage storage) {
        ItemStackHandler handler = new ItemStackHandler(54);
        for (int i = 0; i < 54; i++) {
            handler.setStackInSlot(i, storage.getItems().get(i).copy());
        }
        return handler;
    }
}