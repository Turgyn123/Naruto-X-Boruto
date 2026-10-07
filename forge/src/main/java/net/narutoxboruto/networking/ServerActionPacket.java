package net.narutoxboruto.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.main.platform.ForgePlatformHelper;
import net.narutoxboruto.networking.jutsu.JutsuStorageMenu;
import net.narutoxboruto.util.JutsuGrantHelper;
import net.narutoxboruto.util.ServerActions;

public class ServerActionPacket {
    private final String action;

    public ServerActionPacket(String action) {
        this.action = action;
    }

    public static void encode(ServerActionPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.action);
    }

    public static ServerActionPacket decode(FriendlyByteBuf buf) {
        return new ServerActionPacket(buf.readUtf());
    }

    public static void handle(ServerActionPacket msg, CustomPayloadEvent.Context ctx) {
        ServerPlayer serverPlayer = ctx.getSender();
        if (serverPlayer == null) return;

        if ("open_jutsu_storage".equals(msg.action)) {
            JutsuGrantHelper.cleanupDuplicateJutsus(serverPlayer);
            JutsuStorage storage = PlayerDataManager.get(serverPlayer).getJutsuStorage();
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, player) ->
                            new JutsuStorageMenu(containerId, playerInventory, ForgePlatformHelper.toItemStackHandler(storage)),
                    Component.translatable("container.narutoxboruto.jutsu_storage")
            ));
            return;
        }

        // Everything else is shared with the other loaders and validated there.
        ServerActions.handleAction(serverPlayer, msg.action);
    }
}
