package net.narutoxboruto.capabilities.stats;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class Medical extends AbstractStat {

    public static final Codec<Medical> CODEC = Codec.INT.xmap(Medical::new, Medical::getValue);

    public Medical() {
        this(0);
    }

    public Medical(int value) {
        super(value, DEFAULT_MAX_VALUE);
    }

    @Override
    public void syncValue(ServerPlayer player) {
        Services.PLATFORM.syncMedical(player, getValue());
    }
}
