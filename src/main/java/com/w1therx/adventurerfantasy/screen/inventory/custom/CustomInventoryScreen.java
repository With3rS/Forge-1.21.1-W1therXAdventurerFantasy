package com.w1therx.adventurerfantasy.screen.inventory.custom;


import com.w1therx.adventurerfantasy.capability.ModCapabilities;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class CustomInventoryScreen extends InventoryScreen {
    private int ticks;

    public CustomInventoryScreen(Player pPlayer) {
        super(pPlayer);
    }

    private final EffectPanelComponent effectPanel = new EffectPanelComponent();
    private final StatPanelComponent statPanel = new StatPanelComponent();
    private Button button;

    @Override
    protected void init() {
        super.init();
        effectPanel.init(this.leftPos, this.topPos);
        statPanel.init(this.leftPos, this.topPos);
        addRenderableWidget(new TexturedToggleButton(leftPos + 26, topPos + 8, 6, 6, new WidgetSprites(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "effect_panel/effect_panel_button_enabled"), ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "effect_panel/effect_panel_button_enabled_focused"), ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "effect_panel/effect_panel_button_disabled"), ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "effect_panel/effect_panel_button_disabled_focused")), b -> {effectPanel.toggle(getRecipeBookComponent()); statPanel.setVisible(false);}));
        addRenderableWidget(new TexturedToggleButton(leftPos + 26 + 43, topPos + 8, 6, 6, new WidgetSprites(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "stat_panel/stat_panel_button_enabled"), ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "stat_panel/effect_panel_button_enabled_focused"), ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "stat_panel/stat_panel_button_disabled"), ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "stat_panel/stat_panel_button_disabled_focused")), b -> {statPanel.toggle(getRecipeBookComponent()); effectPanel.setVisible(false);}));
        button = addRenderableWidget(new TexturedToggleButton(leftPos + 20 -41, topPos + 8 - 4, 6, 6, new WidgetSprites(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "hide_stats/hide_stats_button_disabled"), ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "hide_stats/hide_stats_enabled_focused"), ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "hide_stats/hide_stats_button_enabled"), ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "hide_stats/hide_stats_button_disabled_focused")), d -> {
            statPanel.toggleHideStats();
        }));
        button.visible = false;
        button.active = false;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (effectPanel.isVisible() && effectPanel.isInsideScroll((int) pMouseX, (int) pMouseY)) {
            effectPanel.click((int) pMouseX, (int) pMouseY, pButton);
        } else if (statPanel.isVisible() &&  statPanel.isInsideScroll((int) pMouseX, (int) pMouseY)) {
            statPanel.click((int) pMouseX, (int) pMouseY, pButton);
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (effectPanel.isVisible()) effectPanel.release((int)pMouseX, (int) pMouseY, pButton);
        if (statPanel.isVisible()) statPanel.release((int)pMouseX, (int) pMouseY, pButton);
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(gui, partialTicks, mouseX, mouseY);
        if (this.minecraft != null) {
            effectPanel.render(gui, mouseX, mouseY);
            statPanel.render(gui, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (minecraft != null && (effectPanel.mouseScrolled(scrollY) || statPanel.mouseScrolled(scrollY))) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void containerTick() {
        if (this.minecraft != null && this.minecraft.player != null) {
            if (getRecipeBookComponent().isVisible()) {
                effectPanel.setVisible(false);
                statPanel.setVisible(false);
            }
            if (effectPanel.isVisible()) {
                effectPanel.onTick();
            }
            if (statPanel.isVisible()) {
                statPanel.onTick();
                button.active = true;
                button.visible = true;
                if (statPanel.areUnchangedStatsShown()) {
                    button.setTooltip(Tooltip.create(Component.literal("Showing all stats")));
                } else {
                    button.setTooltip(Tooltip.create(Component.literal("Hiding unchanged stats")));
                }
            } else {
                button.active = false;
                button.visible = false;
            }
            ticks++;
            if (ticks>=10) {
                ticks = 0;
                this.minecraft.player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                    effectPanel.updateEffects(statsI.getActiveEffectList());
                });
                this.minecraft.player.getCapability(ModCapabilities.FINAL_STATS).ifPresent(statsF ->  this.minecraft.player.getCapability(ModCapabilities.BASE_STATS).ifPresent(statsB ->  this.minecraft.player.getCapability(ModCapabilities.ADD_STATS).ifPresent(statsA ->  this.minecraft.player.getCapability(ModCapabilities.MULT_STATS).ifPresent(statsM -> {
                    statPanel.updateStats(statsF.getFinalStatMap(), statsB.getBaseStatMap(), statsA.getAddStatMap(), statsM.getMultStatMap());
                }))));
            }
        }
    }
}
