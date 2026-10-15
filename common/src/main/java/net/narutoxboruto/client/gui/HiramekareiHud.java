package net.narutoxboruto.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.items.swords.Hiramekarei;

/**
 * The chakra stored in the Hiramekarei, always shown above the hotbar while it is held. Drawn by the HUD overlay
 * of each loader.
 */
public class HiramekareiHud {

    private static final int BAR_WIDTH = 80;
    private static final int BAR_HEIGHT = 4;
    /** Above the action bar messages, which sit just over the hotbar. */
    private static final int ABOVE_BOTTOM = 80;

    public static void render(GuiGraphics gui, Minecraft minecraft) {
        if (minecraft.player == null) return;
        ItemStack held = minecraft.player.getMainHandItem();
        if (!(held.getItem() instanceof Hiramekarei blade) || Hiramekarei.isOffhandBlade(held)) return;

        int stored = blade.getStored(held);
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        Component text = Component.translatable("hiramekarei.stored", stored, Hiramekarei.MAX_STORED);
        int textY = height - ABOVE_BOTTOM;
        gui.drawCenteredString(minecraft.font, text, width / 2, textY, 0x55FFFF);

        int x = (width - BAR_WIDTH) / 2;
        int y = textY + minecraft.font.lineHeight + 2;
        int filled = (int) (BAR_WIDTH * stored / (float) Hiramekarei.MAX_STORED);
        gui.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xAA000000);
        gui.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF1B3A4B);
        if (filled > 0) {
            gui.fill(x, y, x + filled, y + BAR_HEIGHT, 0xFF55D8FF);
        }
    }
}
