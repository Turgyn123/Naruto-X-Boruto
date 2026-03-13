package net.narutoxboruto.capabilities.release;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

public class FireList {

    private final String id;

    protected String value = "";

    public static final Codec<FireList> CODEC = Codec.STRING.xmap(FireList::new, FireList::getValue);

    public FireList(String id, String value) { this.id = id; this.value = value; }
    public FireList(String identifier) { this.id = identifier; this.value = ""; }
    public FireList() { this("fire"); }

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

    public void copyFrom(FireList source, ServerPlayer serverPlayer) {
        this.value = source.getValue();
        this.syncValue(serverPlayer);
    }

    public void resetValue(ServerPlayer serverPlayer) {
        this.value = "";
        this.syncValue(serverPlayer);
    }

    public void syncValue(ServerPlayer serverPlayer) {
        Services.PLATFORM.syncFireList(serverPlayer, this.value);
    }
}