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
    private static final int PANEL_HEIGHT = 240;

    // Eye selection column centers (1/4 and 3/4 of panel)
    private static final int LEFT_COL = 70;
    private static final int RIGHT_COL = 190;

    private final LocalPlayer player;

    // Tracks which arrow is hovered (null if none)
    private String hoveredArrow = null;

    // Dragging state for eye positioning
    private String draggingEye = null; // "left" or "right" or null
    private int dragStartOffsetX, dragStartOffsetY;
    private double dragStartMouseX, dragStartMouseY;

    // Tooltip for ? button
    private boolean hoveringHelp = false;

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

        // Done button (centered in footer row) — only standard MC button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .bounds(left + 100, top + 220, 60, 20).build());

        super.init();
    }

    // --- Custom clickable control bounds ---
    private static final int CONTROLS_Y_OFFSET = 130;
    private static final int CTRL_H = 12;

    private record ClickArea(int x, int y, int w, int h) {
        boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private ClickArea getMinusArea() {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        return new ClickArea(left + 56, top + CONTROLS_Y_OFFSET, 12, CTRL_H);
    }

    private ClickArea getPlusArea() {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        return new ClickArea(left + 114, top + CONTROLS_Y_OFFSET, 12, CTRL_H);
    }

    private ClickArea getResetArea() {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        return new ClickArea(left + 132, top + CONTROLS_Y_OFFSET, this.font.width("[Reset]"), CTRL_H);
    }

    private ClickArea getHideShowArea() {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        Dojutsu dojutsu = getDojutsu();
        String label = dojutsu.areEyesVisible() ? "[Hide]" : "[Show]";
        return new ClickArea(left + 178, top + CONTROLS_Y_OFFSET, this.font.width(label), CTRL_H);
    }

    private void adjustScale(float delta) {
        Dojutsu dojutsu = getDojutsu();
        float newScale = dojutsu.getEyeScale() + delta;
        newScale = Math.max(Dojutsu.MIN_EYE_SCALE, Math.min(Dojutsu.MAX_EYE_SCALE, newScale));
        dojutsu.setEyeScale(newScale);
        Services.PLATFORM.sendEquipDojutsu("scale", String.valueOf(newScale));
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

    /**
     * Draws text centered, auto-scaling down if it exceeds maxWidth.
     * Uses pose stack scaling to shrink text smoothly.
     */
    private void drawCenteredAutoSize(GuiGraphics guiGraphics, Component text, int centerX, int y, int maxWidth, int color) {
        int w = this.font.width(text);
        if (w <= maxWidth) {
            guiGraphics.drawString(this.font, text, centerX - w / 2, y, color, false);
        } else {
            float scale = (float) maxWidth / w;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(centerX, y, 0);
            guiGraphics.pose().scale(scale, scale, 1.0F);
            guiGraphics.drawString(this.font, text, -w / 2, 0, color, false);
            guiGraphics.pose().popPose();
        }
    }

    // --- Arrow hit detection ---

    private String getArrowAt(double mouseX, double mouseY) {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        int arrowY = top + 186;
        int arrowH = 9;
        int arrowW = 8;

        int leftPrevX = left + LEFT_COL - 40;
        int leftNextX = left + LEFT_COL + 32;
        int rightPrevX = left + RIGHT_COL - 40;
        int rightNextX = left + RIGHT_COL + 32;

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
            // Custom controls: scale -/+, reset, hide/show
            if (getMinusArea().contains(mouseX, mouseY)) {
                adjustScale(-Dojutsu.EYE_SCALE_STEP);
                return true;
            }
            if (getPlusArea().contains(mouseX, mouseY)) {
                adjustScale(Dojutsu.EYE_SCALE_STEP);
                return true;
            }
            if (getResetArea().contains(mouseX, mouseY)) {
                Dojutsu dojutsu = getDojutsu();
                dojutsu.resetEyeVisuals();
                Services.PLATFORM.sendEquipDojutsu("reset", "");
                return true;
            }
            if (getHideShowArea().contains(mouseX, mouseY)) {
                Dojutsu dojutsu = getDojutsu();
                if (dojutsu.areEyesVisible()) {
                    dojutsu.setEyesVisible(false);
                    Services.PLATFORM.sendEquipDojutsu("hide", "");
                } else {
                    dojutsu.setEyesVisible(true);
                    Services.PLATFORM.sendEquipDojutsu("show", "");
                }
                return true;
            }

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

            // Check if clicking on an eye texture to start dragging
            String clickedEye = getEyeAt(mouseX, mouseY);
            if (clickedEye != null) {
                draggingEye = clickedEye;
                Dojutsu dojutsu = getDojutsu();
                if ("left".equals(clickedEye)) {
                    dragStartOffsetX = dojutsu.getLeftEyeOffsetX();
                    dragStartOffsetY = dojutsu.getLeftEyeOffsetY();
                } else {
                    dragStartOffsetX = dojutsu.getRightEyeOffsetX();
                    dragStartOffsetY = dojutsu.getRightEyeOffsetY();
                }
                dragStartMouseX = mouseX;
                dragStartMouseY = mouseY;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && draggingEye != null) {
            int dx = (int) (mouseX - dragStartMouseX);
            int dy = (int) (mouseY - dragStartMouseY);
            int newOffsetX = Math.max(-Dojutsu.MAX_EYE_OFFSET, Math.min(Dojutsu.MAX_EYE_OFFSET, dragStartOffsetX + dx));
            int newOffsetY = Math.max(-Dojutsu.MAX_EYE_OFFSET, Math.min(Dojutsu.MAX_EYE_OFFSET, dragStartOffsetY + dy));
            // Update client-side immediately for visual feedback
            Dojutsu dojutsu = getDojutsu();
            if ("left".equals(draggingEye)) {
                dojutsu.setLeftEyeOffset(newOffsetX, newOffsetY);
            } else {
                dojutsu.setRightEyeOffset(newOffsetX, newOffsetY);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingEye != null) {
            // Send final offset to server
            Dojutsu dojutsu = getDojutsu();
            if ("left".equals(draggingEye)) {
                Services.PLATFORM.sendEquipDojutsu("offset_left",
                        dojutsu.getLeftEyeOffsetX() + "," + dojutsu.getLeftEyeOffsetY());
            } else {
                Services.PLATFORM.sendEquipDojutsu("offset_right",
                        dojutsu.getRightEyeOffsetX() + "," + dojutsu.getRightEyeOffsetY());
            }
            draggingEye = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private String getEyeAt(double mouseX, double mouseY) {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        int faceX = left + 128;
        int faceY = top + 94;
        Dojutsu dojutsu = getDojutsu();
        float scale = dojutsu.getEyeScale();
        int eyeW = Math.round(14 * scale);
        int eyeH = Math.round(5 * scale);
        // Add padding around eye for easier clicking
        int pad = 6;

        String le = dojutsu.getLeftEye();
        if (le != null && !le.isEmpty() && Dojutsu.DOJUTSU_LEFT_EYE.containsKey(le)) {
            int ex = faceX - 16 + dojutsu.getLeftEyeOffsetX();
            int ey = faceY - 6 + dojutsu.getLeftEyeOffsetY();
            if (mouseX >= ex - pad && mouseX < ex + eyeW + pad && mouseY >= ey - pad && mouseY < ey + eyeH + pad) {
                return "left";
            }
        }
        String re = dojutsu.getRightEye();
        if (re != null && !re.isEmpty() && Dojutsu.DOJUTSU_RIGHT_EYE.containsKey(re)) {
            int ex = faceX + 2 + dojutsu.getRightEyeOffsetX();
            int ey = faceY - 6 + dojutsu.getRightEyeOffsetY();
            if (mouseX >= ex - pad && mouseX < ex + eyeW + pad && mouseY >= ey - pad && mouseY < ey + eyeH + pad) {
                return "right";
            }
        }
        return null;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        // Draw background panel — use full texture including borders
        guiGraphics.blit(BACKGROUND, left, top, PANEL_WIDTH, PANEL_HEIGHT, 0.0F, 0.0F, 246, 200, 256, 256);

        Dojutsu dojutsu = getDojutsu();
        List<String> unlockedList = dojutsu.getUnlockedList();

        // Title
        drawCenteredNoShadow(guiGraphics,
                Component.translatable("gui.narutoxboruto.dojutsu_menu"),
                left + 128, top + 14, 0x404040);

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
                        left + 128, top + 26, 0x226622);
            } else if (unlockedList.size() >= Dojutsu.MAX_DOJUTSU) {
                drawCenteredNoShadow(guiGraphics,
                        Component.translatable("dojutsu.max_reached"),
                        left + 128, top + 26, 0x662222);
            } else {
                int remaining = Math.max(0, Dojutsu.REQUIRED_TICKS - timer);
                int totalSeconds = remaining / 20;
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;
                drawCenteredNoShadow(guiGraphics,
                        Component.translatable("dojutsu.timer_line1",
                                Component.translatable("dojutsu." + clanDojutsu)),
                        left + 128, top + 26, 0x664400);
                drawCenteredNoShadow(guiGraphics,
                        Component.translatable("dojutsu.timer_line2",
                                String.format("%dm %02ds", minutes, seconds)),
                        left + 128, top + 36, 0x664400);
            }
        } else {
            drawCenteredNoShadow(guiGraphics,
                    Component.translatable("dojutsu.not_eligible"),
                    left + 128, top + 26, 0x555555);
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
                unlockedStartX, top + 52, 0x404040, false);

        int iconStartX = unlockedStartX + labelWidth;
        if (!unlockedList.isEmpty()) {
            for (int i = 0; i < unlockedList.size(); i++) {
                String dj = unlockedList.get(i);
                if (Dojutsu.DOJUTSU_ICON.containsKey(dj)) {
                    ResourceLocation icon = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                            "textures/dojutsu/icons/" + Dojutsu.DOJUTSU_ICON.get(dj) + ".png");
                    guiGraphics.blit(icon, iconStartX + i * 14, top + 50,
                            11, 11, 0.0F, 0.0F, 32, 32, 32, 32);
                }
            }
        } else {
            guiGraphics.drawString(this.font, Component.translatable("shinobiStat.dojutsu_none"),
                    iconStartX, top + 52, 0x888888, false);
        }

        // --- Character model display (face close-up, static pose) ---
        int entityLeft = left + 88;
        int entityTop = top + 64;
        int entityRight = left + 168;
        int entityBottom = top + 128;
        int lookX = (entityLeft + entityRight) / 2;
        int lookY = entityTop + 10;
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics, entityLeft, entityTop, entityRight, entityBottom,
                110, 0.72F, lookX, lookY, player);

        // Draw equipped dojutsu eye textures on the character face
        // Push Z forward so eyes render IN FRONT of the 3D player model
        String le = dojutsu.getLeftEye();
        String re = dojutsu.getRightEye();
        boolean hasLeft = le != null && !le.isEmpty() && Dojutsu.DOJUTSU_LEFT_EYE.containsKey(le);
        boolean hasRight = re != null && !re.isEmpty() && Dojutsu.DOJUTSU_RIGHT_EYE.containsKey(re);

        if (hasLeft || hasRight) {
            int faceX = (entityLeft + entityRight) / 2;
            int faceY = (entityTop + entityBottom) / 2;
            float scale = dojutsu.getEyeScale();
            int eyeW = Math.round(14 * scale);
            int eyeH = Math.round(5 * scale);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200); // Push in front of 3D model
            if (hasLeft) {
                ResourceLocation leftEyeTex = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                        "textures/dojutsu/eyes/" + Dojutsu.DOJUTSU_LEFT_EYE.get(le) + ".png");
                guiGraphics.blit(leftEyeTex, faceX - 16 + dojutsu.getLeftEyeOffsetX(), faceY - 6 + dojutsu.getLeftEyeOffsetY(),
                        eyeW, eyeH, 0.0F, 0.0F, 152, 47, 152, 47);
            }
            if (hasRight) {
                ResourceLocation rightEyeTex = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                        "textures/dojutsu/eyes/" + Dojutsu.DOJUTSU_RIGHT_EYE.get(re) + ".png");
                guiGraphics.blit(rightEyeTex, faceX + 2 + dojutsu.getRightEyeOffsetX(), faceY - 6 + dojutsu.getRightEyeOffsetY(),
                        eyeW, eyeH, 0.0F, 0.0F, 152, 47, 152, 47);
            }
            guiGraphics.pose().popPose();
        }

        // --- Visual controls (custom drawn with dark text) ---
        int ctrlY = top + CONTROLS_Y_OFFSET;

        // Draw [-] and [+] with hover highlight
        ClickArea minusArea = getMinusArea();
        ClickArea plusArea = getPlusArea();
        ClickArea resetArea = getResetArea();
        ClickArea hideShowArea = getHideShowArea();

        int minusColor = minusArea.contains(mouseX, mouseY) ? 0x000000 : 0x404040;
        int plusColor = plusArea.contains(mouseX, mouseY) ? 0x000000 : 0x404040;
        int resetColor = resetArea.contains(mouseX, mouseY) ? 0x000000 : 0x404040;
        int hideShowColor = hideShowArea.contains(mouseX, mouseY) ? 0x000000 : 0x404040;

        guiGraphics.drawString(this.font, "[-]", minusArea.x(), ctrlY + 2, minusColor, false);
        String scaleText = String.format("%.1fx", dojutsu.getEyeScale());
        drawCenteredNoShadow(guiGraphics, Component.literal(scaleText), left + 90, ctrlY + 2, 0x404040);
        guiGraphics.drawString(this.font, "[+]", plusArea.x(), ctrlY + 2, plusColor, false);
        guiGraphics.drawString(this.font, "[Reset]", resetArea.x(), ctrlY + 2, resetColor, false);
        String hideShowLabel = dojutsu.areEyesVisible() ? "[Hide]" : "[Show]";
        guiGraphics.drawString(this.font, hideShowLabel, hideShowArea.x(), ctrlY + 2, hideShowColor, false);

        // --- Equipped Dojutsu section header ---
        drawCenteredNoShadow(guiGraphics,
                Component.literal("-- Equipped Dojutsu --"), left + 128, top + 156, 0x404040);

        // --- Eye selection labels ---
        drawCenteredNoShadow(guiGraphics,
                Component.translatable("dojutsu.left_eye"), left + LEFT_COL, top + 168, 0x404040);
        drawCenteredNoShadow(guiGraphics,
                Component.translatable("dojutsu.right_eye"), left + RIGHT_COL, top + 168, 0x404040);

        // --- Eye type names with ◀ ▶ arrows (hover-highlighted) ---
        hoveredArrow = getArrowAt(mouseX, mouseY);
        int arrowY = top + 186;

        // Left eye selector: ◀ name ▶
        String leftEye = dojutsu.getLeftEye();
        Component leftLabel = (leftEye == null || leftEye.isEmpty())
                ? Component.literal("None")
                : Component.translatable("dojutsu." + leftEye);
        drawCenteredAutoSize(guiGraphics, leftLabel, left + LEFT_COL, arrowY, 50, 0x226622);
        if (leftEye != null && !leftEye.isEmpty() && Dojutsu.DOJUTSU_LEFT_EYE.containsKey(leftEye)) {
            ResourceLocation leftIcon = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                    "textures/dojutsu/eyes/" + Dojutsu.DOJUTSU_LEFT_EYE.get(leftEye) + ".png");
            guiGraphics.blit(leftIcon, left + LEFT_COL - 10, top + 199, 19, 6, 0.0F, 0.0F, 152, 47, 152, 47);
        }
        int leftPrevColor = "left_prev".equals(hoveredArrow) ? 0xFFFFFF : 0x606060;
        int leftNextColor = "left_next".equals(hoveredArrow) ? 0xFFFFFF : 0x606060;
        guiGraphics.drawString(this.font, "\u25C0", left + LEFT_COL - 40, arrowY, leftPrevColor, false);
        guiGraphics.drawString(this.font, "\u25B6", left + LEFT_COL + 32, arrowY, leftNextColor, false);

        // Right eye selector: ◀ name ▶
        String rightEye = dojutsu.getRightEye();
        Component rightLabel = (rightEye == null || rightEye.isEmpty())
                ? Component.literal("None")
                : Component.translatable("dojutsu." + rightEye);
        drawCenteredAutoSize(guiGraphics, rightLabel, left + RIGHT_COL, arrowY, 50, 0x226622);
        if (rightEye != null && !rightEye.isEmpty() && Dojutsu.DOJUTSU_RIGHT_EYE.containsKey(rightEye)) {
            ResourceLocation rightIcon = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                    "textures/dojutsu/eyes/" + Dojutsu.DOJUTSU_RIGHT_EYE.get(rightEye) + ".png");
            guiGraphics.blit(rightIcon, left + RIGHT_COL - 10, top + 199, 19, 6, 0.0F, 0.0F, 152, 47, 152, 47);
        }
        int rightPrevColor = "right_prev".equals(hoveredArrow) ? 0xFFFFFF : 0x606060;
        int rightNextColor = "right_next".equals(hoveredArrow) ? 0xFFFFFF : 0x606060;
        guiGraphics.drawString(this.font, "\u25C0", left + RIGHT_COL - 40, arrowY, rightPrevColor, false);
        guiGraphics.drawString(this.font, "\u25B6", left + RIGHT_COL + 32, arrowY, rightNextColor, false);

        // --- ? help button (bottom right, same row as Done) ---
        int helpX = left + PANEL_WIDTH - 28;
        int helpY = top + 220;
        int helpSize = 16;
        hoveringHelp = mouseX >= helpX && mouseX < helpX + helpSize && mouseY >= helpY && mouseY < helpY + helpSize;
        // Filled background so it's visible on the book texture
        int bgColor = hoveringHelp ? 0xFF555555 : 0xFF333333;
        guiGraphics.fill(helpX, helpY, helpX + helpSize, helpY + helpSize, bgColor);
        int borderColor = hoveringHelp ? 0xFFFFFFFF : 0xFFAAAAAA;
        guiGraphics.hLine(helpX, helpX + helpSize - 1, helpY, borderColor);
        guiGraphics.hLine(helpX, helpX + helpSize - 1, helpY + helpSize - 1, borderColor);
        guiGraphics.vLine(helpX, helpY, helpY + helpSize - 1, borderColor);
        guiGraphics.vLine(helpX + helpSize - 1, helpY, helpY + helpSize - 1, borderColor);
        guiGraphics.drawString(this.font, "?", helpX + 5, helpY + 4, 0xFFFFFF, false);

        // Draw tooltip when hovering ?
        if (hoveringHelp) {
            List<Component> tooltip = List.of(
                    Component.literal("\u00A7lDojutsu Menu Help"),
                    Component.literal(""),
                    Component.literal("\u00A7eEquipped Dojutsu\u00A7r: Use \u25C0 \u25B6 arrows to"),
                    Component.literal("select which dojutsu is active for abilities."),
                    Component.literal(""),
                    Component.literal("\u00A7eEye Visuals\u00A7r: Drag eyes on the character"),
                    Component.literal("to reposition. Use +/- for eye size."),
                    Component.literal("\u00A7eReset\u00A7r restores default position & size."),
                    Component.literal("\u00A7eHide/Show\u00A7r toggles eye visibility on skin.")
            );
            guiGraphics.renderTooltip(this.font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
