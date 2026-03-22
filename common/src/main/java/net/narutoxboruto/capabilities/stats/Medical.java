package net.narutoxboruto.capabilities.stats;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.narutoxboruto.main.platform.Services;

public class Medical {
    private int value;
    private static final int MAX_VALUE = 500;

    public static final Codec<Medical> CODEC = Codec.INT.xmap(Medical::new, Medical::getValue);

    public Medical() { this.value = 0; }
    public Medical(int value) { this.value = value; }

    public int getValue() { return value; }

    public void setValue(int value) { this.value = value; }

    public void setValue(int value, ServerPlayer player) {
        this.value = Math.min(value, MAX_VALUE);
        updateMaxHealth(player);
        this.syncValue(player);
    }
    public void addValue(int amount, ServerPlayer player) {
        this.value = Math.min(this.value + amount, MAX_VALUE);
        updateMaxHealth(player);
        this.syncValue(player);
    }
    public void subValue(int amount, ServerPlayer player) {
        this.value = Math.max(this.value - amount, 0);
        updateMaxHealth(player);
        this.syncValue(player);
    }
    public void incrementValue(int amount, ServerPlayer player) {
        this.value = Math.min(this.value + amount, MAX_VALUE);
        updateMaxHealth(player);
        this.syncValue(player);
    }
    public void syncValue(ServerPlayer player) {
        Services.PLATFORM.syncMedical(player, this.value);
    }

    private void updateMaxHealth(ServerPlayer player) {
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(20.0 + this.value * 2.0);
        }
    }
}
