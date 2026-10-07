package net.narutoxboruto.capabilities.stats;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class Speed extends AbstractStat {

    public static final Codec<Speed> CODEC = Codec.INT.xmap(Speed::new, Speed::getValue);

    public Speed() {
        this(0);
    }

    public Speed(int value) {
        super(value, 20);
    }

    @Override
    public void syncValue(ServerPlayer player) {
        Services.PLATFORM.syncSpeed(player, getValue());
    }
}
