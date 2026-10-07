package net.narutoxboruto.capabilities.stats;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class Shurikenjutsu extends AbstractStat {

    public static final Codec<Shurikenjutsu> CODEC = Codec.INT.xmap(Shurikenjutsu::new, Shurikenjutsu::getValue);

    public Shurikenjutsu() {
        this(0);
    }

    public Shurikenjutsu(int value) {
        super(value, DEFAULT_MAX_VALUE);
    }

    @Override
    public void syncValue(ServerPlayer player) {
        Services.PLATFORM.syncShurikenjutsu(player, getValue());
    }
}
