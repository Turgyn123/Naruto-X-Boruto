package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.main.platform.Services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReleaseList {
    private final String id;
    protected String value = "";

    public static final Codec<ReleaseList> CODEC = Codec.STRING.xmap(value -> {ReleaseList releaseList = new ReleaseList();releaseList.value = value;return releaseList;}, releaseList -> releaseList.getValue());

    public ReleaseList(String identifier) { this.id = identifier; }
    public ReleaseList() { this("releaseList"); }

    public String getValue() { return value; }

    public void setValue(String value) { this.value = value; }

    public void setValue(String value, Player player) {
        this.value = value;
        if (player instanceof ServerPlayer serverPlayer) {
            this.syncValue(serverPlayer);
        }
    }

    public void syncValue(ServerPlayer serverPlayer) {
        Services.PLATFORM.syncReleaseList(serverPlayer, this.value);
    }

    public void concatList(String newRelease, Player player) {
        if (value.isEmpty()) {
            value = newRelease;
        } else {
            value = value + ", " + newRelease;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            this.syncValue(serverPlayer);
        }
    }

    public void copyFrom(ReleaseList source, Player player) {
        this.value = source.getValue();
        if (player instanceof ServerPlayer serverPlayer) {
            this.syncValue(serverPlayer);
        }
    }

    public void resetValue(Player player) {
        this.value = "";
        if (player instanceof ServerPlayer serverPlayer) {
            this.syncValue(serverPlayer);
        }
    }

    public List<String> getReleasesAsList() {
        if (value == null || value.isEmpty()) return new ArrayList<>();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public boolean isEmpty() { return value == null || value.isEmpty(); }
}
