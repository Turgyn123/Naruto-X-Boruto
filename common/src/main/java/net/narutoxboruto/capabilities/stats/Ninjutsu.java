package net.narutoxboruto.capabilities.stats;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.capabilities.info.MaxChakra;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

/** Every Ninjutsu point also raises the player's maximum chakra. */
public class Ninjutsu extends AbstractStat {

    private static final int CHAKRA_PER_POINT = 5;

    public static final Codec<Ninjutsu> CODEC = Codec.INT.xmap(Ninjutsu::new, Ninjutsu::getValue);

    public Ninjutsu() {
        this(0);
    }

    public Ninjutsu(int value) {
        super(value, DEFAULT_MAX_VALUE);
    }

    @Override
    public void syncValue(ServerPlayer player) {
        Services.PLATFORM.syncNinjutsu(player, getValue());
    }

    /** Only real gains grow max chakra, so a stat at its cap can't be farmed for chakra. */
    @Override
    protected void onChanged(ServerPlayer player, int delta) {
        if (delta > 0) {
            MaxChakra maxChakra = Services.PLATFORM.getMaxChakra(player);
            maxChakra.addValue(delta * CHAKRA_PER_POINT * ModUtil.getChakraGrowthMultiplier(player), player);
        }
    }

    /** Lowering Ninjutsu takes back the max chakra it gave. Setting it directly (admin commands) does not. */
    @Override
    public int subValue(int amount, ServerPlayer player) {
        int delta = super.subValue(amount, player);
        if (delta < 0) {
            MaxChakra maxChakra = Services.PLATFORM.getMaxChakra(player);
            maxChakra.subValue(-delta * CHAKRA_PER_POINT * ModUtil.getChakraGrowthMultiplier(player), player);
            ModUtil.capChakraToMax(player);
        }
        return delta;
    }
}
