package net.narutoxboruto.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.networking.jutsu.JutsuStorageMenu;
import net.narutoxboruto.util.JutsuSearch;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Screen for the Jutsu Storage - like a double chest GUI but for jutsus.
 * Supports pagination for future-proofing as more jutsus are added.
 * Only accepts AbstractJutsuItem instances.
 */
@OnlyIn(Dist.CLIENT)
public class JutsuStorageScreen extends AbstractContainerScreen<JutsuStorageMenu> {

    // Use generic container texture (similar to chest)
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/jutsu_storage.png");
    
    private static final int SLOTS_PER_PAGE = 54; // 6 rows of 9
    private int currentPage = 0;
    
    private Button prevButton;
    private Button nextButton;
    private EditBox searchBox;
    /** What is typed in the search bar. Kept when the screen is resized. */
    private String searchText = "";

    public JutsuStorageScreen(JutsuStorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // Set dimensions for 6-row container
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        // Adjust title position if needed
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // Add page navigation buttons
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
        
        // The search bar, above the storage: slots that do not match are dimmed
        searchBox = new EditBox(this.font, x + 23, y - 18, 130, 14, Component.translatable("gui.narutoxboruto.jutsu_search"));
        searchBox.setMaxLength(32);
        searchBox.setHint(Component.translatable("gui.narutoxboruto.jutsu_search").withStyle(net.minecraft.ChatFormatting.GRAY));
        searchBox.setValue(searchText);
        searchBox.setResponder(text -> searchText = text);
        this.addRenderableWidget(searchBox);

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
        renderSearchMarks(guiGraphics);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        
        // Render page indicator if more than 1 page
        if (getMaxPages() > 1) {
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;
            String pageText = "Page " + (currentPage + 1) + "/" + getMaxPages();
            int textWidth = this.font.width(pageText);
            guiGraphics.drawString(this.font, pageText, x + (imageWidth - textWidth) / 2, y - 30, 0xFFFFFF, true);
        }
    }

    /** With something typed, the slots that do not match are dimmed and the ones that do get a gold edge. */
    private void renderSearchMarks(GuiGraphics guiGraphics) {
        if (searchText.trim().isEmpty()) return;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.leftPos, this.topPos, 200.0F);
        for (int i = 0; i < 54 && i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (slot.getItem().isEmpty()) continue;
            if (JutsuSearch.matches(slot.getItem(), searchText)) {
                guiGraphics.renderOutline(slot.x - 1, slot.y - 1, 18, 18, 0xFFFFC832);
            } else {
                guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0xB02B1D14);
            }
        }
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // While typing in the search bar, keys must not close the screen or move items (the inventory key, 1-9)
        if (searchBox != null && searchBox.isFocused() && keyCode != 256) {
            return searchBox.keyPressed(keyCode, scanCode, modifiers) || searchBox.canConsumeInput();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Draw title centered
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        // Draw inventory label
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}
