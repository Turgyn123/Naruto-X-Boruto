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

    public static final int REQUIRED_TICKS = 36000; // 30 minutes at 20 TPS
    public static final int MAX_DOJUTSU = 2;

    public static final List<String> DOJUTSU_TYPES = Arrays.asList(
            "1_tomoe_sharingan", "2_tomoe_sharingan", "3_tomoe_sharingan",
            "byakugan", "ketsuryugan"
    );

    public static final Map<String, String> CLAN_DOJUTSU = Map.of(
            "uchiha", "1_tomoe_sharingan",
            "hyuuga", "byakugan",
            "chinoike", "ketsuryugan"
    );

    // Map dojutsu type to icon texture name
    public static final Map<String, String> DOJUTSU_ICON = Map.of(
            "1_tomoe_sharingan", "1_tomoe_icon",
            "2_tomoe_sharingan", "2_tomoe_icon",
            "3_tomoe_sharingan", "3_tomoe_icon",
            "byakugan", "byakugan_icon",
            "ketsuryugan", "ketsuryugan_icon"
    );

    // Map dojutsu type to eye texture name
    public static final Map<String, String> DOJUTSU_EYES = Map.of(
            "1_tomoe_sharingan", "1_tomoe_eyes",
            "2_tomoe_sharingan", "2_tomoe_eyes",
            "3_tomoe_sharingan", "3_tomoe_eyes",
            "byakugan", "byakugan_eyes",
            "ketsuryugan", "ketsuryugan_eyes"
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

    public static final int MAX_EYE_OFFSET = 5;

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
}
