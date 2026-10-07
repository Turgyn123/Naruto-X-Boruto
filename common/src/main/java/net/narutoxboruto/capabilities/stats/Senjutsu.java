package net.narutoxboruto.capabilities.stats;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class Senjutsu extends AbstractStat {

    public static final Codec<Senjutsu> CODEC = Codec.INT.xmap(Senjutsu::new, Senjutsu::getValue);

    public Senjutsu() {
        this(0);
    }

    public Senjutsu(int value) {
        super(value, DEFAULT_MAX_VALUE);
    }

    @Override
    public void syncValue(ServerPlayer player) {
        Services.PLATFORM.syncSenjutsu(player, getValue());
    }
}
