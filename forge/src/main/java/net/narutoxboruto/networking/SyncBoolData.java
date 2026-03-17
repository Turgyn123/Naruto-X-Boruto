package net.narutoxboruto.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.client.PlayerData;

public class SyncBoolData {
    private final String key;
    private final boolean value;

    public SyncBoolData(String key, boolean value) {
        this.key = key;
        this.value = value;
    }

    public static void encode(SyncBoolData msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.key);
        buf.writeBoolean(msg.value);
    }

    public static SyncBoolData decode(FriendlyByteBuf buf) {
        return new SyncBoolData(buf.readUtf(), buf.readBoolean());
    }

    public static void handle(SyncBoolData msg, CustomPayloadEvent.Context ctx) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            PlayerDataManager.get(player).setBoolValue(msg.key, msg.value);
            switch (msg.key) {
                case "kiba_active" -> PlayerData.setKibaActive(msg.value);
                case "lightning_chakra_mode_active" -> PlayerData.setLightningChakraModeActive(msg.value);
                case "chakra_control" -> PlayerData.setChakraControlActive(msg.value);
            }
        }
    }
}
