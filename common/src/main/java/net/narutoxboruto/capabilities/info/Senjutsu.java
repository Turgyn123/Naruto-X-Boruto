package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;

public class Senjutsu {
    private int value;

    public static final Codec<Senjutsu> CODEC = Codec.INT.xmap(Senjutsu::new, Senjutsu::getValue);

    public Senjutsu() { this.value = 0; }
    public Senjutsu(int value) { this.value = value; }

    public int getValue() { return value; }

    public void setValue(int value) { this.value = value; }

    public void setValue(int value, net.minecraft.server.level.ServerPlayer player) { this.value = value; }
    public void addValue(int amount, net.minecraft.server.level.ServerPlayer player) { this.value += amount; }
    public void subValue(int amount, net.minecraft.server.level.ServerPlayer player) { this.value -= amount; }
    public void incrementValue(int amount, net.minecraft.server.level.ServerPlayer player) { this.value += amount; }
    public void syncValue(net.minecraft.server.level.ServerPlayer player) { /* sync handled by platform */ }
}
