package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class Chakra {
    private int value;

    public static final Codec<Chakra> CODEC = Codec.INT.xmap(Chakra::new, Chakra::getValue);

    public Chakra() { this.value = 0; }
    public Chakra(int value) { this.value = value; }

    public int getValue() { return value; }

    public void setValue(int value) { this.value = value; }

    public void addValue(int add, ServerPlayer serverPlayer) {
        MaxChakra maxChakra = Services.PLATFORM.getMaxChakra(serverPlayer);
        this.value = Math.min(this.value + add, maxChakra.getValue());
        this.syncValue(serverPlayer);
    }

    public void subValue(int sub, ServerPlayer serverPlayer) {
        this.value = Math.max(this.value - sub, 0);
        this.syncValue(serverPlayer);
    }

    public void syncValue(ServerPlayer serverPlayer) {
        Services.PLATFORM.setChakra(serverPlayer, this);
        Services.PLATFORM.syncChakra(serverPlayer, this.value);
    }

    public void reset(ServerPlayer serverPlayer) {
        MaxChakra maxChakra = Services.PLATFORM.getMaxChakra(serverPlayer);
        this.value = maxChakra.getValue() / 2;
        this.syncValue(serverPlayer);
    }

    public void replenish(ServerPlayer serverPlayer) {
        MaxChakra maxChakra = Services.PLATFORM.getMaxChakra(serverPlayer);
        this.value = maxChakra.getValue();
        this.syncValue(serverPlayer);
    }
}
