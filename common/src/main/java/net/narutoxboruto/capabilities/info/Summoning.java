package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;

public class Summoning {
    private int value;

    public static final Codec<Summoning> CODEC = Codec.INT.xmap(Summoning::new, Summoning::getValue);

    public Summoning() { this.value = 0; }
    public Summoning(int value) { this.value = value; }

    public int getValue() { return value; }

    public void setValue(int value) { this.value = value; }

    public void setValue(int value, net.minecraft.server.level.ServerPlayer player) { this.value = value; }
    public void addValue(int amount, net.minecraft.server.level.ServerPlayer player) { this.value += amount; }
    public void subValue(int amount, net.minecraft.server.level.ServerPlayer player) { this.value -= amount; }
    public void incrementValue(int amount, net.minecraft.server.level.ServerPlayer player) { this.value += amount; }
    public void syncValue(net.minecraft.server.level.ServerPlayer player) { /* sync handled by platform */ }
}
