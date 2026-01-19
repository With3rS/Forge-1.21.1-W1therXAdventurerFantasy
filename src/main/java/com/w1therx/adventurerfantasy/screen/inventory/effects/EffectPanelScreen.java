package com.w1therx.adventurerfantasy.screen.inventory.effects;

import com.w1therx.adventurerfantasy.capability.ModCapabilities;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class EffectPanelScreen extends InventoryScreen {
    public EffectPanelScreen(Player pPlayer) {
        super(pPlayer);
    }

    private final EffectPanelComponent effectPanel = new EffectPanelComponent();

    @Override
    protected void init() {
        super.init();
        effectPanel.init(this.leftPos, this.topPos);

        addRenderableWidget(Button.builder(Component.literal("Effects"), b -> effectPanel.toggle()).bounds(leftPos-24, topPos + 5, 20, 20).build());
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
        if (minecraft != null && effectPanel.mouseScrolled(Math.sqrt(scrollX*scrollX + scrollY*scrollY))) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void containerTick() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> effectPanel.updateEffects(statsI.getActiveEffectList()));
        }
    }
}
