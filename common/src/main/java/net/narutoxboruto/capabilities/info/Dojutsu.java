package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

import java.util.*;
import java.util.stream.Collectors;

public class Dojutsu {
    private String unlockedList;
    private String leftEye;
    private String rightEye;
    private int timer;
    private int leftEyeOffsetX;
    private int leftEyeOffsetY;
    private int rightEyeOffsetX;
    private int rightEyeOffsetY;
    private float eyeScale = 1.0F;
    private boolean eyesVisible = true;

    // ── Sharingan progression (Uchiha clan only) ─────────────────────────
    /** Total ticks the player has spent in the Uchiha clan. Includes time before 1-tomoe. */
    private int sharinganClanPlaytime = 0;
    /** True once the player has been at NEAR_DEATH_HP_THRESHOLD or lower for NEAR_DEATH_DURATION_TICKS while having 1-tomoe unlocked. */
    private boolean nearDeathWith1Tomoe = false;
    /** True once the player has been at NEAR_DEATH_HP_THRESHOLD or lower for NEAR_DEATH_DURATION_TICKS while having 2-tomoe unlocked. */
    private boolean nearDeathWith2Tomoe = false;
    /** Transient (server-only) running count of consecutive low-HP ticks. Not persisted. */
    private transient int lowHpTicks = 0;

    public static final int REQUIRED_TICKS = 36000; // 30 minutes at 20 TPS
    public static final int MAX_DOJUTSU = 2;

    // Sharingan upgrade thresholds (Uchiha-only). 20 ticks per second.
    public static final int SHARINGAN_2_TOMOE_PLAYTIME_TICKS = 3 * 60 * 60 * 20; // 3 hours
    public static final int SHARINGAN_3_TOMOE_PLAYTIME_TICKS = 8 * 60 * 60 * 20; // 8 hours
    public static final float NEAR_DEATH_HP_THRESHOLD = 4.0F; // 2 hearts
    public static final int NEAR_DEATH_DURATION_TICKS = 100; // 5 seconds

    public static final List<String> DOJUTSU_TYPES = Arrays.asList(
            "1_tomoe_sharingan", "2_tomoe_sharingan", "3_tomoe_sharingan",
            "byakugan", "ketsuryugan"
    );

    public static final Map<String, String> CLAN_DOJUTSU = Map.of(
            "uchiha", "1_tomoe_sharingan",
            "hyuuga", "byakugan",
            "chinoike", "ketsuryugan"
    );

    // Sharingan evolution chain: key = new level, value = prerequisite level it replaces
    public static final Map<String, String> SHARINGAN_UPGRADES = Map.of(
            "2_tomoe_sharingan", "1_tomoe_sharingan",
            "3_tomoe_sharingan", "2_tomoe_sharingan"
    );

    // Map dojutsu type to icon texture name
    public static final Map<String, String> DOJUTSU_ICON = Map.of(
            "1_tomoe_sharingan", "1_tomoe_icon",
            "2_tomoe_sharingan", "2_tomoe_icon",
            "3_tomoe_sharingan", "3_tomoe_icon",
            "byakugan", "byakugan_icon",
            "ketsuryugan", "ketsuryugan_icon"
    );

    // Map dojutsu type to left eye texture name
    public static final Map<String, String> DOJUTSU_LEFT_EYE = Map.of(
            "1_tomoe_sharingan", "left_eye_1_tomoe",
            "2_tomoe_sharingan", "left_eye_2_tomoe",
            "3_tomoe_sharingan", "left_eye_3_tomoe",
            "byakugan", "left_eye_byakugan",
            "ketsuryugan", "left_eye_ketsuryugan"
    );

    // Map dojutsu type to right eye texture name
    public static final Map<String, String> DOJUTSU_RIGHT_EYE = Map.of(
            "1_tomoe_sharingan", "right_eye_1_tomoe",
            "2_tomoe_sharingan", "right_eye_2_tomoe",
            "3_tomoe_sharingan", "right_eye_3_tomoe",
            "byakugan", "right_eye_byakugan",
            "ketsuryugan", "right_eye_ketsuryugan"
    );

    public static final Codec<Dojutsu> CODEC = CompoundTag.CODEC.xmap(
            Dojutsu::fromNbt,
            Dojutsu::toNbt
    );

    public Dojutsu() {
        this.unlockedList = "";
        this.leftEye = "";
        this.rightEye = "";
        this.timer = 0;
    }

    public Dojutsu(String unlockedList, String leftEye, String rightEye, int timer) {
        this.unlockedList = unlockedList;
        this.leftEye = leftEye;
        this.rightEye = rightEye;
        this.timer = timer;
    }

    public static final int MAX_EYE_OFFSET = 20;
    public static final float MIN_EYE_SCALE = 0.5F;
    public static final float MAX_EYE_SCALE = 3.0F;
    public static final float EYE_SCALE_STEP = 0.25F;

    // NBT serialization
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("unlocked", unlockedList);
        tag.putString("left_eye", leftEye);
        tag.putString("right_eye", rightEye);
        tag.putInt("timer", timer);
        tag.putInt("left_eye_offset_x", leftEyeOffsetX);
        tag.putInt("left_eye_offset_y", leftEyeOffsetY);
        tag.putInt("right_eye_offset_x", rightEyeOffsetX);
        tag.putInt("right_eye_offset_y", rightEyeOffsetY);
        tag.putFloat("eye_scale", eyeScale);
        tag.putBoolean("eyes_visible", eyesVisible);
        tag.putInt("sharingan_playtime", sharinganClanPlaytime);
        tag.putBoolean("near_death_1_tomoe", nearDeathWith1Tomoe);
        tag.putBoolean("near_death_2_tomoe", nearDeathWith2Tomoe);
        return tag;
    }

    public static Dojutsu fromNbt(CompoundTag tag) {
        Dojutsu d = new Dojutsu();
        if (tag != null) {
            d.unlockedList = tag.getString("unlocked");
            d.leftEye = tag.getString("left_eye");
            d.rightEye = tag.getString("right_eye");
            d.timer = tag.getInt("timer");
            d.leftEyeOffsetX = tag.getInt("left_eye_offset_x");
            d.leftEyeOffsetY = tag.getInt("left_eye_offset_y");
            d.rightEyeOffsetX = tag.getInt("right_eye_offset_x");
            d.rightEyeOffsetY = tag.getInt("right_eye_offset_y");
            d.eyeScale = tag.contains("eye_scale") ? tag.getFloat("eye_scale") : 1.0F;
            d.eyesVisible = !tag.contains("eyes_visible") || tag.getBoolean("eyes_visible");
            d.sharinganClanPlaytime = tag.getInt("sharingan_playtime");
            d.nearDeathWith1Tomoe = tag.getBoolean("near_death_1_tomoe");
            d.nearDeathWith2Tomoe = tag.getBoolean("near_death_2_tomoe");
        }
        return d;
    }

    // Getters
    public String getUnlockedListRaw() { return unlockedList; }
    public String getLeftEye() { return leftEye; }
    public String getRightEye() { return rightEye; }
    public int getTimer() { return timer; }
    public int getLeftEyeOffsetX() { return leftEyeOffsetX; }
    public int getLeftEyeOffsetY() { return leftEyeOffsetY; }
    public int getRightEyeOffsetX() { return rightEyeOffsetX; }
    public int getRightEyeOffsetY() { return rightEyeOffsetY; }
    public float getEyeScale() { return eyeScale; }
    public boolean areEyesVisible() { return eyesVisible; }

    public List<String> getUnlockedList() {
        if (unlockedList == null || unlockedList.isEmpty()) return Collections.emptyList();
        return Arrays.stream(unlockedList.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public int getUnlockedCount() {
        return getUnlockedList().size();
    }

    public boolean hasUnlocked(String dojutsu) {
        return getUnlockedList().contains(dojutsu);
    }

    public boolean canObtainMore() {
        return getUnlockedCount() < MAX_DOJUTSU;
    }

    // Setters
    public void setLeftEye(String dojutsu) {
        if (dojutsu.isEmpty() || hasUnlocked(dojutsu)) {
            this.leftEye = dojutsu;
        }
    }

    public void setRightEye(String dojutsu) {
        if (dojutsu.isEmpty() || hasUnlocked(dojutsu)) {
            this.rightEye = dojutsu;
        }
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public void setLeftEyeOffset(int x, int y) {
        this.leftEyeOffsetX = Math.max(-MAX_EYE_OFFSET, Math.min(MAX_EYE_OFFSET, x));
        this.leftEyeOffsetY = Math.max(-MAX_EYE_OFFSET, Math.min(MAX_EYE_OFFSET, y));
    }

    public void setRightEyeOffset(int x, int y) {
        this.rightEyeOffsetX = Math.max(-MAX_EYE_OFFSET, Math.min(MAX_EYE_OFFSET, x));
        this.rightEyeOffsetY = Math.max(-MAX_EYE_OFFSET, Math.min(MAX_EYE_OFFSET, y));
    }

    public void setEyeScale(float scale) {
        this.eyeScale = Math.max(MIN_EYE_SCALE, Math.min(MAX_EYE_SCALE, scale));
    }

    public void setEyesVisible(boolean visible) {
        this.eyesVisible = visible;
    }

    public void resetEyeVisuals() {
        this.leftEyeOffsetX = 0;
        this.leftEyeOffsetY = 0;
        this.rightEyeOffsetX = 0;
        this.rightEyeOffsetY = 0;
        this.eyeScale = 1.0F;
    }

    public void incrementTimer() {
        this.timer++;
    }

    public void resetTimer() {
        this.timer = 0;
    }

    public boolean isTimerComplete() {
        return this.timer >= REQUIRED_TICKS;
    }

    public void unlock(String dojutsu) {
        if (hasUnlocked(dojutsu) || !canObtainMore()) return;
        if (unlockedList.isEmpty()) {
            unlockedList = dojutsu;
        } else {
            unlockedList = unlockedList + "," + dojutsu;
        }
        // Auto-equip to both eyes if both are empty, otherwise fill first empty slot
        if (leftEye.isEmpty() && rightEye.isEmpty()) {
            leftEye = dojutsu;
            rightEye = dojutsu;
        } else if (leftEye.isEmpty()) {
            leftEye = dojutsu;
        } else if (rightEye.isEmpty()) {
            rightEye = dojutsu;
        }
    }

    /**
     * Upgrades an existing dojutsu to the next evolution (e.g. 1_tomoe → 2_tomoe_sharingan).
     * Replaces oldType with newType in the unlocked list and updates equipped eyes.
     */
    public void upgrade(String oldType, String newType) {
        if (!hasUnlocked(oldType)) return;
        List<String> list = new ArrayList<>(getUnlockedList());
        int idx = list.indexOf(oldType);
        if (idx >= 0) list.set(idx, newType);
        this.unlockedList = String.join(",", list);
        if (oldType.equals(leftEye)) leftEye = newType;
        if (oldType.equals(rightEye)) rightEye = newType;
    }

    public void syncValue(ServerPlayer serverPlayer) {
        Services.PLATFORM.syncDojutsu(serverPlayer, this.toNbt());
    }

    /**
     * Gets the dojutsu type that a clan would grant, or null if the clan has no dojutsu.
     */
    public static String getDojutsuForClan(String clan) {
        return CLAN_DOJUTSU.get(clan);
    }

    /**
     * Checks if a clan is eligible for dojutsu acquisition.
     */
    public static boolean isClanEligible(String clan) {
        return CLAN_DOJUTSU.containsKey(clan);
    }

    /**
     * True if the player already owns any tier in the evolution family of the dojutsu the given
     * clan grants. Prevents re-awakening a fresh 1_tomoe_sharingan when the player has already
     * upgraded to 2- or 3-tomoe and re-acquires the Uchiha clan.
     */
    public boolean hasClanDojutsuFamily(String clan) {
        String base = getDojutsuForClan(clan);
        if (base == null) return false;
        if (hasUnlocked(base)) return true;
        if ("uchiha".equals(clan)) {
            return hasUnlocked("2_tomoe_sharingan") || hasUnlocked("3_tomoe_sharingan");
        }
        return false;
    }

    // ── Sharingan progression accessors ──────────────────────────────────
    public int getSharinganPlaytime() { return sharinganClanPlaytime; }
    public void setSharinganPlaytime(int ticks) { this.sharinganClanPlaytime = Math.max(0, ticks); }
    public void incrementSharinganPlaytime() { this.sharinganClanPlaytime++; }

    public boolean hasNearDeathWith1Tomoe() { return nearDeathWith1Tomoe; }
    public void markNearDeathWith1Tomoe() { this.nearDeathWith1Tomoe = true; }

    public boolean hasNearDeathWith2Tomoe() { return nearDeathWith2Tomoe; }
    public void markNearDeathWith2Tomoe() { this.nearDeathWith2Tomoe = true; }

    public int getLowHpTicks() { return lowHpTicks; }
    public void incrementLowHpTicks() { this.lowHpTicks++; }
    public void resetLowHpTicks() { this.lowHpTicks = 0; }
}
