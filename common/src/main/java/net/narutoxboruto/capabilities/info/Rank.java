package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

public class Rank {
    private String value;

    public static final Codec<Rank> CODEC = Codec.STRING.xmap(Rank::new, Rank::getValue);

    public Rank() { this.value = "rank"; }
    public Rank(String value) { this.value = value; }

    public String getValue() { return value; }

    public void setValue(String value) { this.value = value; }

    public void setValue(String value, ServerPlayer serverPlayer) {
        this.value = value;
        this.syncValue(serverPlayer);
    }

    public void concatList(String value, ServerPlayer serverPlayer) {
        this.value = ModUtil.concatAndFormat(this.value, value);
        this.syncValue(serverPlayer);
    }

    public void syncValue(ServerPlayer serverPlayer) {
        Services.PLATFORM.syncRank(serverPlayer, this.value);
    }
}
