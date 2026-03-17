package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

public class Affiliation {
    private String value;

    public static final Codec<Affiliation> CODEC = Codec.STRING.xmap(Affiliation::new, Affiliation::getValue);

    public Affiliation() { this.value = "affiliation"; }
    public Affiliation(String value) { this.value = value; }

    public String getValue() { return value; }

    public void setValue(String value) { this.value = value; }

    public void setValue(String value, ServerPlayer serverPlayer) {
        this.value = value;
        Services.PLATFORM.syncAffiliation(serverPlayer, this.value);
    }

    public void concatList(String value, ServerPlayer serverPlayer) {
        this.value = ModUtil.concatAndFormat(this.value, value);
        Services.PLATFORM.syncAffiliation(serverPlayer, this.value);
    }

    public void syncValue(ServerPlayer serverPlayer) {
        Services.PLATFORM.syncAffiliation(serverPlayer, this.value);
    }
}
