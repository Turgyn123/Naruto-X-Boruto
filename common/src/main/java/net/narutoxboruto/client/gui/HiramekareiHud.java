package net.narutoxboruto.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.items.swords.Hiramekarei;

/**
 * The chakra stored in the Hiramekarei, always shown in white above the hotbar while it is held. Drawn by the HUD overlay
 * of each loader.
 */
public class HiramekareiHud {

    /** Just above the hotbar and the health and food rows. */
    private static final int ABOVE_BOTTOM = 50;

    public static void render(GuiGraphics gui, Minecraft minecraft) {
        if (minecraft.player == null) return;
        ItemStack held = minecraft.player.getMainHandItem();
        if (!(held.getItem() instanceof Hiramekarei blade) || Hiramekarei.isOffhandBlade(held)) return;

        int stored = blade.getStored(held);
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        Component text = Component.translatable("hiramekarei.stored", stored, Hiramekarei.MAX_STORED);
        gui.drawCenteredString(minecraft.font, text, width / 2, height - ABOVE_BOTTOM, 0xFFFFFF);
    }
}
