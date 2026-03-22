package net.narutoxboruto.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.client.PlayerData;

public class SyncNbtData {
    private final String key;
    private final CompoundTag nbt;

    public SyncNbtData(String key, CompoundTag nbt) {
        this.key = key;
        this.nbt = nbt;
    }

    public static void encode(SyncNbtData msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.key);
        buf.writeNbt(msg.nbt);
    }

    public static SyncNbtData decode(FriendlyByteBuf buf) {
        return new SyncNbtData(buf.readUtf(), buf.readNbt());
    }

    public static void handle(SyncNbtData msg, CustomPayloadEvent.Context ctx) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if ("jutsu_storage".equals(msg.key)) {
                PlayerDataManager.get(player).setJutsuStorage(JutsuStorage.fromNbt(msg.nbt));
            } else if ("dojutsu".equals(msg.key)) {
                Dojutsu dojutsu = Dojutsu.fromNbt(msg.nbt);
                PlayerDataManager.get(player).setDojutsu(dojutsu);
                PlayerData.setDojutsuUnlockedList(dojutsu.getUnlockedListRaw());
                PlayerData.setDojutsuLeftEye(dojutsu.getLeftEye());
                PlayerData.setDojutsuRightEye(dojutsu.getRightEye());
                PlayerData.setDojutsuTimer(dojutsu.getTimer());
            }
        }
    }
}
