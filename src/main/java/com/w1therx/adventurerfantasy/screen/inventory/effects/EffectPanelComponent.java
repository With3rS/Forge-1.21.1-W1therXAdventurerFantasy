package com.w1therx.adventurerfantasy.screen.inventory.effects;


import com.w1therx.adventurerfantasy.effect.general.StatusEffectInstanceEntry;
import com.w1therx.adventurerfantasy.util.ModGeneralUtils;
import com.w1therx.adventurerfantasy.util.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EffectPanelComponent {
    private static final int ICON_SIZE = 24;
    private static final int COLUMNS = 5;
    private static final int VISIBLE_ROWS = 6;

    private boolean visible = false;
    private int scrollOffset = 0;
    private Map<MobEffect, StatusEffectInstanceEntry> effects;

    private int left;
    private int top;
    private int width;
    private int height;

    public void init(int leftPos, int topPos) {
        this.left = leftPos - 162;
        this.top = topPos + 7;
        this.width = COLUMNS * ICON_SIZE + 12;
        this.height = VISIBLE_ROWS * ICON_SIZE + 12;
    }

    public void updateEffects(Map<MobEffect, StatusEffectInstanceEntry> effectMap) {
        effects = effectMap;
    }

    public void toggle() {
        visible = !visible;
    }

    public boolean isVisible() {
        return visible;
    }

    private int maxScroll() {
        int rows = (int) Math.ceil((double) effects.size()/COLUMNS);
        return Math.max(0, rows - VISIBLE_ROWS);
    }

    private void clampScroll() {
        scrollOffset = Math.clamp(scrollOffset, 0, maxScroll());
    }

    public boolean mouseScrolled(double delta) {
        if (!visible) return false;
        scrollOffset -= (int) Math.signum(delta);
        clampScroll();
        return true;
    }

    public void render(GuiGraphics gui, int mouseX, int mouseY) {
        if (!visible) return;

        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/side_screen.png"), left + 17, top - 7, 0, 0, 134, 166, 134, 166);
        int startIndex = scrollOffset * COLUMNS;
        int maxVisible = VISIBLE_ROWS * COLUMNS;
        int i = 0;
            for (MobEffect effect : (new HashMap<>(effects)).keySet()) {
                Optional<ResourceKey<MobEffect>> optional = BuiltInRegistries.MOB_EFFECT.getResourceKey(effect);
                if (optional.isPresent()) {
                    Holder<MobEffect> holder = optional.get().getOrThrow(Minecraft.getInstance().player);
                    if (!holder.is(ModTags.EFFECTS_NOT_SHOWN_IN_GUI)) {
                        System.out.print("");
                        if (i >= maxVisible) break;

                        int col = i % COLUMNS;
                        int row = i / COLUMNS;

                        int x = left + col * ICON_SIZE + 24;
                        int y = top + row * ICON_SIZE;

                        i++;

                        if (holder.is(ModTags.SPECIAL_BUFFS)) {
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/panel_special_buff_background.png"), x, y, 0, 0, 24, 24, 24, 24);
                        } else if (holder.is(ModTags.SPECIAL_NEUTRAL_EFFECT)) {
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/panel_special_neutral_effect_background.png"), x, y, 0, 0, 24, 24, 24, 24);
                        } else if (holder.is(ModTags.SPECIAL_DEBUFFS)) {
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/panel_special_debuff_background.png"), x, y, 0, 0, 24, 24, 24, 24);
                        } else {
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/panel_effect_icon_background.png"), x, y, 0, 0, 24, 24, 24, 24);
                        }

                        ResourceLocation icon = ModGeneralUtils.getEffectIcon(effect);
                        if (ModGeneralUtils.isVanillaEffect(effect)) {
                            gui.blit(icon, x + 3, y + 3, 0, 0, 18, 18, 18, 18);
                        } else {
                            gui.blit(icon, x + 4, y + 4, 0, 0, 16, 16, 16, 16);
                        }

                        if (holder.is(ModTags.BUFFS) || holder.is(ModTags.GENERAL_BUFF) || holder.is(ModTags.SPECIAL_BUFFS)) {
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/buff_icon_background.png"), x + 1, y +1, 0, 0, 22, 22, 22, 22);
                        } else if (holder.is(ModTags.DEBUFFS) || holder.is(ModTags.GENERAL_DEBUFF) || holder.is(ModTags.CC_DEBUFFS) || holder.is(ModTags.SPECIAL_DEBUFFS)) {
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/debuff_icon_background.png"), x + 1, y +1, 0, 0, 22, 22, 22, 22);
                        }


                        if (isHovered(x, y, mouseX, mouseY)) {
                            List<Component> tooltip = List.of(Component.translatable(effect.getDescriptionId()), Component.literal("Amplifier: " + effects.get(effect).amplifier()), Component.literal("Duration: " + ModGeneralUtils.formatDuration(effects.get(effect).duration())));
                            gui.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), mouseX, mouseY);
                            gui.renderTooltip(Minecraft.getInstance().font, List.of(Component.literal("")), Optional.empty(), -10, -10);
                        }
                    }
                }
        }
    }

    private boolean isHovered(int x, int y, int mouseX, int mouseY) {
        return (mouseX >= x && mouseX <= x + ICON_SIZE && mouseY >= y && mouseY <= y + ICON_SIZE);
    }

    private void renderTooltip(GuiGraphics gui, MobEffect effect, int mouseX, int mouseY) {
            List<Component> tooltip = List.of(Component.translatable(effect.getDescriptionId()), Component.literal("Amplifier: " + effects.get(effect).amplifier()), Component.literal("Duration: " + ModGeneralUtils.formatDuration(effects.get(effect).duration())));
            gui.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), mouseX, mouseY);
    }
}
