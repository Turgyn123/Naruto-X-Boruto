package net.narutoxboruto.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.CommonComponents;
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
 * The character screen, opened with the stats key. One cream and green panel with two parts: the information
 * (name, affiliation, clan, rank, releases, dojutsu and a model of the player) on top, and the ten stats on a
 * radar chart below it. A tab on the right side opens the dojutsu menu.
 */
public class ShinobiStatsGui extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/gui/character_screen.png");

    private static final int PANEL_W = 240;
    private static final int PANEL_H = 260;
    private static final int TAB_W = 28;
    private static final int TAB_H = 24;
    private static final int TAB_GAP = 3;

    private static final int TEXTURE_H = 512;

    private static final int INK = 0xFF1B2A24;
    private static final int LABEL = 0xFF5C7568;
    private static final int GREEN = 0xFF1F4034;
    private static final int LINE = 0xFFC4CCBA;
    private static final int FRAME_BG = 0xFFE9EDD0;

    private static final int TAB_CHARACTER = 0;
    private static final int TAB_DOJUTSU = 1;
    private static final String[] TAB_KEYS = {"gui.narutoxboruto.tab_character", "gui.narutoxboruto.tab_dojutsu"};

    /** The stats in the order around the radar chart, clockwise from the top, with the colour of each. */
    private static final String[] STATS = {
            "ninjutsu", "taijutsu", "kenjutsu", "shurikenjutsu", "speed",
            "summoning", "senjutsu", "genjutsu", "kinjutsu", "medical"
    };
    private static final int[] STAT_COLORS = {
            0xFF1F4E9E, 0xFFA02820, 0xFFA02820, 0xFFA02820, 0xFF9A6A00,
            0xFFB05A10, 0xFF1E7A72, 0xFF6A2E8A, 0xFF6B4A22, 0xFF2E7A2E
    };
    /** Stats that show "-" until they have a value (they are not trained from the start). */
    private static final List<String> LOCKED = new ArrayList<>(Arrays.asList("ninjutsu", "genjutsu", "kinjutsu", "senjutsu", "summoning"));

    /** How much the whole screen is shrunk to fit the window: 1 when there is room for it. */
    private static final float MARGIN = 8.0F;

    private final LocalPlayer player;
    private float uiScale = 1.0F;

    public ShinobiStatsGui() {
        super(Component.translatable("gui.narutoxboruto.shinobi_stats"));
        this.player = Minecraft.getInstance().player;
    }

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
        // Shrink to fit: the panel and its tabs have to fit in the window, also at a large GUI scale
        float fitHeight = (this.height - MARGIN) / PANEL_H;
        float fitWidth = (this.width - MARGIN) / (PANEL_W + 22);
        float fit = Math.min(1.0F, Math.min(fitHeight, fitWidth));
        uiScale = Math.max(0.25F, (float) Math.floor(fit * 20.0F) / 20.0F);
        super.init();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---------------------------------------------------------------- tabs

    private int tabX(int index) {
        // The character tab is the open one, so it sticks out further from the panel
        return left() + PANEL_W - (index == TAB_CHARACTER ? 4 : 7);
    }

    private int tabY(int index) {
        return top() + 12 + index * (TAB_H + TAB_GAP);
    }

    private boolean overTab(int index, double realMouseX, double realMouseY) {
        double mouseX = realMouseX / uiScale;
        double mouseY = realMouseY / uiScale;
        return mouseX >= tabX(index) && mouseX < tabX(index) + TAB_W && mouseY >= tabY(index) && mouseY < tabY(index) + TAB_H;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && overTab(TAB_DOJUTSU, mouseX, mouseY)) {
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
        // The tabs first, so the panel covers the part of them that is under it
        for (int i = 0; i < TAB_KEYS.length; i++) {
            g.blit(TEXTURE, tabX(i), tabY(i), i == TAB_CHARACTER ? TAB_W : 0, PANEL_H, TAB_W, TAB_H, 256, TEXTURE_H);
            g.blit(TEXTURE, tabX(i) + 8, tabY(i) + 4, 56 + i * 16, PANEL_H, 16, 16, 256, TEXTURE_H);
        }
        g.blit(TEXTURE, left(), top(), 0, 0, PANEL_W, PANEL_H, 256, TEXTURE_H);
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
            drawHeading(g, "gui.narutoxboruto.heading_information", top() + 9);
            g.fill(left() + 12, top() + 22, left() + PANEL_W - 12, top() + 23, LINE);
            drawInformation(g, virtualMouseX, virtualMouseY);

            // A thick line between the two parts
            g.fill(left() + 12, top() + 106, left() + PANEL_W - 12, top() + 108, GREEN);
            drawHeading(g, "gui.narutoxboruto.heading_statistics", top() + 111);
            drawStatistics(g);
        }
        g.pose().popPose();

        for (int i = 0; i < TAB_KEYS.length; i++) {
            if (overTab(i, mouseX, mouseY)) {
                g.renderTooltip(this.font, Component.translatable(TAB_KEYS[i]), mouseX, mouseY);
            }
        }
    }

    private void drawHeading(GuiGraphics g, String key, int y) {
        Component heading = Component.translatable(key).withStyle(ChatFormatting.BOLD);
        g.drawString(this.font, heading, left() + (PANEL_W - this.font.width(heading)) / 2, y, GREEN, false);
    }

    // ---------------------------------------------------------------- information tab

    /**
     * The top part is a shinobi registration card: a portrait on the left, the name with a red rank stamp and
     * the card number on the right, and under them the village, clan, nature releases and dojutsu.
     */
    private void drawInformation(GuiGraphics g, int mouseX, int mouseY) {
        String affiliation = Services.PLATFORM.getAffiliation(player).getValue();
        String clan = Services.PLATFORM.getClan(player).getValue();
        String rank = Services.PLATFORM.getRank(player).getValue();

        // Portrait
        int frameX = left() + 14;
        int frameY = top() + 28;
        drawFrame(g, frameX, frameY, 54, 72);
        InventoryScreen.renderEntityInInventoryFollowsMouse(g, frameX + 1, frameY + 1, frameX + 53, frameY + 71,
                27, 0.0625F, mouseX, mouseY, player);

        int x = left() + 78;
        int right = left() + PANEL_W - 14;

        // The name, a little larger than the rest
        g.pose().pushPose();
        g.pose().translate(x, top() + 28, 0.0F);
        g.pose().scale(1.25F, 1.25F, 1.0F);
        g.drawString(this.font, player.getName(), 0, 0, 0xFF1F4E9E, false);
        g.pose().popPose();

        // The rank as a red stamp, and the card number
        String stamp = Component.translatable("rank." + rank).getString().toUpperCase(java.util.Locale.ROOT);
        int stampY = top() + 42;
        int stampW = this.font.width(stamp) + 8;
        g.fill(x, stampY, x + stampW, stampY + 12, 0xFFA02820);
        g.fill(x + 1, stampY + 1, x + stampW - 1, stampY + 11, 0xFFFFFDE8);
        g.drawString(this.font, stamp, x + 4, stampY + 2, 0xFFA02820, false);
        String card = String.format("No. %08X", player.getUUID().hashCode());
        g.drawString(this.font, card, right - this.font.width(card), stampY + 2, LABEL, false);

        // The rows
        int y = top() + 57;
        int rowHeight = 10;
        drawRow(g, x, y, "shinobiStat.affiliation", "affiliation." + affiliation, "affiliations", affiliation, ModUtil.AFF_LIST);
        y += rowHeight;
        drawRow(g, x, y, "shinobiStat.clan", "clan." + clan, "clans", clan, ModUtil.CLAN_LIST);

        y += rowHeight;
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
                g.blit(texture, iconX + i * 13, y - 2, 0, 0, 12, 12, 12, 12);
            }
        }

        y += rowHeight;
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
        boolean icon = iconFolder != null && iconList != null && iconList.contains(value);
        if (icon) {
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/" + iconFolder + "/" + value + ".png");
            g.blit(texture, valueX, y - 2, 0, 0, 11, 11, 11, 11);
            valueX += 14;
        }
        g.drawString(this.font, Component.translatable(valueKey), valueX, y, INK, false);
    }

    private void drawFrame(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, GREEN);
        g.fill(x, y, x + w, y + h, FRAME_BG);
    }

    // ---------------------------------------------------------------- statistics tab

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
        int count = STATS.length;
        int[] values = new int[count];
        int highest = 0;
        for (int i = 0; i < count; i++) {
            values[i] = statValue(STATS[i]);
            highest = Math.max(highest, values[i]);
        }
        // The outer ring is a round number above the highest stat
        int scale = Math.max(50, ((highest + 49) / 50) * 50);

        float cx = left() + PANEL_W / 2.0F;
        float cy = top() + 186;
        float radius = 36.0F;

        // Shinobi points, on the right of the heading
        String points = "SP: " + Services.PLATFORM.getShinobiPoints(player).getValue();
        g.drawString(this.font, points, left() + PANEL_W - 14 - this.font.width(points), top() + 111, LABEL, false);

        // The chart is drawn in real screen pixels, not GUI pixels, so the circles are smooth at any scale
        float f = Math.max(1.0F, (float) Math.ceil(Minecraft.getInstance().getWindow().getGuiScale() * uiScale));
        float big = radius * f;
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0.0F);
        g.pose().scale(1.0F / f, 1.0F / f, 1.0F);

        // The web: round rings and the spokes to each stat
        for (int ring = 1; ring <= 4; ring++) {
            float r = big * ring / 4.0F;
            int segments = Math.max(96, (int) (Math.PI * 2 * r * 1.1));
            int ringColor = ring == 4 ? 0xFF7C9A8A : 0xFFC6D2C2;
            int thickness = ring == 4 ? Math.max(1, Math.round(f * 0.5F)) : 1;
            for (int s = 0; s < segments; s++) {
                double a = Math.PI * 2 * s / segments;
                dot(g, (float) Math.sin(a) * r, (float) -Math.cos(a) * r, thickness, ringColor);
            }
        }
        for (int i = 0; i < count; i++) {
            drawLine(g, 0, 0, (float) Math.sin(Math.PI * 2 * i / count) * big,
                    (float) -Math.cos(Math.PI * 2 * i / count) * big, 0xFFD2DCCB, 1);
        }

        // The stats
        float[] px = new float[count];
        float[] py = new float[count];
        for (int i = 0; i < count; i++) {
            float r = big * Math.min(1.0F, values[i] / (float) scale);
            px[i] = (float) Math.sin(Math.PI * 2 * i / count) * r;
            py[i] = (float) -Math.cos(Math.PI * 2 * i / count) * r;
        }
        fillPolygon(g, px, py, 0x70B02820);
        int outline = Math.max(1, Math.round(f * 0.6F));
        for (int i = 0; i < count; i++) {
            int j = (i + 1) % count;
            drawLine(g, px[i], py[i], px[j], py[j], 0xFF8A1E18, outline);
        }
        int marker = Math.max(3, Math.round(f * 1.5F));
        for (int i = 0; i < count; i++) {
            dot(g, px[i], py[i], marker, STAT_COLORS[i]);
        }
        g.pose().popPose();

        // The names and values around it
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2 * i / count;
            float sin = (float) Math.sin(angle);
            float cos = (float) Math.cos(angle);
            String name = STATS[i];
            Component label = Component.translatable("shinobiStat." + name);
            boolean dashed = LOCKED.contains(name) && values[i] == 0;
            String value = dashed ? "-" : String.valueOf(values[i]);
            int color = dashed ? 0xFF9AAA9E : STAT_COLORS[i];

            float out = Math.abs(sin) > 0.3F ? radius + 12 : radius + 8;
            float ax = cx + sin * out;
            float ay = cy - cos * out;
            int labelWidth = this.font.width(label);
            int valueWidth = this.font.width(value);
            int lx;
            int vx;
            if (sin > 0.3F) {
                lx = Math.round(ax);
                vx = lx;
            } else if (sin < -0.3F) {
                lx = Math.round(ax) - labelWidth;
                vx = Math.round(ax) - valueWidth;
            } else {
                lx = Math.round(ax) - labelWidth / 2;
                vx = Math.round(ax) - valueWidth / 2;
            }
            // Keep the names inside the panel
            lx = Mth.clamp(lx, left() + 12, left() + PANEL_W - 12 - labelWidth);
            vx = Mth.clamp(vx, left() + 12, left() + PANEL_W - 12 - valueWidth);
            int ly = Math.round(ay) - (cos > 0.3F ? 17 : cos < -0.3F ? -1 : 8);
            g.drawString(this.font, label, lx, ly, color, false);
            g.drawString(this.font, value, vx, ly + 9, INK, false);
        }
    }

    // ---------------------------------------------------------------- lines and polygons

    private void dot(GuiGraphics g, float x, float y, int size, int color) {
        int ix = Math.round(x - size / 2.0F);
        int iy = Math.round(y - size / 2.0F);
        g.fill(ix, iy, ix + size, iy + size, color);
    }

    /** A line of the given thickness, in the units of the current pose. */
    private void drawLine(GuiGraphics g, float x0, float y0, float x1, float y1, int color, int thickness) {
        int steps = (int) Math.ceil(Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0)));
        if (steps <= 0) {
            dot(g, x0, y0, thickness, color);
            return;
        }
        for (int s = 0; s <= steps; s++) {
            dot(g, x0 + (x1 - x0) * s / steps, y0 + (y1 - y0) * s / steps, thickness, color);
        }
    }

    /** A filled polygon, one row of pixels at a time. */
    private void fillPolygon(GuiGraphics g, float[] xs, float[] ys, int color) {
        int n = xs.length;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (float y : ys) {
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        float[] crossings = new float[n];
        for (int y = (int) Math.floor(minY); y <= (int) Math.ceil(maxY); y++) {
            float rowY = y + 0.5F;
            int found = 0;
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                boolean crosses = (ys[i] <= rowY && ys[j] > rowY) || (ys[j] <= rowY && ys[i] > rowY);
                if (crosses && found < n) {
                    crossings[found++] = xs[i] + (rowY - ys[i]) * (xs[j] - xs[i]) / (ys[j] - ys[i]);
                }
            }
            Arrays.sort(crossings, 0, found);
            for (int k = 0; k + 1 < found; k += 2) {
                g.fill(Math.round(crossings[k]), y, Math.round(crossings[k + 1]), y + 1, color);
            }
        }
    }
}
