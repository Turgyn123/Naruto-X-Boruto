package net.narutoxboruto.capabilities.stats;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class Kenjutsu extends AbstractStat {

    public static final Codec<Kenjutsu> CODEC = Codec.INT.xmap(Kenjutsu::new, Kenjutsu::getValue);

    public Kenjutsu() {
        this(0);
    }

    public Kenjutsu(int value) {
        super(value, DEFAULT_MAX_VALUE);
    }

    @Override
    public void syncValue(ServerPlayer player) {
        Services.PLATFORM.syncKenjutsu(player, getValue());
    }
}
