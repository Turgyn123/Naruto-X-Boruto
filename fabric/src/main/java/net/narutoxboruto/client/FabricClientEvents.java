package net.narutoxboruto.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.client.gui.ShinobiStatsGui;
import net.narutoxboruto.items.swords.AbstractAbilitySword;
import net.narutoxboruto.items.throwables.FumaShurikenItem;
import net.narutoxboruto.items.throwables.ThrowableWeaponItem;
import net.narutoxboruto.networking.FabricPacketHandler;
import net.narutoxboruto.networking.ServerActionPacket;
import net.narutoxboruto.util.ModKeyBinds;

public class FabricClientEvents {

    private static boolean cKeyWasPressed = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LocalPlayer player = client.player;
            if (player == null) return;

            // Open GUI keybind
            if (ModKeyBinds.OPEN_GUI.consumeClick()) {
                client.setScreen(new ShinobiStatsGui());
            }

            // Dojutsu menu keybind
            if (ModKeyBinds.DOJUTSU_MENU.consumeClick()) {
                client.setScreen(new net.narutoxboruto.client.gui.DojutsuScreen());
            }

            // Jutsu storage keybind
            if (client.screen == null && ModKeyBinds.JUTSU_STORAGE.consumeClick()) {
                FabricPacketHandler.sendToServer(new ServerActionPacket("open_jutsu_storage"));
            }

            // Special action keybind
            if (ModKeyBinds.SPECIAL_ACTION.consumeClick()) {
                ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (itemStack.getItem() instanceof AbstractAbilitySword) {
                    FabricPacketHandler.sendToServer(new ServerActionPacket("toggle_sword_ability"));
                } else if (itemStack.getItem() instanceof ThrowableWeaponItem &&
                        !(itemStack.getItem() instanceof FumaShurikenItem)) {
                    FabricPacketHandler.sendToServer(new ServerActionPacket("special_throw"));
                }
            }

            // Chakra control / recharge keybind (both use C key - combined here because
            // Fabric's vanilla KeyMapping only tracks one mapping per key in the internal MAP)
            boolean cKeyPressed = ModKeyBinds.CHAKRA_CONTROL.isDown();
            if (cKeyPressed) {
                if (player.isCrouching()) {
                    // Hold to charge: send packet every tick while Shift+C is held
                    FabricPacketHandler.sendToServer(new ServerActionPacket("recharge_chakra"));
                } else if (!cKeyWasPressed) {
                    // Toggle: only on rising edge when not crouching
                    FabricPacketHandler.sendToServer(new ServerActionPacket("toggle_chakra_control"));
                }
            }
            cKeyWasPressed = cKeyPressed;
        });
    }
}
