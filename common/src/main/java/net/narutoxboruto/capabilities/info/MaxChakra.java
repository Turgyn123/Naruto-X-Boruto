package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class MaxChakra {
    private int value;

    public static final Codec<MaxChakra> CODEC = Codec.INT.xmap(MaxChakra::new, MaxChakra::getValue);

    public MaxChakra() { this.value = 10; }
    public MaxChakra(int value) { this.value = value; }

    public int getValue() { return value; }

    public void setValue(int value) { this.value = value; }

    public void addValue(int add, ServerPlayer serverPlayer) {
        this.value = this.value + add;
        this.syncValue(serverPlayer);
    }

    public void subValue(int sub, ServerPlayer serverPlayer) {
        this.value = Math.max(this.value - sub, 0);
        this.syncValue(serverPlayer);
    }

    public void syncValue(ServerPlayer serverPlayer) {
        Services.PLATFORM.setMaxChakra(serverPlayer, this);
        Services.PLATFORM.syncMaxChakra(serverPlayer, this.value);
    }

    public void copyFrom(MaxChakra source, ServerPlayer serverPlayer) {
        this.value = source.getValue();
        this.syncValue(serverPlayer);
    }
}
