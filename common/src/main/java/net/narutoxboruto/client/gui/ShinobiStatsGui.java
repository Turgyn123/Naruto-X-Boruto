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
import net.narutoxboruto.capabilities.info.ReleaseList;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The character screen, opened with the stats key. A parchment panel with tabs on its right side:
 * <ul>
 *   <li><b>Information</b>: name, affiliation, clan, rank, releases, dojutsu and a model of the player.</li>
 *   <li><b>Statistics</b>: the ten stats on a radar chart.</li>
 *   <li><b>Dojutsu</b>: opens the dojutsu menu.</li>
 * </ul>
 */
public class ShinobiStatsGui extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/gui/character_screen.png");

    private static final int PANEL_W = 240;
    private static final int PANEL_H = 232;
    private static final int TAB_W = 28;
    private static final int TAB_H = 24;
    private static final int TAB_GAP = 3;

    private static final int INK = 0xFF2B1D14;
    private static final int LABEL = 0xFF5A4028;
    private static final int LINE = 0xFF6E502D;
    private static final int PAPER_DARK = 0xFFC8AE80;
    private static final int PAPER_LIGHT = 0xFFECDCB8;

    private static final int TAB_INFO = 0;
    private static final int TAB_STATS = 1;
    private static final int TAB_DOJUTSU = 2;
    private static final String[] TAB_KEYS = {"gui.narutoxboruto.tab_info", "gui.narutoxboruto.tab_stats", "gui.narutoxboruto.tab_dojutsu"};

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

    private static int rememberedTab = TAB_INFO;

    private final LocalPlayer player;
    private int tab = rememberedTab;

    public ShinobiStatsGui() {
        super(Component.translatable("gui.narutoxboruto.shinobi_stats"));
        this.player = Minecraft.getInstance().player;
    }

    private int left() {
        return (this.width - PANEL_W) / 2;
    }

    private int top() {
        return (this.height - PANEL_H) / 2;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(left() + (PANEL_W - 90) / 2, top() + PANEL_H - 26, 90, 18).build());
        super.init();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---------------------------------------------------------------- tabs

    private int tabX(int index) {
        // The selected tab sticks out further from the panel
        return left() + PANEL_W - (index == tab ? 4 : 7);
    }

    private int tabY(int index) {
        return top() + 12 + index * (TAB_H + TAB_GAP);
    }

    private boolean overTab(int index, double mouseX, double mouseY) {
        return mouseX >= tabX(index) && mouseX < tabX(index) + TAB_W && mouseY >= tabY(index) && mouseY < tabY(index) + TAB_H;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < TAB_KEYS.length; i++) {
                if (!overTab(i, mouseX, mouseY)) continue;
                if (i == TAB_DOJUTSU) {
                    Minecraft.getInstance().setScreen(new DojutsuScreen());
                } else {
                    tab = i;
                    rememberedTab = i;
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ---------------------------------------------------------------- drawing

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(g, mouseX, mouseY, partialTick);
        // The tabs first, so the panel covers the part of them that is under it
        for (int i = 0; i < TAB_KEYS.length; i++) {
            g.blit(TEXTURE, tabX(i), tabY(i), i == tab ? TAB_W : 0, PANEL_H, TAB_W, TAB_H);
            g.blit(TEXTURE, tabX(i) + 8, tabY(i) + 4, 56 + i * 16, PANEL_H, 16, 16);
        }
        g.blit(TEXTURE, left(), top(), 0, 0, PANEL_W, PANEL_H);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        Component title = Component.translatable(TAB_KEYS[tab]);
        g.drawString(this.font, title, (this.width - this.font.width(title)) / 2, top() + 8, INK, false);

        if (player != null) {
            if (tab == TAB_INFO) drawInformation(g, mouseX, mouseY);
            else drawStatistics(g);
        }

        for (int i = 0; i < TAB_KEYS.length; i++) {
            if (overTab(i, mouseX, mouseY)) {
                g.renderTooltip(this.font, Component.translatable(TAB_KEYS[i]), mouseX, mouseY);
            }
        }
    }

    // ---------------------------------------------------------------- information tab

    private void drawInformation(GuiGraphics g, int mouseX, int mouseY) {
        int x = left() + 16;
        int y = top() + 28;
        int rowHeight = 17;

        // Name
        g.drawString(this.font, player.getName(), x, y, INK, false);

        // Affiliation and clan, each with its icon
        String affiliation = Services.PLATFORM.getAffiliation(player).getValue();
        String clan = Services.PLATFORM.getClan(player).getValue();
        String rank = Services.PLATFORM.getRank(player).getValue();

        y += rowHeight;
        drawRow(g, x, y, "shinobiStat.affiliation", "affiliation." + affiliation, "affiliations", affiliation, ModUtil.AFF_LIST);
        y += rowHeight;
        drawRow(g, x, y, "shinobiStat.clan", "clan." + clan, "clans", clan, ModUtil.CLAN_LIST);
        y += rowHeight;
        drawRow(g, x, y, "shinobiStat.rank", "rank." + rank, null, rank, null);

        // Releases
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

        // Kekkei genkai and dojutsu
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

        // The model of the player in a frame
        int frameX = left() + PANEL_W - 82;
        int frameY = top() + 26;
        drawFrame(g, frameX, frameY, 66, 100);
        InventoryScreen.renderEntityInInventoryFollowsMouse(g, frameX + 1, frameY + 1, frameX + 65, frameY + 99,
                36, 0.0625F, mouseX, mouseY, player);

        // Bottom boxes: shinobi points and chakra
        int boxY = top() + 140;
        g.fill(left() + 12, boxY - 6, left() + PANEL_W - 12, boxY - 5, LINE);
        int boxW = (PANEL_W - 24 - 8) / 2;
        drawBox(g, left() + 12, boxY, boxW, Component.translatable("shinobiStat.shinobi_points"),
                String.valueOf(Services.PLATFORM.getShinobiPoints(player).getValue()));
        drawBox(g, left() + 12 + boxW + 8, boxY, boxW, Component.translatable("gui.narutoxboruto.chakra"),
                Services.PLATFORM.getChakra(player).getValue() + " / " + Services.PLATFORM.getMaxChakra(player).getValue());
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
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF2B1D14);
        g.fill(x, y, x + w, y + h, 0xFFEADBB4);
        g.fill(x, y, x + w, y + 1, 0xFF8A6A3B);
        g.fill(x, y, x + 1, y + h, 0xFF8A6A3B);
    }

    private void drawBox(GuiGraphics g, int x, int y, int w, Component label, String value) {
        g.fill(x - 1, y - 1, x + w + 1, y + 29, 0xFF2B1D14);
        g.fill(x, y, x + w, y + 28, PAPER_LIGHT);
        g.fill(x, y, x + w, y + 1, 0xFF8A6A3B);
        g.fill(x, y, x + 1, y + 28, 0xFF8A6A3B);
        g.drawString(this.font, label, x + 5, y + 5, LABEL, false);
        g.drawString(this.font, value, x + 5, y + 16, INK, false);
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
        float cy = top() + 126;
        float radius = 58.0F;

        // Shinobi points under the title
        Component points = Component.translatable("shinobiStat.shinobi_points").append(": "
                + Services.PLATFORM.getShinobiPoints(player).getValue());
        g.drawString(this.font, points, (this.width - this.font.width(points)) / 2, top() + 22, LABEL, false);

        // The web: rings and spokes
        for (int ring = 1; ring <= 4; ring++) {
            float r = radius * ring / 4.0F;
            float[] xs = new float[count];
            float[] ys = new float[count];
            for (int i = 0; i < count; i++) {
                xs[i] = cx + (float) Math.sin(Math.PI * 2 * i / count) * r;
                ys[i] = cy - (float) Math.cos(Math.PI * 2 * i / count) * r;
            }
            for (int i = 0; i < count; i++) {
                int j = (i + 1) % count;
                drawLine(g, xs[i], ys[i], xs[j], ys[j], ring == 4 ? 0xFF6E502D : 0x886E502D);
            }
        }
        for (int i = 0; i < count; i++) {
            drawLine(g, cx, cy, cx + (float) Math.sin(Math.PI * 2 * i / count) * radius,
                    cy - (float) Math.cos(Math.PI * 2 * i / count) * radius, 0x666E502D);
        }

        // The stats
        float[] px = new float[count];
        float[] py = new float[count];
        for (int i = 0; i < count; i++) {
            float r = radius * Math.min(1.0F, values[i] / (float) scale);
            px[i] = cx + (float) Math.sin(Math.PI * 2 * i / count) * r;
            py[i] = cy - (float) Math.cos(Math.PI * 2 * i / count) * r;
        }
        fillPolygon(g, px, py, 0x70B02820);
        for (int i = 0; i < count; i++) {
            int j = (i + 1) % count;
            drawLine(g, px[i], py[i], px[j], py[j], 0xFF8A1E18);
        }
        for (int i = 0; i < count; i++) {
            g.fill(Math.round(px[i]) - 1, Math.round(py[i]) - 1, Math.round(px[i]) + 2, Math.round(py[i]) + 2, STAT_COLORS[i]);
        }

        // The names and values around it
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2 * i / count;
            float sin = (float) Math.sin(angle);
            float cos = (float) Math.cos(angle);
            String name = STATS[i];
            Component label = Component.translatable("shinobiStat." + name);
            boolean dashed = LOCKED.contains(name) && values[i] == 0;
            String value = dashed ? "-" : String.valueOf(values[i]);
            int color = dashed ? 0xFF8C7A5A : STAT_COLORS[i];

            float ax = cx + sin * (radius + 8);
            float ay = cy - cos * (radius + 8);
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
            int ly = Math.round(ay) - (cos > 0.3F ? 17 : cos < -0.3F ? -1 : 8);
            g.drawString(this.font, label, lx, ly, color, false);
            g.drawString(this.font, value, vx, ly + 9, INK, false);
        }
    }

    // ---------------------------------------------------------------- lines and polygons

    /** A one pixel line. */
    private void drawLine(GuiGraphics g, float x0, float y0, float x1, float y1, int color) {
        int steps = (int) Math.ceil(Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0)));
        if (steps <= 0) {
            g.fill(Math.round(x0), Math.round(y0), Math.round(x0) + 1, Math.round(y0) + 1, color);
            return;
        }
        for (int s = 0; s <= steps; s++) {
            int x = Math.round(x0 + (x1 - x0) * s / steps);
            int y = Math.round(y0 + (y1 - y0) * s / steps);
            g.fill(x, y, x + 1, y + 1, color);
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
