package net.narutoxboruto.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.main.Main;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ModHudOverlays {
    private static final ResourceLocation EMPTY_CHAKRA = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/chakra/empty_chakra.png");
    private static final ResourceLocation FULL_CHAKRA = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/chakra/full_chakra.png");

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        // Only render if in game with a player
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        GuiGraphics gui = event.getGuiGraphics();
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        int x = width / 20;
        int y = height - 21; // Moved higher to be more visible
        int chakraWidth = 90;
        int chakraHeight = 5;

        int currentChakra = minecraft.player.getData(NeoForgeCapabilities.CHAKRA.get()).getValue();
        int maxChakra = minecraft.player.getData(NeoForgeCapabilities.MAX_CHAKRA.get()).getValue();

        // Avoid division by zero
        if (maxChakra <= 0) return;

        int filledWidth = (int) (currentChakra / (float) maxChakra * chakraWidth);

        // Render with explicit texture dimensions
        try {
            // Set up the rendering properly
            gui.pose().pushPose();

            // Enable transparency
            gui.setColor(1.0F, 1.0F, 1.0F, 1.0F);

            // Render empty background
            gui.blit(EMPTY_CHAKRA, x, y, 0, 0, chakraWidth, chakraHeight);

            // Render filled portion
            if (filledWidth > 0) {
                gui.blit(FULL_CHAKRA, x, y, 0, 0, filledWidth, chakraHeight);
            }

            gui.pose().popPose();
        } catch (Exception e) {
            // Fallback to colored rectangles
            drawFallbackBars(gui, x, y, chakraWidth, chakraHeight, filledWidth);
        }

        // Draw text (this is working for you)
        String chakraText = currentChakra + "/" + maxChakra;
        var font = minecraft.font;
        int textWidth = font.width(chakraText);
        int textX = x + (chakraWidth - textWidth) / 2;
        int textY = y - 12;
        gui.drawString(font, chakraText, textX, textY, 0x1c7dc6, true);
    }

    private static void drawFallbackBars(GuiGraphics gui, int x, int y, int width, int height, int filledWidth) {
        // Gray background
        gui.fill(x, y, x + width, y + height, 0xFF555555);
        // Green fill
        if (filledWidth > 0) {
            gui.fill(x, y, x + filledWidth, y + height, 0xFF00FF00);
        }
    }
}

   // private static final ResourceLocation WIDGETS = new ResourceLocation("textures/gui/widgets.png");
   //
   // public static final IGuiOverlay SECOND_OFFHAND = ((gui, poseStack, partialTick, width, height) -> {
   //     int halfWidth = width / 2;
   //     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   //     RenderSystem.setShader(GameRenderer::getPositionTexShader);
   //     RenderSystem.setShaderTexture(0, WIDGETS);
   //     drawSecondOffhandOverlay(poseStack, height, halfWidth);
   // });
//
   // private static void drawSecondOffhandOverlay(PoseStack poseStack, int height, int x) {
   //     int size = 256;
   //     LocalPlayer player = Minecraft.getInstance().player;
   //     HumanoidArm humanoidarm = player.getMainArm().getOpposite();
   //     if (PlayerData.getSecondOffhandStack() != ItemStack.EMPTY && !player.isSpectator()) {
   //         if (humanoidarm.equals(HumanoidArm.LEFT)) {
   //             GuiComponent.blit(poseStack, x - 120, height - 40, 24, 23, 22, 20, size, size);
   //             renderSlot(x - 117, height - 37, PlayerData.getSecondOffhandStack());
   //         }
   //         else {
   //             GuiComponent.blit(poseStack, x + 98, height - 40, 60, 23, 22, 20, size, size);
   //             renderSlot(x + 101, height - 37, PlayerData.getSecondOffhandStack());
   //         }
   //     }
   // }

