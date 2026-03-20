package net.narutoxboruto.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.narutoxboruto.capabilities.climber.ClimberComponent;
import net.narutoxboruto.capabilities.info.*;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.capabilities.release.*;
import net.narutoxboruto.capabilities.stats.*;

/**
 * Holds all capability data for a single player.
 * Used by Forge and Fabric loaders as an alternative to NeoForge's AttachmentType system.
 */
public class PlayerCapData {

    // Info
    private Chakra chakra = new Chakra();
    private MaxChakra maxChakra = new MaxChakra();
    private ShinobiPoints shinobiPoints = new ShinobiPoints();
    private Rank rank = new Rank();
    private Clan clan = new Clan();
    private Affiliation affiliation = new Affiliation();
    private ReleaseList releaseList = new ReleaseList();

    // Stats
    private Genjutsu genjutsu = new Genjutsu();
    private Kenjutsu kenjutsu = new Kenjutsu();
    private Kinjutsu kinjutsu = new Kinjutsu();
    private Medical medical = new Medical();
    private Ninjutsu ninjutsu = new Ninjutsu();
    private Senjutsu senjutsu = new Senjutsu();
    private Shurikenjutsu shurikenjutsu = new Shurikenjutsu();
    private Speed speed = new Speed();
    private Summoning summoning = new Summoning();
    private Taijutsu taijutsu = new Taijutsu();

    // Releases
    private EarthList earthList = new EarthList();
    private FireList fireList = new FireList();
    private LightningList lightningList = new LightningList();
    private WaterList waterList = new WaterList();
    private WindList windList = new WindList();
    private YangList yangList = new YangList();
    private YinList yinList = new YinList();

    // Modes
    private ChakraControl chakraControl = new ChakraControl();
    private NarutoRun narutoRun = new NarutoRun();
    private KibaActive kibaActive = new KibaActive();
    private LightningChakraModeActive lightningChakraModeActive = new LightningChakraModeActive();
    private WallRunning wallRunning = new WallRunning();
    private ClimberComponent climberComponent = new ClimberComponent();

    // Jutsu
    private JutsuStorage jutsuStorage = new JutsuStorage();

    // ===== Getters =====
    public Chakra getChakra() { return chakra; }
    public MaxChakra getMaxChakra() { return maxChakra; }
    public ShinobiPoints getShinobiPoints() { return shinobiPoints; }
    public Rank getRank() { return rank; }
    public Clan getClan() { return clan; }
    public Affiliation getAffiliation() { return affiliation; }
    public ReleaseList getReleaseList() { return releaseList; }
    public Genjutsu getGenjutsu() { return genjutsu; }
    public Kenjutsu getKenjutsu() { return kenjutsu; }
    public Kinjutsu getKinjutsu() { return kinjutsu; }
    public Medical getMedical() { return medical; }
    public Ninjutsu getNinjutsu() { return ninjutsu; }
    public Senjutsu getSenjutsu() { return senjutsu; }
    public Shurikenjutsu getShurikenjutsu() { return shurikenjutsu; }
    public Speed getSpeed() { return speed; }
    public Summoning getSummoning() { return summoning; }
    public Taijutsu getTaijutsu() { return taijutsu; }
    public EarthList getEarthList() { return earthList; }
    public FireList getFireList() { return fireList; }
    public LightningList getLightningList() { return lightningList; }
    public WaterList getWaterList() { return waterList; }
    public WindList getWindList() { return windList; }
    public YangList getYangList() { return yangList; }
    public YinList getYinList() { return yinList; }
    public ChakraControl getChakraControl() { return chakraControl; }
    public NarutoRun getNarutoRun() { return narutoRun; }
    public KibaActive getKibaActive() { return kibaActive; }
    public LightningChakraModeActive getLightningChakraModeActive() { return lightningChakraModeActive; }
    public WallRunning getWallRunning() { return wallRunning; }
    public ClimberComponent getClimberComponent() { return climberComponent; }
    public JutsuStorage getJutsuStorage() { return jutsuStorage; }

    // ===== Setters =====
    public void setChakra(Chakra v) { this.chakra = v; }
    public void setMaxChakra(MaxChakra v) { this.maxChakra = v; }
    public void setJutsuStorage(JutsuStorage v) { this.jutsuStorage = v; }

    // ===== Int value setter by key (for generic sync packets) =====
    public void setIntValue(String key, int value) {
        switch (key) {
            case "chakra" -> chakra = new Chakra(value);
            case "max_chakra" -> maxChakra = new MaxChakra(value);
            case "shinobi_points" -> { ShinobiPoints sp = new ShinobiPoints(); sp.value = value; shinobiPoints = sp; }
            case "genjutsu" -> genjutsu = new Genjutsu(value);
            case "kenjutsu" -> kenjutsu = new Kenjutsu(value);
            case "kinjutsu" -> kinjutsu = new Kinjutsu(value);
            case "medical" -> medical = new Medical(value);
            case "ninjutsu" -> ninjutsu = new Ninjutsu(value);
            case "senjutsu" -> senjutsu = new Senjutsu(value);
            case "shurikenjutsu" -> shurikenjutsu = new Shurikenjutsu(value);
            case "speed" -> speed = new Speed(value);
            case "summoning" -> summoning = new Summoning(value);
            case "taijutsu" -> taijutsu = new Taijutsu(value);
        }
    }

    // ===== String value setter by key (for generic sync packets) =====
    public void setStringValue(String key, String value) {
        switch (key) {
            case "affiliation" -> affiliation.setValue(value);
            case "clan" -> clan.setValue(value);
            case "rank" -> rank.setValue(value);
            case "release_list" -> releaseList.setValue(value);
            case "earth_list" -> earthList.setValue(value);
            case "fire_list" -> fireList.setValue(value);
            case "lightning_list" -> lightningList.setValue(value);
            case "water_list" -> waterList.setValue(value);
            case "wind_list" -> windList.setValue(value);
            case "yang_list" -> yangList.setValue(value);
            case "yin_list" -> yinList.setValue(value);
        }
    }

    // ===== Bool value setter by key (for generic sync packets) =====
    public void setBoolValue(String key, boolean value) {
        switch (key) {
            case "chakra_control" -> chakraControl.setValue(value);
            case "kiba_active" -> kibaActive.setValue(value);
            case "lightning_chakra_mode_active" -> lightningChakraModeActive.setValue(value);
            case "naruto_run" -> narutoRun.setValue(value);
        }
    }

    // ===== Save to NBT =====
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        // Info
        tag.putInt("chakra", chakra.getValue());
        tag.putInt("max_chakra", maxChakra.getValue());
        tag.putInt("shinobi_points", shinobiPoints.getValue());
        tag.putString("rank", rank.getValue());
        tag.putString("clan", clan.getValue());
        tag.putString("affiliation", affiliation.getValue());
        tag.putString("release_list", releaseList.getValue());
        // Stats
        tag.putInt("genjutsu", genjutsu.getValue());
        tag.putInt("kenjutsu", kenjutsu.getValue());
        tag.putInt("kinjutsu", kinjutsu.getValue());
        tag.putInt("medical", medical.getValue());
        tag.putInt("ninjutsu", ninjutsu.getValue());
        tag.putInt("senjutsu", senjutsu.getValue());
        tag.putInt("shurikenjutsu", shurikenjutsu.getValue());
        tag.putInt("speed", speed.getValue());
        tag.putInt("summoning", summoning.getValue());
        tag.putInt("taijutsu", taijutsu.getValue());
        // Releases
        tag.putString("earth_list", earthList.getValue());
        tag.putString("fire_list", fireList.getValue());
        tag.putString("lightning_list", lightningList.getValue());
        tag.putString("water_list", waterList.getValue());
        tag.putString("wind_list", windList.getValue());
        tag.putString("yang_list", yangList.getValue());
        tag.putString("yin_list", yinList.getValue());
        // Modes
        tag.putBoolean("chakra_control", chakraControl.getValue());
        tag.putBoolean("naruto_run", narutoRun.getValue());
        tag.putBoolean("kiba_active", kibaActive.getValue());
        tag.putBoolean("lcm_active", lightningChakraModeActive.getValue());
        tag.putBoolean("wall_running", wallRunning.getValue());
        // Jutsu Storage
        tag.put("jutsu_storage", jutsuStorage.toNbt());
        return tag;
    }

    // ===== Load from NBT =====
    public void loadFromNbt(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return;
        // Info
        if (tag.contains("chakra")) chakra = new Chakra(tag.getInt("chakra"));
        if (tag.contains("max_chakra")) maxChakra = new MaxChakra(tag.getInt("max_chakra"));
        if (tag.contains("shinobi_points")) { ShinobiPoints sp = new ShinobiPoints(); sp.value = tag.getInt("shinobi_points"); shinobiPoints = sp; }
        if (tag.contains("rank")) rank = new Rank(tag.getString("rank"));
        if (tag.contains("clan")) clan = new Clan(tag.getString("clan"));
        if (tag.contains("affiliation")) affiliation = new Affiliation(tag.getString("affiliation"));
        if (tag.contains("release_list")) { releaseList = new ReleaseList(); releaseList.setValue(tag.getString("release_list")); }
        // Stats
        if (tag.contains("genjutsu")) genjutsu = new Genjutsu(tag.getInt("genjutsu"));
        if (tag.contains("kenjutsu")) kenjutsu = new Kenjutsu(tag.getInt("kenjutsu"));
        if (tag.contains("kinjutsu")) kinjutsu = new Kinjutsu(tag.getInt("kinjutsu"));
        if (tag.contains("medical")) medical = new Medical(tag.getInt("medical"));
        if (tag.contains("ninjutsu")) ninjutsu = new Ninjutsu(tag.getInt("ninjutsu"));
        if (tag.contains("senjutsu")) senjutsu = new Senjutsu(tag.getInt("senjutsu"));
        if (tag.contains("shurikenjutsu")) shurikenjutsu = new Shurikenjutsu(tag.getInt("shurikenjutsu"));
        if (tag.contains("speed")) speed = new Speed(tag.getInt("speed"));
        if (tag.contains("summoning")) summoning = new Summoning(tag.getInt("summoning"));
        if (tag.contains("taijutsu")) taijutsu = new Taijutsu(tag.getInt("taijutsu"));
        // Releases
        if (tag.contains("earth_list")) { earthList = new EarthList(); earthList.setValue(tag.getString("earth_list")); }
        if (tag.contains("fire_list")) { fireList = new FireList(); fireList.setValue(tag.getString("fire_list")); }
        if (tag.contains("lightning_list")) { lightningList = new LightningList(); lightningList.setValue(tag.getString("lightning_list")); }
        if (tag.contains("water_list")) { waterList = new WaterList(); waterList.setValue(tag.getString("water_list")); }
        if (tag.contains("wind_list")) { windList = new WindList(); windList.setValue(tag.getString("wind_list")); }
        if (tag.contains("yang_list")) { yangList = new YangList(); yangList.setValue(tag.getString("yang_list")); }
        if (tag.contains("yin_list")) { yinList = new YinList(); yinList.setValue(tag.getString("yin_list")); }
        // Modes
        if (tag.contains("chakra_control")) chakraControl = new ChakraControl(tag.getBoolean("chakra_control"));
        if (tag.contains("naruto_run")) narutoRun = new NarutoRun(tag.getBoolean("naruto_run"));
        if (tag.contains("kiba_active")) kibaActive = new KibaActive(tag.getBoolean("kiba_active"));
        if (tag.contains("lcm_active")) lightningChakraModeActive = new LightningChakraModeActive(tag.getBoolean("lcm_active"));
        if (tag.contains("wall_running")) wallRunning = new WallRunning(tag.getBoolean("wall_running"));
        // Jutsu Storage
        if (tag.contains("jutsu_storage")) jutsuStorage = JutsuStorage.fromNbt(tag.getCompound("jutsu_storage"));
    }

    // ===== Copy from another PlayerCapData =====
    public void copyFrom(PlayerCapData source) {
        this.chakra = new Chakra(source.chakra.getValue());
        this.maxChakra = new MaxChakra(source.maxChakra.getValue());
        ShinobiPoints sp = new ShinobiPoints(); sp.value = source.shinobiPoints.getValue(); this.shinobiPoints = sp;
        this.rank = new Rank(source.rank.getValue());
        this.clan = new Clan(source.clan.getValue());
        this.affiliation = new Affiliation(source.affiliation.getValue());
        this.releaseList = new ReleaseList(); this.releaseList.setValue(source.releaseList.getValue());

        this.genjutsu = new Genjutsu(source.genjutsu.getValue());
        this.kenjutsu = new Kenjutsu(source.kenjutsu.getValue());
        this.kinjutsu = new Kinjutsu(source.kinjutsu.getValue());
        this.medical = new Medical(source.medical.getValue());
        this.ninjutsu = new Ninjutsu(source.ninjutsu.getValue());
        this.senjutsu = new Senjutsu(source.senjutsu.getValue());
        this.shurikenjutsu = new Shurikenjutsu(source.shurikenjutsu.getValue());
        this.speed = new Speed(source.speed.getValue());
        this.summoning = new Summoning(source.summoning.getValue());
        this.taijutsu = new Taijutsu(source.taijutsu.getValue());

        this.earthList = new EarthList(); this.earthList.setValue(source.earthList.getValue());
        this.fireList = new FireList(); this.fireList.setValue(source.fireList.getValue());
        this.lightningList = new LightningList(); this.lightningList.setValue(source.lightningList.getValue());
        this.waterList = new WaterList(); this.waterList.setValue(source.waterList.getValue());
        this.windList = new WindList(); this.windList.setValue(source.windList.getValue());
        this.yangList = new YangList(); this.yangList.setValue(source.yangList.getValue());
        this.yinList = new YinList(); this.yinList.setValue(source.yinList.getValue());

        this.chakraControl = new ChakraControl(source.chakraControl.getValue());
        this.narutoRun = new NarutoRun(source.narutoRun.getValue());
        this.kibaActive = new KibaActive(source.kibaActive.getValue());
        this.lightningChakraModeActive = new LightningChakraModeActive(source.lightningChakraModeActive.getValue());
        this.wallRunning = new WallRunning(source.wallRunning.getValue());

        this.jutsuStorage = new JutsuStorage();
        this.jutsuStorage.copyFrom(source.jutsuStorage);
    }
}
