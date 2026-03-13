package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class ShinobiPoints {
    private final String id;
    protected int maxValue;
    public int value;

    public static final Codec<ShinobiPoints> CODEC = Codec.INT.xmap(value -> { ShinobiPoints sp = new ShinobiPoints(); sp.value = value; return sp; }, ShinobiPoints::getValue);

    public ShinobiPoints(String id, int maxValue) {
        this.id = id;
        this.maxValue = maxValue;
        this.value = 0;
    }

    public ShinobiPoints() { this("shinobi_points", Integer.MAX_VALUE); }

    public int getValue() { return value; }

    public int getMaxValue() { return maxValue; }

    public String getId() { return id; }

    public void incrementValue(ServerPlayer serverPlayer) { incrementValue(1, serverPlayer); }

    public void incrementValue(int add, ServerPlayer serverPlayer) {
        this.value = Math.min(value + add, maxValue);
        this.syncValue(serverPlayer);
    }

    public void addValue(int add, ServerPlayer serverPlayer) { incrementValue(add, serverPlayer); }

    public void setValue(int value, ServerPlayer serverPlayer) {
        this.value = Math.min(value, maxValue);
        this.syncValue(serverPlayer);
    }

    public void subValue(int sub, ServerPlayer serverPlayer) {
        this.value = Math.max(value - sub, 0);
        this.syncValue(serverPlayer);
    }

    public void syncValue(ServerPlayer serverPlayer) {
        Services.PLATFORM.syncShinobiPoints(serverPlayer, this.value);
    }

    public void copyFrom(ShinobiPoints source, ServerPlayer serverPlayer) {
        this.value = source.getValue();
        this.syncValue(serverPlayer);
    }

    public void resetValue(ServerPlayer serverPlayer) {
        this.value = 0;
        this.syncValue(serverPlayer);
    }
}
