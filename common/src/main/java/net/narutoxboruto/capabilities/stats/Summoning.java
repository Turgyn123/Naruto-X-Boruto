package net.narutoxboruto.capabilities.stats;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class Summoning extends AbstractStat {

    public static final Codec<Summoning> CODEC = Codec.INT.xmap(Summoning::new, Summoning::getValue);

    public Summoning() {
        this(0);
    }

    public Summoning(int value) {
        super(value, DEFAULT_MAX_VALUE);
    }

    @Override
    public void syncValue(ServerPlayer player) {
        Services.PLATFORM.syncSummoning(player, getValue());
    }
}
