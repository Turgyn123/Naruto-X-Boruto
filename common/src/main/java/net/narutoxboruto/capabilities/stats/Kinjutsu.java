package net.narutoxboruto.capabilities.stats;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class Kinjutsu extends AbstractStat {

    public static final Codec<Kinjutsu> CODEC = Codec.INT.xmap(Kinjutsu::new, Kinjutsu::getValue);

    public Kinjutsu() {
        this(0);
    }

    public Kinjutsu(int value) {
        super(value, DEFAULT_MAX_VALUE);
    }

    @Override
    public void syncValue(ServerPlayer player) {
        Services.PLATFORM.syncKinjutsu(player, getValue());
    }
}
