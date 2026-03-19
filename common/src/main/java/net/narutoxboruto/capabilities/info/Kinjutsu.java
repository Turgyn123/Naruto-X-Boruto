package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class Kinjutsu {
    private int value;
    private static final int MAX_VALUE = 500;

    public static final Codec<Kinjutsu> CODEC = Codec.INT.xmap(Kinjutsu::new, Kinjutsu::getValue);

    public Kinjutsu() { this.value = 0; }
    public Kinjutsu(int value) { this.value = value; }

    public int getValue() { return value; }

    public void setValue(int value) { this.value = value; }

    public void setValue(int value, net.minecraft.server.level.ServerPlayer player) {
        this.value = Math.min(value, MAX_VALUE);
        this.syncValue(player);
    }
    public void addValue(int amount, net.minecraft.server.level.ServerPlayer player) {
        this.value = Math.min(this.value + amount, MAX_VALUE);
        this.syncValue(player);
    }
    public void subValue(int amount, net.minecraft.server.level.ServerPlayer player) {
        this.value = Math.max(this.value - amount, 0);
        this.syncValue(player);
    }
    public void incrementValue(int amount, net.minecraft.server.level.ServerPlayer player) {
        this.value = Math.min(this.value + amount, MAX_VALUE);
        this.syncValue(player);
    }
    public void syncValue(net.minecraft.server.level.ServerPlayer player) {
        Services.PLATFORM.syncKinjutsu(player, this.value);
    }
}
