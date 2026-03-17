package net.narutoxboruto.client.overlay;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.main.Main;

public class FabricHudOverlay {

    private static final ResourceLocation EMPTY_CHAKRA = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/chakra/empty_chakra.png");
    private static final ResourceLocation FULL_CHAKRA = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/chakra/full_chakra.png");

    public static void register() {
        HudRenderCallback.EVENT.register((gui, tickCounter) -> {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.player == null || minecraft.options.hideGui) {
                return;
            }

            int width = minecraft.getWindow().getGuiScaledWidth();
            int height = minecraft.getWindow().getGuiScaledHeight();

            int x = width / 20;
            int y = height - 21;
            int chakraWidth = 90;
            int chakraHeight = 5;

            var data = PlayerDataManager.get(minecraft.player);
            int currentChakra = data.getChakra().getValue();
            int maxChakra = data.getMaxChakra().getValue();

            if (maxChakra <= 0) return;

            int filledWidth = (int) (currentChakra / (float) maxChakra * chakraWidth);

            try {
                gui.pose().pushPose();
                gui.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                gui.blit(EMPTY_CHAKRA, x, y, 0, 0, chakraWidth, chakraHeight);
                if (filledWidth > 0) {
                    gui.blit(FULL_CHAKRA, x, y, 0, 0, filledWidth, chakraHeight);
                }
                gui.pose().popPose();
            } catch (Exception e) {
                gui.fill(x, y, x + chakraWidth, y + chakraHeight, 0xFF555555);
                if (filledWidth > 0) {
                    gui.fill(x, y, x + filledWidth, y + chakraHeight, 0xFF00FF00);
                }
            }

            String chakraText = currentChakra + "/" + maxChakra;
            var font = minecraft.font;
            int textWidth = font.width(chakraText);
            int textX = x + (chakraWidth - textWidth) / 2;
            int textY = y - 12;
            gui.drawString(font, chakraText, textX, textY, 0x1c7dc6, true);
        });
    }
}
