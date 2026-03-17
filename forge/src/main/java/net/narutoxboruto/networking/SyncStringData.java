package net.narutoxboruto.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.narutoxboruto.capabilities.PlayerDataManager;

public class SyncStringData {
    private final String key;
    private final String value;

    public SyncStringData(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static void encode(SyncStringData msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.key);
        buf.writeUtf(msg.value);
    }

    public static SyncStringData decode(FriendlyByteBuf buf) {
        return new SyncStringData(buf.readUtf(), buf.readUtf());
    }

    public static void handle(SyncStringData msg, CustomPayloadEvent.Context ctx) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            PlayerDataManager.get(player).setStringValue(msg.key, msg.value);
        }
    }
}
