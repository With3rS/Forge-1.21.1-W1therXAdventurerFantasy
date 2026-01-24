package com.w1therx.adventurerfantasy.screen.inventory.effects;

import com.w1therx.adventurerfantasy.capability.ModCapabilities;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class CustomInventoryScreen extends InventoryScreen {
    public CustomInventoryScreen(Player pPlayer) {
        super(pPlayer);
    }

    private final EffectPanelComponent effectPanel = new EffectPanelComponent();

    @Override
    protected void init() {
        super.init();
        effectPanel.init(this.leftPos, this.topPos);
        addRenderableWidget(new TexturedToggleButton(leftPos + 26, topPos + 8, 6, 6, new WidgetSprites(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "effect_panel/effect_panel_button_enabled"), ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "effect_panel/effect_panel_button_enabled_focused"), ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "effect_panel/effect_panel_button_disabled"), ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "effect_panel/effect_panel_button_disabled_focused")), b -> effectPanel.toggle(getRecipeBookComponent())));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (effectPanel.isVisible() && effectPanel.isInsideScroll((int) pMouseX, (int) pMouseY)) effectPanel.click((int)pMouseX, (int) pMouseY, pButton);
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (effectPanel.isVisible()) effectPanel.release((int)pMouseX, (int) pMouseY, pButton);
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(gui, partialTicks, mouseX, mouseY);
        if (this.minecraft != null) {
            effectPanel.render(gui, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (minecraft != null && effectPanel.mouseScrolled(scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void containerTick() {
        if (this.minecraft != null && this.minecraft.player != null) {
            if (getRecipeBookComponent().isVisible()) {
                effectPanel.setVisible(false);
            }
            if (effectPanel.isVisible()) {
                effectPanel.onTick();
            }
            this.minecraft.player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> effectPanel.updateEffects(statsI.getActiveEffectList()));
        }
    }
}
