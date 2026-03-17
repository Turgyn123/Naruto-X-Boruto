package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;

public class Medical {
    private int value;

    public static final Codec<Medical> CODEC = Codec.INT.xmap(Medical::new, Medical::getValue);

    public Medical() { this.value = 0; }
    public Medical(int value) { this.value = value; }

    public int getValue() { return value; }

    public void setValue(int value) { this.value = value; }

    public void setValue(int value, net.minecraft.server.level.ServerPlayer player) { this.value = value; }
    public void addValue(int amount, net.minecraft.server.level.ServerPlayer player) { this.value += amount; }
    public void subValue(int amount, net.minecraft.server.level.ServerPlayer player) { this.value -= amount; }
    public void incrementValue(int amount, net.minecraft.server.level.ServerPlayer player) { this.value += amount; }
    public void syncValue(net.minecraft.server.level.ServerPlayer player) { /* sync handled by platform */ }
}
