package net.narutoxboruto.capabilities.stats;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class Taijutsu extends AbstractStat {

    public static final Codec<Taijutsu> CODEC = Codec.INT.xmap(Taijutsu::new, Taijutsu::getValue);

    public Taijutsu() {
        this(0);
    }

    public Taijutsu(int value) {
        super(value, DEFAULT_MAX_VALUE);
    }

    @Override
    public void syncValue(ServerPlayer player) {
        Services.PLATFORM.syncTaijutsu(player, getValue());
    }
}
