package net.narutoxboruto.capabilities.release;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

public class YinList {

    private final String id;

    protected String value = "";

    public static final Codec<YinList> CODEC = Codec.STRING.xmap(YinList::new, YinList::getValue);

    public YinList(String id, String value) { this.id = id; this.value = value; }
    public YinList(String identifier) { this.id = identifier; this.value = ""; }
    public YinList() { this("yin"); }

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

    public void copyFrom(YinList source, ServerPlayer serverPlayer) {
        this.value = source.getValue();
        this.syncValue(serverPlayer);
    }

    public void resetValue(ServerPlayer serverPlayer) {
        this.value = "";
        this.syncValue(serverPlayer);
    }

    public void syncValue(ServerPlayer serverPlayer) {
        Services.PLATFORM.syncYinList(serverPlayer, this.value);
    }
}
