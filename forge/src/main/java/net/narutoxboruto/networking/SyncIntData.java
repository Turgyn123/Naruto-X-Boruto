package net.narutoxboruto.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.narutoxboruto.capabilities.PlayerDataManager;

public class SyncIntData {
    private final String key;
    private final int value;

    public SyncIntData(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public static void encode(SyncIntData msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.key);
        buf.writeInt(msg.value);
    }

    public static SyncIntData decode(FriendlyByteBuf buf) {
        return new SyncIntData(buf.readUtf(), buf.readInt());
    }

    public static void handle(SyncIntData msg, CustomPayloadEvent.Context ctx) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            PlayerDataManager.get(player).setIntValue(msg.key, msg.value);
        }
    }
}
