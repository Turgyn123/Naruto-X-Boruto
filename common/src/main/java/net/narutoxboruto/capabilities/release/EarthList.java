package net.narutoxboruto.capabilities.release;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

public class EarthList {

    private final String id;

    protected String value = "";

    public static final Codec<EarthList> CODEC = Codec.STRING.xmap(EarthList::new, EarthList::getValue);

    public EarthList(String id, String value) { this.id = id; this.value = value; }
    public EarthList(String identifier) { this.id = identifier; this.value = ""; }
    public EarthList() { this("earth"); }

    public String getValue() { return value; }

    public String getId() { return id; }

    public void setValue(String value) { this.value = value; }

    public void setValue(String value, ServerPlayer serverPlayer) {
        this.value = value;
        this.syncValue(serverPlayer);
    }

    public void concatList(String value, ServerPlayer serverPlayer) {
        this.value = ModUtil.concatAndFormat(this.value, value);
        this.syncValue(serverPlayer);
    }

    public void copyFrom(EarthList source, ServerPlayer serverPlayer) {
        this.value = source.getValue();
        this.syncValue(serverPlayer);
    }

    public void resetValue(ServerPlayer serverPlayer) {
        this.value = "";
        this.syncValue(serverPlayer);
    }

    public void syncValue(ServerPlayer serverPlayer) {
        Services.PLATFORM.syncEarthList(serverPlayer, this.value);
    }
}
