package net.narutoxboruto.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.client.gui.ShinobiStatsGui;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.items.swords.AbstractAbilitySword;
import net.narutoxboruto.items.throwables.FumaShurikenItem;
import net.narutoxboruto.items.throwables.ThrowableWeaponItem;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.networking.FabricPacketHandler;
import net.narutoxboruto.networking.ServerActionPacket;
import net.narutoxboruto.util.ModKeyBinds;
import org.lwjgl.glfw.GLFW;

public class FabricClientEvents {

    private static boolean cKeyWasPressed = false;
    private static boolean dojutsuToggleWasDown = false;

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

            // Dojutsu activate / deactivate keybind (combat-friendly).
            // Default key X conflicts with vanilla "Load Toolbar Activator", which wins
            // KeyMapping.MAP[X], so consumeClick() never fires. Poll GLFW directly with
            // edge detection. Mapping is still registered so it appears in Controls menu.
            boolean dojutsuToggleDown = InputConstants.isKeyDown(client.getWindow().getWindow(), GLFW.GLFW_KEY_X);
            if (client.screen == null && dojutsuToggleDown && !dojutsuToggleWasDown) {
                Dojutsu dojutsu = Services.PLATFORM.getDojutsu(player);
                if (dojutsu != null) {
                    if (dojutsu.areEyesVisible()) {
                        dojutsu.setEyesVisible(false);
                        Services.PLATFORM.sendEquipDojutsu("hide", "");
                    } else {
                        dojutsu.setEyesVisible(true);
                        Services.PLATFORM.sendEquipDojutsu("show", "");
                    }
                }
            }
            dojutsuToggleWasDown = dojutsuToggleDown;
            // Drain any queued click events so they don't accumulate on the unused mapping.
            //noinspection StatementWithEmptyBody
            while (ModKeyBinds.DOJUTSU_TOGGLE.consumeClick()) { }

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
