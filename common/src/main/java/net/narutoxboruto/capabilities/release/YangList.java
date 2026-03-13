package net.narutoxboruto.capabilities.release;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

public class YangList {

    private final String id;

    protected String value = "";

    public static final Codec<YangList> CODEC = Codec.STRING.xmap(YangList::new, YangList::getValue);

    public YangList(String id, String value) { this.id = id; this.value = value; }
    public YangList(String identifier) { this.id = identifier; this.value = ""; }
    public YangList() { this("yang"); }

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

    public void copyFrom(YangList source, ServerPlayer serverPlayer) {
        this.value = source.getValue();
        this.syncValue(serverPlayer);
    }

    public void resetValue(ServerPlayer serverPlayer) {
        this.value = "";
        this.syncValue(serverPlayer);
    }

    public void syncValue(ServerPlayer serverPlayer) {
        Services.PLATFORM.syncYangList(serverPlayer, this.value);
    }
}
