package net.narutoxboruto.capabilities.stats;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class Genjutsu extends AbstractStat {

    public static final Codec<Genjutsu> CODEC = Codec.INT.xmap(Genjutsu::new, Genjutsu::getValue);

    public Genjutsu() {
        this(0);
    }

    public Genjutsu(int value) {
        super(value, DEFAULT_MAX_VALUE);
    }

    @Override
    public void syncValue(ServerPlayer player) {
        Services.PLATFORM.syncGenjutsu(player, getValue());
    }
}
