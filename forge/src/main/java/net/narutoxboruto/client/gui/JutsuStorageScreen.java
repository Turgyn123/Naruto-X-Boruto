package net.narutoxboruto.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.networking.jutsu.JutsuStorageMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class JutsuStorageScreen extends AbstractContainerScreen<JutsuStorageMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    private static final int SLOTS_PER_PAGE = 54;
    private int currentPage = 0;

    private Button prevButton;
    private Button nextButton;

    public JutsuStorageScreen(JutsuStorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        prevButton = Button.builder(Component.literal("<"), button -> {
            if (currentPage > 0) {
                currentPage--;
                menu.setCurrentPage(currentPage);
                updateButtonVisibility();
            }
        }).bounds(x - 25, y + 60, 20, 20).build();

        nextButton = Button.builder(Component.literal(">"), button -> {
            if (currentPage < getMaxPages() - 1) {
                currentPage++;
                menu.setCurrentPage(currentPage);
                updateButtonVisibility();
            }
        }).bounds(x + imageWidth + 5, y + 60, 20, 20).build();

        this.addRenderableWidget(prevButton);
        this.addRenderableWidget(nextButton);

        updateButtonVisibility();
    }

    private int getMaxPages() {
        return menu.getMaxPages();
    }

    private void updateButtonVisibility() {
        prevButton.visible = currentPage > 0;
        nextButton.visible = currentPage < getMaxPages() - 1;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        if (getMaxPages() > 1) {
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;
            String pageText = "Page " + (currentPage + 1) + "/" + getMaxPages();
            int textWidth = this.font.width(pageText);
            guiGraphics.drawString(this.font, pageText, x + (imageWidth - textWidth) / 2, y - 10, 0xFFFFFF, true);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}
