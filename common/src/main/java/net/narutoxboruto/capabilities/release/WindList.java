package net.narutoxboruto.capabilities.release;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

public class WindList {

    private final String id;

    protected String value = "";

    public static final Codec<WindList> CODEC = Codec.STRING.xmap(WindList::new, WindList::getValue);

    public WindList(String id, String value) { this.id = id; this.value = value; }
    public WindList(String identifier) { this.id = identifier; this.value = ""; }
    public WindList() { this("wind"); }

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

    public void copyFrom(WindList source, ServerPlayer serverPlayer) {
        this.value = source.getValue();
        this.syncValue(serverPlayer);
    }

    public void resetValue(ServerPlayer serverPlayer) {
        this.value = "";
        this.syncValue(serverPlayer);
    }

    public void syncValue(ServerPlayer serverPlayer) {
        Services.PLATFORM.syncWindList(serverPlayer, this.value);
    }
}
