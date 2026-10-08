package net.narutoxboruto.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.capabilities.info.ReleaseList;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The character screen, opened with the stats key: an unrolled ninja scroll.
 *
 * The top of the scroll is the shinobi registry entry: a portrait, the name with the rank as a red stamp and
 * a card number, the village, clan, nature releases and dojutsu, and the Shinobi Points on a seal. Below a
 * brush line are the ten stats as ink bars, combat stats on the left and ninja arts on the right. The red
 * seal in the top right corner opens the dojutsu menu.
 */
public class ShinobiStatsGui extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/gui/character_screen.png");
    private static final int TEXTURE_W = 512;
    private static final int TEXTURE_H = 256;

    private static final int PANEL_W = 320;
    private static final int PANEL_H = 196;
    private static final int PAPER_LEFT = 24;
    private static final int PAPER_RIGHT = PANEL_W - 24;

    private static final int SEAL_SIZE = 22;
    private static final float MARGIN = 8.0F;

    private static final int INK = 0xFF2A1A10;
    private static final int LABEL = 0xFF6B5030;
    private static final int LINE = 0xFF8A6A3B;
    private static final int RED = 0xFFA02820;
    private static final int PAPER_LIGHT = 0xFFF4E6C2;
    private static final int TRACK = 0xFFD2BE94;

    /** The stats in the two columns of the bottom half, with the colour of each bar. */
    private static final String[] LEFT_STATS = {"taijutsu", "kenjutsu", "shurikenjutsu", "speed", "medical"};
    private static final String[] RIGHT_STATS = {"ninjutsu", "genjutsu", "kinjutsu", "senjutsu", "summoning"};
    private static final int[] LEFT_COLORS = {0xFFA02820, 0xFFA02820, 0xFFA02820, 0xFF9A6A00, 0xFF2E7A2E};
    private static final int[] RIGHT_COLORS = {0xFF1F4E9E, 0xFF6A2E8A, 0xFF6B4A22, 0xFF1E7A72, 0xFFB05A10};
    /** Stats that show "-" until they have a value (they are not trained from the start). */
    private static final List<String> LOCKED = new ArrayList<>(Arrays.asList("ninjutsu", "genjutsu", "kinjutsu", "senjutsu", "summoning"));

    private final LocalPlayer player;
    private float uiScale = 1.0F;

    public ShinobiStatsGui() {
        super(Component.translatable("gui.narutoxboruto.shinobi_stats"));
        this.player = Minecraft.getInstance().player;
    }

    // ---------------------------------------------------------------- layout

    /** Everything is laid out in the shrunk screen: this wide and this high. */
    private float virtualWidth() {
        return this.width / uiScale;
    }

    private float virtualHeight() {
        return this.height / uiScale;
    }

    private int left() {
        return Math.round((virtualWidth() - PANEL_W) / 2.0F);
    }

    private int top() {
        return Math.round((virtualHeight() - PANEL_H) / 2.0F);
    }

    @Override
    protected void init() {
        // Shrink to fit: the scroll has to fit in the window, also at a large GUI scale
        float fit = Math.min(1.0F, Math.min((this.height - MARGIN) / PANEL_H, (this.width - MARGIN) / PANEL_W));
        uiScale = Math.max(0.25F, (float) Math.floor(fit * 20.0F) / 20.0F);
        super.init();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int sealX() {
        return left() + PAPER_RIGHT - SEAL_SIZE - 6;
    }

    private int sealY() {
        return top() + 12;
    }

    private boolean overSeal(double realMouseX, double realMouseY) {
        double x = realMouseX / uiScale;
        double y = realMouseY / uiScale;
        return x >= sealX() && x < sealX() + SEAL_SIZE && y >= sealY() && y < sealY() + SEAL_SIZE;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && overSeal(mouseX, mouseY)) {
            Minecraft.getInstance().setScreen(new DojutsuScreen());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ---------------------------------------------------------------- drawing

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(g, mouseX, mouseY, partialTick);
        g.pose().pushPose();
        g.pose().scale(uiScale, uiScale, 1.0F);
        g.blit(TEXTURE, left(), top(), 0, 0, PANEL_W, PANEL_H, TEXTURE_W, TEXTURE_H);
        g.pose().popPose();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        g.pose().pushPose();
        g.pose().scale(uiScale, uiScale, 1.0F);
        int virtualMouseX = Math.round(mouseX / uiScale);
        int virtualMouseY = Math.round(mouseY / uiScale);
        if (player != null) {
            drawRegistry(g, virtualMouseX, virtualMouseY);
            drawStatistics(g);
        }

        // The seal that opens the dojutsu menu
        boolean hover = overSeal(mouseX, mouseY);
        g.blit(TEXTURE, sealX(), sealY(), hover ? 344 : 320, 0, SEAL_SIZE, SEAL_SIZE, TEXTURE_W, TEXTURE_H);
        g.blit(TEXTURE, sealX() + 3, sealY() + 3, 368, 0, 16, 16, TEXTURE_W, TEXTURE_H);
        g.pose().popPose();

        if (hover) {
            g.renderTooltip(this.font, Component.translatable("gui.narutoxboruto.tab_dojutsu"), mouseX, mouseY);
        }
    }

    // ---------------------------------------------------------------- the registry entry

    private void drawRegistry(GuiGraphics g, int mouseX, int mouseY) {
        String affiliation = Services.PLATFORM.getAffiliation(player).getValue();
        String clan = Services.PLATFORM.getClan(player).getValue();
        String rank = Services.PLATFORM.getRank(player).getValue();

        // The title, in the middle at the top
        Component title = Component.translatable("gui.narutoxboruto.heading_information").withStyle(net.minecraft.ChatFormatting.BOLD);
        g.drawString(this.font, title, left() + (PANEL_W - this.font.width(title)) / 2, top() + 14, INK, false);
        g.fill(left() + PAPER_LEFT + 8, top() + 26, sealX() - 6, top() + 27, LINE);

        // Portrait
        int frameX = left() + PAPER_LEFT + 6;
        int frameY = top() + 32;
        drawFrame(g, frameX, frameY, 56, 74);
        InventoryScreen.renderEntityInInventoryFollowsMouse(g, frameX + 1, frameY + 1, frameX + 55, frameY + 73,
                28, 0.0625F, mouseX, mouseY, player);

        int x = frameX + 56 + 12;
        int right = left() + PAPER_RIGHT - 10;

        // The name, a little larger than the rest
        g.pose().pushPose();
        g.pose().translate(x, top() + 32, 0.0F);
        g.pose().scale(1.25F, 1.25F, 1.0F);
        g.drawString(this.font, player.getName(), 0, 0, 0xFF1F4E9E, false);
        g.pose().popPose();

        // The rank as a red stamp, and the card number
        String stamp = Component.translatable("rank." + rank).getString().toUpperCase(Locale.ROOT);
        int stampY = top() + 46;
        int stampW = this.font.width(stamp) + 8;
        g.fill(x, stampY, x + stampW, stampY + 12, RED);
        g.fill(x + 1, stampY + 1, x + stampW - 1, stampY + 11, PAPER_LIGHT);
        g.drawString(this.font, stamp, x + 4, stampY + 2, RED, false);
        String card = String.format("No. %08X", player.getUUID().hashCode());
        g.drawString(this.font, card, x + stampW + 8, stampY + 2, LABEL, false);

        // The rows
        int y = top() + 63;
        int rowHeight = 9;
        drawRow(g, x, y, "shinobiStat.affiliation", "affiliation." + affiliation, "affiliations", affiliation, ModUtil.AFF_LIST);
        y += rowHeight + 1;
        drawRow(g, x, y, "shinobiStat.clan", "clan." + clan, "clans", clan, ModUtil.CLAN_LIST);

        y += rowHeight + 1;
        ReleaseList releases = Services.PLATFORM.getReleaseList(player);
        Component label = Component.translatable("shinobiStat.releases").append(": ");
        g.drawString(this.font, label, x, y, LABEL, false);
        int iconX = x + this.font.width(label);
        if (releases.isEmpty()) {
            g.drawString(this.font, Component.translatable("shinobiStat.release_unknown"), iconX, y, INK, false);
        } else {
            List<String> list = releases.getReleasesAsList();
            for (int i = 0; i < list.size(); i++) {
                ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                        "textures/item/release/" + list.get(i).toLowerCase() + ".png");
                g.blit(texture, iconX + i * 12, y - 2, 0, 0, 11, 11, 11, 11);
            }
        }

        y += rowHeight + 1;
        g.drawString(this.font, Component.translatable("shinobiStat.kekkei_genkai").append(": ")
                .append(Component.translatable("kekkei_genkai.none")), x, y, LABEL, false);

        y += rowHeight + 1;
        Dojutsu dojutsu = Services.PLATFORM.getDojutsu(player);
        List<String> unlocked = dojutsu.getUnlockedList();
        Component dojutsuLabel = Component.translatable("shinobiStat.dojutsu").append(": ");
        g.drawString(this.font, dojutsuLabel, x, y, LABEL, false);
        int dojutsuX = x + this.font.width(dojutsuLabel);
        if (unlocked.isEmpty()) {
            g.drawString(this.font, Component.translatable("shinobiStat.dojutsu_none"), dojutsuX, y, INK, false);
        } else {
            for (int i = 0; i < unlocked.size(); i++) {
                String name = unlocked.get(i);
                if (!Dojutsu.DOJUTSU_ICON.containsKey(name)) continue;
                ResourceLocation icon = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                        "textures/dojutsu/icons/" + Dojutsu.DOJUTSU_ICON.get(name) + ".png");
                g.blit(icon, dojutsuX + i * 12, y - 2, 11, 11, 0.0F, 0.0F, 32, 32, 32, 32);
            }
        }

        // The Shinobi Points on a seal, on the right
        int boxW = 70;
        int boxX = right - boxW;
        int boxY = top() + 64;
        g.fill(boxX - 1, boxY - 1, boxX + boxW + 1, boxY + 36, RED);
        g.fill(boxX, boxY, boxX + boxW, boxY + 35, PAPER_LIGHT);
        Component pointsLabel = Component.translatable("shinobiStat.shinobi_points");
        g.drawString(this.font, pointsLabel, boxX + (boxW - this.font.width(pointsLabel)) / 2, boxY + 4, RED, false);
        String points = String.valueOf(Services.PLATFORM.getShinobiPoints(player).getValue());
        g.pose().pushPose();
        g.pose().translate(boxX + boxW / 2.0F, boxY + 16, 0.0F);
        g.pose().scale(2.0F, 2.0F, 1.0F);
        g.drawString(this.font, points, -this.font.width(points) / 2, 0, INK, false);
        g.pose().popPose();

        // The brush line between the two halves
        g.fill(left() + PAPER_LEFT + 8, top() + 112, left() + PAPER_RIGHT - 8, top() + 114, INK);
        g.fill(left() + PAPER_LEFT + 8, top() + 114, left() + PAPER_RIGHT - 8, top() + 115, LINE);
    }

    private void drawRow(GuiGraphics g, int x, int y, String labelKey, String valueKey, String iconFolder, String value, List<String> iconList) {
        Component label = Component.translatable(labelKey).append(": ");
        g.drawString(this.font, label, x, y, LABEL, false);
        int valueX = x + this.font.width(label);
        if (iconFolder != null && iconList != null && iconList.contains(value)) {
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/" + iconFolder + "/" + value + ".png");
            g.blit(texture, valueX, y - 1, 0, 0, 10, 10, 10, 10);
            valueX += 13;
        }
        g.drawString(this.font, Component.translatable(valueKey), valueX, y, INK, false);
    }

    private void drawFrame(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, LINE);
        g.fill(x, y, x + w, y + h, 0xFFEADBB4);
    }

    // ---------------------------------------------------------------- the stats

    private int statValue(String name) {
        return switch (name) {
            case "taijutsu" -> Services.PLATFORM.getTaijutsu(player).getValue();
            case "ninjutsu" -> Services.PLATFORM.getNinjutsu(player).getValue();
            case "genjutsu" -> Services.PLATFORM.getGenjutsu(player).getValue();
            case "kenjutsu" -> Services.PLATFORM.getKenjutsu(player).getValue();
            case "kinjutsu" -> Services.PLATFORM.getKinjutsu(player).getValue();
            case "medical" -> Services.PLATFORM.getMedical(player).getValue();
            case "senjutsu" -> Services.PLATFORM.getSenjutsu(player).getValue();
            case "shurikenjutsu" -> Services.PLATFORM.getShurikenjutsu(player).getValue();
            case "speed" -> Services.PLATFORM.getSpeed(player).getValue();
            case "summoning" -> Services.PLATFORM.getSummoning(player).getValue();
            default -> 0;
        };
    }

    private void drawStatistics(GuiGraphics g) {
        int highest = 0;
        for (String name : LEFT_STATS) highest = Math.max(highest, statValue(name));
        for (String name : RIGHT_STATS) highest = Math.max(highest, statValue(name));
        // A full bar is a round number above the highest stat
        int scale = Math.max(50, ((highest + 49) / 50) * 50);

        int columnWidth = 128;
        int first = left() + PAPER_LEFT + 4;
        drawColumn(g, first, top() + 121, columnWidth, LEFT_STATS, LEFT_COLORS, scale);
        drawColumn(g, first + columnWidth + 8, top() + 121, columnWidth, RIGHT_STATS, RIGHT_COLORS, scale);
    }

    private void drawColumn(GuiGraphics g, int x, int y, int width, String[] names, int[] colors, int scale) {
        int labelWidth = 72;
        int valueWidth = 20;
        int barX = x + labelWidth;
        int barWidth = width - labelWidth - valueWidth;
        for (int i = 0; i < names.length; i++) {
            int rowY = y + i * 11;
            int value = statValue(names[i]);
            boolean dashed = LOCKED.contains(names[i]) && value == 0;

            g.drawString(this.font, Component.translatable("shinobiStat." + names[i]), x, rowY, dashed ? 0xFF9A8A6A : colors[i], false);

            g.fill(barX, rowY + 1, barX + barWidth, rowY + 7, LINE);
            g.fill(barX + 1, rowY + 2, barX + barWidth - 1, rowY + 6, TRACK);
            int fill = Math.round((barWidth - 2) * Math.min(1.0F, value / (float) scale));
            if (fill > 0) {
                g.fill(barX + 1, rowY + 2, barX + 1 + fill, rowY + 6, colors[i]);
                g.fill(barX + 1, rowY + 2, barX + 1 + fill, rowY + 3, 0x40FFFFFF);
            }
            g.drawString(this.font, dashed ? "-" : String.valueOf(value), barX + barWidth + 4, rowY, INK, false);
        }
    }
}
