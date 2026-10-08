package net.narutoxboruto.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
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

/**
 * The character screen, opened with the stats key: an open ninja scroll with two columns, like a book page.
 *
 * On the left are the ten stats, each with a small ink bar and its value. On the right is the information:
 * clan, affiliation, rank, nature releases, kekkei genkai and dojutsu. The village emblem sits in a round seal
 * at the top, and the Dojutsu button in the corner turns the page to the dojutsu menu. The bottom strip shows
 * the total of the stats, the Shinobi Points and a bar of the overall progress.
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

    private static final float MARGIN = 8.0F;

    private static final int INK = 0xFF2A1A10;
    private static final int LABEL = 0xFF6B5030;
    private static final int LINE = 0xFF8A6A3B;
    private static final int RED = 0xFFA02820;
    private static final int PAPER_LIGHT = 0xFFF4E6C2;
    private static final int TRACK = 0xFFD2BE94;

    /** The stats in the order of the list, with the colour of each bar. */
    private static final String[] STATS = {
            "ninjutsu", "taijutsu", "genjutsu", "kenjutsu", "shurikenjutsu",
            "summoning", "kinjutsu", "senjutsu", "medical", "speed"
    };
    private static final int[] COLORS = {
            0xFF1F4E9E, 0xFFA02820, 0xFF6A2E8A, 0xFFA02820, 0xFFA02820,
            0xFFB05A10, 0xFF6B4A22, 0xFF1E7A72, 0xFF2E7A2E, 0xFF9A6A00
    };
    /** Stats that show "-" until they have a value (they are not trained from the start). */
    private static final List<String> LOCKED = new ArrayList<>(Arrays.asList("ninjutsu", "genjutsu", "kinjutsu", "senjutsu", "summoning"));

    private static final int BUTTON_W = 66;
    private static final int BUTTON_H = 14;

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

    private int buttonX() {
        return left() + PAPER_RIGHT - BUTTON_W - 2;
    }

    private int buttonY() {
        return top() + 12;
    }

    private boolean overButton(double realMouseX, double realMouseY) {
        double x = realMouseX / uiScale;
        double y = realMouseY / uiScale;
        return x >= buttonX() && x < buttonX() + BUTTON_W && y >= buttonY() && y < buttonY() + BUTTON_H;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && overButton(mouseX, mouseY)) {
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
        if (player != null) {
            drawStats(g);
            drawInformation(g);
            drawBottom(g);
            drawEmblem(g);
        }

        // The button that turns the page to the dojutsu menu
        boolean hover = overButton(mouseX, mouseY);
        g.fill(buttonX() - 1, buttonY() - 1, buttonX() + BUTTON_W + 1, buttonY() + BUTTON_H + 1, INK);
        g.fill(buttonX(), buttonY(), buttonX() + BUTTON_W, buttonY() + BUTTON_H, hover ? 0xFFCC3C30 : RED);
        g.fill(buttonX() + 1, buttonY() + 1, buttonX() + BUTTON_W - 1, buttonY() + 2, 0x60FFFFFF);
        Component text = Component.translatable("gui.narutoxboruto.tab_dojutsu").append(" >");
        g.drawString(this.font, text, buttonX() + (BUTTON_W - this.font.width(text)) / 2, buttonY() + 3, PAPER_LIGHT, false);
        g.pose().popPose();
    }

    // ---------------------------------------------------------------- the emblem

    /** The village emblem in a round seal at the top, over the edge of the paper. */
    private void drawEmblem(GuiGraphics g) {
        String affiliation = Services.PLATFORM.getAffiliation(player).getValue();
        int cx = left() + PANEL_W / 2;
        int cy = top() + 18;
        int radius = 15;
        fillCircle(g, cx, cy, radius + 1, INK);
        fillCircle(g, cx, cy, radius, RED);
        fillCircle(g, cx, cy, radius - 2, PAPER_LIGHT);
        if (ModUtil.AFF_LIST.contains(affiliation)) {
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/affiliations/" + affiliation + ".png");
            g.blit(texture, cx - 10, cy - 10, 0, 0, 20, 20, 20, 20);
        }
    }

    private void fillCircle(GuiGraphics g, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int half = (int) Math.round(Math.sqrt(radius * radius - dy * dy + radius * 0.5));
            g.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, color);
        }
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

    /** A full bar is a round number above the highest stat. */
    private int barScale() {
        int highest = 0;
        for (String name : STATS) highest = Math.max(highest, statValue(name));
        return Math.max(50, ((highest + 49) / 50) * 50);
    }

    private void drawStats(GuiGraphics g) {
        int scale = barScale();
        int x = left() + PAPER_LEFT + 8;
        int y = top() + 36;
        int labelWidth = 74;
        int barWidth = 40;
        for (int i = 0; i < STATS.length; i++) {
            int rowY = y + i * 11;
            int value = statValue(STATS[i]);
            boolean dashed = LOCKED.contains(STATS[i]) && value == 0;

            g.drawString(this.font, Component.translatable("shinobiStat." + STATS[i]), x, rowY, dashed ? 0xFF9A8A6A : INK, false);

            int barX = x + labelWidth;
            g.fill(barX, rowY + 2, barX + barWidth, rowY + 7, LINE);
            g.fill(barX + 1, rowY + 3, barX + barWidth - 1, rowY + 6, TRACK);
            int fill = Math.round((barWidth - 2) * Math.min(1.0F, value / (float) scale));
            if (fill > 0) g.fill(barX + 1, rowY + 3, barX + 1 + fill, rowY + 6, COLORS[i]);

            String text = dashed ? "-" : String.valueOf(value);
            g.drawString(this.font, text, barX + barWidth + 8 + (24 - this.font.width(text)), rowY, INK, false);
        }
    }

    // ---------------------------------------------------------------- the information

    private void drawInformation(GuiGraphics g) {
        String affiliation = Services.PLATFORM.getAffiliation(player).getValue();
        String clan = Services.PLATFORM.getClan(player).getValue();
        String rank = Services.PLATFORM.getRank(player).getValue();

        int x = left() + PAPER_LEFT + 168;
        int y = top() + 36;
        int rowHeight = 13;

        g.drawString(this.font, player.getName(), x, y, 0xFF1F4E9E, false);
        y += rowHeight + 2;
        drawRow(g, x, y, "shinobiStat.clan", "clan." + clan, "clans", clan, ModUtil.CLAN_LIST);
        y += rowHeight;
        drawRow(g, x, y, "shinobiStat.affiliation", "affiliation." + affiliation, "affiliations", affiliation, ModUtil.AFF_LIST);
        y += rowHeight;
        drawRow(g, x, y, "shinobiStat.rank", "rank." + rank, null, rank, null);

        // Releases on a line of their own
        y += rowHeight;
        ReleaseList releases = Services.PLATFORM.getReleaseList(player);
        g.drawString(this.font, Component.translatable("shinobiStat.releases"), x, y, LABEL, false);
        y += 10;
        if (releases.isEmpty()) {
            g.drawString(this.font, Component.translatable("shinobiStat.release_unknown"), x, y, INK, false);
        } else {
            List<String> list = releases.getReleasesAsList();
            for (int i = 0; i < list.size(); i++) {
                ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                        "textures/item/release/" + list.get(i).toLowerCase() + ".png");
                g.blit(texture, x + i * 14, y - 2, 0, 0, 12, 12, 12, 12);
            }
        }

        y += rowHeight + 2;
        g.drawString(this.font, Component.translatable("shinobiStat.kekkei_genkai").append(": ")
                .append(Component.translatable("kekkei_genkai.none")), x, y, LABEL, false);

        y += rowHeight;
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
                g.blit(icon, dojutsuX + i * 13, y - 2, 11, 11, 0.0F, 0.0F, 32, 32, 32, 32);
            }
        }
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

    // ---------------------------------------------------------------- the bottom strip

    private void drawBottom(GuiGraphics g) {
        int x0 = left() + PAPER_LEFT + 8;
        int x1 = left() + PAPER_RIGHT - 8;
        g.fill(x0, top() + 150, x1, top() + 152, INK);
        g.fill(x0, top() + 152, x1, top() + 153, LINE);

        int total = 0;
        for (String name : STATS) total += statValue(name);
        int scale = barScale();

        int y = top() + 158;
        Component totalLabel = Component.translatable("gui.narutoxboruto.total").append(": " + total);
        g.drawString(this.font, totalLabel, x0, y, INK, false);
        String points = Component.translatable("shinobiStat.shinobi_points").getString() + ": "
                + Services.PLATFORM.getShinobiPoints(player).getValue();
        g.drawString(this.font, points, x1 - this.font.width(points), y, RED, false);

        // The overall bar: all the stats together against a full set of bars
        int barY = top() + 170;
        int width = x1 - x0;
        g.fill(x0, barY, x1, barY + 7, LINE);
        g.fill(x0 + 1, barY + 1, x1 - 1, barY + 6, TRACK);
        int fill = Math.round((width - 2) * Math.min(1.0F, total / (float) (scale * STATS.length)));
        if (fill > 0) {
            g.fill(x0 + 1, barY + 1, x0 + 1 + fill, barY + 6, RED);
            g.fill(x0 + 1, barY + 1, x0 + 1 + fill, barY + 2, 0x50FFFFFF);
        }
    }
}
