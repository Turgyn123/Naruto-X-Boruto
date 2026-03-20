package net.narutoxboruto.capabilities.stats;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.capabilities.info.MaxChakra;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

public class Ninjutsu {
    private int value;
    private static final int MAX_VALUE = 500;

    public static final Codec<Ninjutsu> CODEC = Codec.INT.xmap(Ninjutsu::new, Ninjutsu::getValue);

    public Ninjutsu() { this.value = 0; }
    public Ninjutsu(int value) { this.value = value; }

    public int getValue() { return value; }

    public void setValue(int value) { this.value = value; }

    public void setValue(int value, ServerPlayer player) {
        int oldValue = this.value;
        this.value = Math.min(value, MAX_VALUE);
        int actualAdd = this.value - oldValue;
        if (actualAdd > 0) {
            growMaxChakra(actualAdd, player);
        }
    }

    public void addValue(int amount, ServerPlayer player) {
        int oldValue = this.value;
        this.value = Math.min(this.value + amount, MAX_VALUE);
        int actualAdd = this.value - oldValue;
        if (actualAdd > 0) {
            growMaxChakra(actualAdd, player);
        }
    }

    public void subValue(int amount, ServerPlayer player) {
        int oldValue = this.value;
        this.value = Math.max(this.value - amount, 0);
        int pointsRemoved = oldValue - this.value;
        if (pointsRemoved > 0) {
            MaxChakra maxChakra = Services.PLATFORM.getMaxChakra(player);
            int multiplier = ModUtil.getChakraGrowthMultiplier(player);
            maxChakra.subValue(pointsRemoved * 5 * multiplier, player);
            ModUtil.capChakraToMax(player);
        }
    }

    public void incrementValue(int amount, ServerPlayer player) {
        int oldValue = this.value;
        this.value = Math.min(this.value + amount, MAX_VALUE);
        int actualAdd = this.value - oldValue;
        if (actualAdd > 0) {
            growMaxChakra(actualAdd, player);
        }
        this.syncValue(player);
    }

    public void syncValue(ServerPlayer player) {
        Services.PLATFORM.syncNinjutsu(player, this.value);
    }

    private void growMaxChakra(int ninjutsuGain, ServerPlayer player) {
        MaxChakra maxChakra = Services.PLATFORM.getMaxChakra(player);
        int multiplier = ModUtil.getChakraGrowthMultiplier(player);
        maxChakra.addValue(ninjutsuGain * 5 * multiplier, player);
    }
}
