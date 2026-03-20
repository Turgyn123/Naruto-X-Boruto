package net.narutoxboruto.capabilities.stats;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class Genjutsu {
    private int value;
    private static final int MAX_VALUE = 500;

    public static final Codec<Genjutsu> CODEC = Codec.INT.xmap(Genjutsu::new, Genjutsu::getValue);

    public Genjutsu() { this.value = 0; }
    public Genjutsu(int value) { this.value = value; }

    public int getValue() { return value; }

    public void setValue(int value) { this.value = value; }

    public void setValue(int value, ServerPlayer player) {
        this.value = Math.min(value, MAX_VALUE);
        this.syncValue(player);
    }
    public void addValue(int amount, ServerPlayer player) {
        this.value = Math.min(this.value + amount, MAX_VALUE);
        this.syncValue(player);
    }
    public void subValue(int amount, ServerPlayer player) {
        this.value = Math.max(this.value - amount, 0);
        this.syncValue(player);
    }
    public void incrementValue(int amount, ServerPlayer player) {
        this.value = Math.min(this.value + amount, MAX_VALUE);
        this.syncValue(player);
    }
    public void syncValue(ServerPlayer player) {
        Services.PLATFORM.syncGenjutsu(player, this.value);
    }
}
