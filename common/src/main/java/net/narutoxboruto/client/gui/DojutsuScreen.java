package net.narutoxboruto.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.main.platform.Services;

import java.util.ArrayList;
import java.util.List;

public class DojutsuScreen extends Screen {

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/gui/shinobi_stats.png");

    private static final int PANEL_WIDTH = 260;
    private static final int PANEL_HEIGHT = 192;

    // Eye selection column centers (1/4 and 3/4 of panel)
    private static final int LEFT_COL = 64;
    private static final int RIGHT_COL = 192;

    private final LocalPlayer player;

    // Tracks which arrow is hovered (null if none)
    private String hoveredArrow = null;

    public DojutsuScreen() {
        super(Component.translatable("gui.narutoxboruto.dojutsu_menu"));
        this.player = Minecraft.getInstance().player;
    }

    private Dojutsu getDojutsu() {
        return Services.PLATFORM.getDojutsu(player);
    }

    private String getClan() {
        return Services.PLATFORM.getClan(player).getValue();
    }

    @Override
    protected void init() {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        // Done button (compact, at very bottom of panel)
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .bounds(left + 98, top + 176, 60, 14).build());

        super.init();
    }

    private void cycleEye(String slot, int direction) {
        Dojutsu dojutsu = getDojutsu();
        List<String> unlocked = dojutsu.getUnlockedList();
        if (unlocked.isEmpty()) return;

        List<String> options = new ArrayList<>();
        options.add("");
        options.addAll(unlocked);

        String current = "left".equals(slot) ? dojutsu.getLeftEye() : dojutsu.getRightEye();
        if (current == null) current = "";

        int idx = options.indexOf(current);
        if (idx < 0) idx = 0;
        idx = (idx + direction + options.size()) % options.size();

        String newType = options.get(idx);
        Services.PLATFORM.sendEquipDojutsu(slot, newType);
    }

    private void drawCenteredNoShadow(GuiGraphics guiGraphics, Component text, int centerX, int y, int color) {
        int w = this.font.width(text);
        guiGraphics.drawString(this.font, text, centerX - w / 2, y, color, false);
    }

    // --- Arrow hit detection ---

    private String getArrowAt(double mouseX, double mouseY) {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        int arrowY = top + 150;
        int arrowH = 9;
        int arrowW = 8;

        int leftPrevX = left + LEFT_COL - 38;
        int leftNextX = left + LEFT_COL + 30;
        int rightPrevX = left + RIGHT_COL - 38;
        int rightNextX = left + RIGHT_COL + 30;

        if (mouseY >= arrowY && mouseY < arrowY + arrowH) {
            if (mouseX >= leftPrevX && mouseX < leftPrevX + arrowW) return "left_prev";
            if (mouseX >= leftNextX && mouseX < leftNextX + arrowW) return "left_next";
            if (mouseX >= rightPrevX && mouseX < rightPrevX + arrowW) return "right_prev";
            if (mouseX >= rightNextX && mouseX < rightNextX + arrowW) return "right_next";
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            String arrow = getArrowAt(mouseX, mouseY);
            if (arrow != null) {
                switch (arrow) {
                    case "left_prev" -> cycleEye("left", -1);
                    case "left_next" -> cycleEye("left", 1);
                    case "right_prev" -> cycleEye("right", -1);
                    case "right_next" -> cycleEye("right", 1);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        // Draw background panel
        guiGraphics.blit(BACKGROUND, left, top, -10, 5, 256, 256, 256, 256);

        Dojutsu dojutsu = getDojutsu();
        List<String> unlockedList = dojutsu.getUnlockedList();

        // Title
        drawCenteredNoShadow(guiGraphics,
                Component.translatable("gui.narutoxboruto.dojutsu_menu"),
                left + 128, top + 6, 0x404040);

        // --- Timer / status ---
        String clan = getClan();
        String clanDojutsu = Dojutsu.getDojutsuForClan(clan);
        boolean eligible = Dojutsu.isClanEligible(clan);

        if (eligible && clanDojutsu != null) {
            int timer = dojutsu.getTimer();
            boolean alreadyHas = unlockedList.contains(clanDojutsu);

            if (alreadyHas) {
                drawCenteredNoShadow(guiGraphics,
                        Component.translatable("dojutsu.already_obtained",
                                Component.translatable("dojutsu." + clanDojutsu)),
                        left + 128, top + 16, 0x226622);
            } else if (unlockedList.size() >= Dojutsu.MAX_DOJUTSU) {
                drawCenteredNoShadow(guiGraphics,
                        Component.translatable("dojutsu.max_reached"),
                        left + 128, top + 16, 0x662222);
            } else {
                int remaining = Math.max(0, Dojutsu.REQUIRED_TICKS - timer);
                int totalSeconds = remaining / 20;
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;
                drawCenteredNoShadow(guiGraphics,
                        Component.translatable("dojutsu.timer_line1",
                                Component.translatable("dojutsu." + clanDojutsu)),
                        left + 128, top + 16, 0x664400);
                drawCenteredNoShadow(guiGraphics,
                        Component.translatable("dojutsu.timer_line2",
                                String.format("%dm %02ds", minutes, seconds)),
                        left + 128, top + 26, 0x664400);
            }
        } else {
            drawCenteredNoShadow(guiGraphics,
                    Component.translatable("dojutsu.not_eligible"),
                    left + 128, top + 16, 0x555555);
        }

        // --- Unlocked dojutsu with count ---
        Component unlockedLabel = Component.translatable("dojutsu.unlocked")
                .append(Component.literal(" (" + unlockedList.size() + "/" + Dojutsu.MAX_DOJUTSU + "): "));
        int labelWidth = this.font.width(unlockedLabel);
        int totalIconWidth = unlockedList.isEmpty() ? 0 : unlockedList.size() * 14;
        int noneWidth = unlockedList.isEmpty() ? this.font.width(Component.translatable("shinobiStat.dojutsu_none")) : 0;
        int totalWidth = labelWidth + Math.max(totalIconWidth, noneWidth);
        int unlockedStartX = left + 128 - totalWidth / 2;

        guiGraphics.drawString(this.font, unlockedLabel,
                unlockedStartX, top + 36, 0x404040, false);

        int iconStartX = unlockedStartX + labelWidth;
        if (!unlockedList.isEmpty()) {
            for (int i = 0; i < unlockedList.size(); i++) {
                String dj = unlockedList.get(i);
                if (Dojutsu.DOJUTSU_ICON.containsKey(dj)) {
                    ResourceLocation icon = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                            "textures/dojutsu/icons/" + Dojutsu.DOJUTSU_ICON.get(dj) + ".png");
                    guiGraphics.blit(icon, iconStartX + i * 14, top + 34,
                            11, 11, 0.0F, 0.0F, 32, 32, 32, 32);
                }
            }
        } else {
            guiGraphics.drawString(this.font, Component.translatable("shinobiStat.dojutsu_none"),
                    iconStartX, top + 36, 0x888888, false);
        }

        // --- Character model display (face close-up, static pose) ---
        // Positive yOffset shifts entity down so the head/face is in the scissor area
        int entityLeft = left + 88;
        int entityTop = top + 46;
        int entityRight = left + 168;
        int entityBottom = top + 110;
        int lookX = (entityLeft + entityRight) / 2;
        int lookY = entityTop + 10;
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics, entityLeft, entityTop, entityRight, entityBottom,
                110, 0.72F, lookX, lookY, player);

        // Draw equipped dojutsu icons on the character face
        String le = dojutsu.getLeftEye();
        String re = dojutsu.getRightEye();
        boolean hasLeft = le != null && !le.isEmpty() && Dojutsu.DOJUTSU_ICON.containsKey(le);
        boolean hasRight = re != null && !re.isEmpty() && Dojutsu.DOJUTSU_ICON.containsKey(re);

        if (hasLeft || hasRight) {
            int faceX = (entityLeft + entityRight) / 2;
            int faceY = (entityTop + entityBottom) / 2;
            if (hasLeft) {
                ResourceLocation leftIcon = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                        "textures/dojutsu/icons/" + Dojutsu.DOJUTSU_ICON.get(le) + ".png");
                guiGraphics.blit(leftIcon, faceX - 14, faceY - 6, 11, 11, 0.0F, 0.0F, 32, 32, 32, 32);
            }
            if (hasRight) {
                ResourceLocation rightIcon = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                        "textures/dojutsu/icons/" + Dojutsu.DOJUTSU_ICON.get(re) + ".png");
                guiGraphics.blit(rightIcon, faceX + 3, faceY - 6, 11, 11, 0.0F, 0.0F, 32, 32, 32, 32);
            }
        }

        // --- Eye selection labels ---
        drawCenteredNoShadow(guiGraphics,
                Component.translatable("dojutsu.left_eye"), left + LEFT_COL, top + 122, 0x404040);
        drawCenteredNoShadow(guiGraphics,
                Component.translatable("dojutsu.right_eye"), left + RIGHT_COL, top + 122, 0x404040);

        // --- Eye type names with ◀ ▶ arrows (hover-highlighted) ---
        hoveredArrow = getArrowAt(mouseX, mouseY);
        int arrowY = top + 150;

        // Left eye selector: ◀ name ▶
        String leftEye = dojutsu.getLeftEye();
        Component leftLabel = (leftEye == null || leftEye.isEmpty())
                ? Component.literal("None")
                : Component.translatable("dojutsu." + leftEye);
        drawCenteredNoShadow(guiGraphics, leftLabel, left + LEFT_COL, arrowY, 0x226622);
        if (leftEye != null && !leftEye.isEmpty() && Dojutsu.DOJUTSU_ICON.containsKey(leftEye)) {
            ResourceLocation leftIcon = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                    "textures/dojutsu/icons/" + Dojutsu.DOJUTSU_ICON.get(leftEye) + ".png");
            guiGraphics.blit(leftIcon, left + LEFT_COL - 5, top + 135, 9, 9, 0.0F, 0.0F, 32, 32, 32, 32);
        }
        int leftPrevColor = "left_prev".equals(hoveredArrow) ? 0xFFFFFF : 0x606060;
        int leftNextColor = "left_next".equals(hoveredArrow) ? 0xFFFFFF : 0x606060;
        guiGraphics.drawString(this.font, "\u25C0", left + LEFT_COL - 38, arrowY, leftPrevColor, false);
        guiGraphics.drawString(this.font, "\u25B6", left + LEFT_COL + 30, arrowY, leftNextColor, false);

        // Right eye selector: ◀ name ▶
        String rightEye = dojutsu.getRightEye();
        Component rightLabel = (rightEye == null || rightEye.isEmpty())
                ? Component.literal("None")
                : Component.translatable("dojutsu." + rightEye);
        drawCenteredNoShadow(guiGraphics, rightLabel, left + RIGHT_COL, arrowY, 0x226622);
        if (rightEye != null && !rightEye.isEmpty() && Dojutsu.DOJUTSU_ICON.containsKey(rightEye)) {
            ResourceLocation rightIcon = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                    "textures/dojutsu/icons/" + Dojutsu.DOJUTSU_ICON.get(rightEye) + ".png");
            guiGraphics.blit(rightIcon, left + RIGHT_COL - 5, top + 135, 9, 9, 0.0F, 0.0F, 32, 32, 32, 32);
        }
        int rightPrevColor = "right_prev".equals(hoveredArrow) ? 0xFFFFFF : 0x606060;
        int rightNextColor = "right_next".equals(hoveredArrow) ? 0xFFFFFF : 0x606060;
        guiGraphics.drawString(this.font, "\u25C0", left + RIGHT_COL - 38, arrowY, rightPrevColor, false);
        guiGraphics.drawString(this.font, "\u25B6", left + RIGHT_COL + 30, arrowY, rightNextColor, false);


    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
