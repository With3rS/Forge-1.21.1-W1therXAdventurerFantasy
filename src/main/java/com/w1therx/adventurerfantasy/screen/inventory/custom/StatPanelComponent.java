package com.w1therx.adventurerfantasy.screen.inventory.custom;


import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.earlydisplay.ElementShader;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StatPanelComponent {
    private static final int ICON_SIZE = 20;
    private static final int VISIBLE_ROWS = 7;
    private static final int STRING_CYCLE_DURATION = 250;

    private boolean visible = false;
    private double scrollOffset = 0;
    private Map<StatType, Double> finalStats;
    private Map<StatType, Double> baseStats;
    private Map<StatType, Double> addStats;
    private Map<StatType, Double> multStats;

    private int left;
    private int top;
    private int width;
    private int height;
    private int savedMouseY;
    private double scrollSpeed;
    private int ticks;
    private boolean hideUnchanged;

    public void init(int leftPos, int topPos) {
        this.left = leftPos - 162;
        this.top = topPos + 7;
        this.width = 120 + 12;
        this.height = (int) (VISIBLE_ROWS * ICON_SIZE + 12);
    }

    public void updateStats(Map<StatType, Double> finalStatMap, Map<StatType, Double> baseStatMap, Map<StatType, Double> addStatMap, Map<StatType, Double> multStatMap) {
        finalStats = finalStatMap;
        addStats = addStatMap;
        baseStats = baseStatMap;
        multStats = multStatMap;
    }

    public void toggle(RecipeBookComponent book) {
        visible = !visible;
        if (book.isVisible()) {
            book.toggleVisibility();
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean isVisible) {visible = isVisible;}

    private int maxScroll() {
        int rows = 0;
        if (!hideUnchanged) {
            rows = StatType.values().length;
        } else {
            for (StatType stat : StatType.values()) {
                if (stat.getBaseValue() != finalStats.get(stat)) {
                    rows++;
                }
            }
        }
        return Math.max(0, rows - VISIBLE_ROWS);
    }

    private void clampScroll() {
        scrollOffset = Math.clamp(scrollOffset, 0, maxScroll());
    }

    public boolean mouseScrolled(double delta) {
        if (!visible) return false;
        scrollSpeed -= Math.signum(delta)*0.95;
        return true;
    }

    public void onTick() {
        scrollOffset = scrollOffset + scrollSpeed/20;
        clampScroll();
        scrollSpeed = scrollSpeed * 0.95;
        ticks++;
        if (ticks>= STRING_CYCLE_DURATION) {
            ticks = 0;
        }
    }

    public void click(int mouseX, int mouseY, int button) {
        int n = 0;
        if (!hideUnchanged) {
            n = StatType.values().length;
        } else {
            for (StatType stat : StatType.values()) {
                if (stat.getBaseValue() != finalStats.get(stat)) {
                    n++;
                }
            }
        }
        if (mouseX >= left + 18 && mouseX <= left + 22 && mouseY>=(int) (top + 7 + 146*scrollOffset/n) && mouseY<= (int) (top + 7 + 146*scrollOffset/n) + (146*VISIBLE_ROWS/n)) {

            savedMouseY = mouseY;
        }
    }

    public boolean isInsideScroll(int mouseX, int mouseY) {
        int n = 0;
        if (!hideUnchanged) {
            n = StatType.values().length;
        } else {
            for (StatType stat : StatType.values()) {
                if (stat.getBaseValue() != finalStats.get(stat)) {
                    n++;
                }
            }
        }
        return mouseX >= left + 18 && mouseX <= left + 22 && mouseY>=(int) (top + 7 + 146 * scrollOffset/n) && mouseY<= (int) (top + 7 + 146 * scrollOffset/ n) + (146*VISIBLE_ROWS/n);

        }

    public void release(int mouseX, int mouseY, int button) {
            savedMouseY = 0;
    }

    public boolean areUnchangedStatsShown() {
        return !hideUnchanged;
    }

    public void render(GuiGraphics gui, int mouseX, int mouseY) {
        if (!visible) return;
        int n = 0;
        if (!hideUnchanged) {
            n = StatType.values().length;
        } else {
            for (StatType stat : StatType.values()) {
                if (stat.getBaseValue() != finalStats.get(stat)) {
                    n++;
                }
            }
        }

        if (savedMouseY > 0) {
            scrollOffset = scrollOffset + (double) (n * (mouseY - savedMouseY)) /146;
            savedMouseY = mouseY;
            clampScroll();
            }
        int heightD = 270;
        int widthD = 480;
        if (Minecraft.getInstance().screen!=null) {
            heightD = Minecraft.getInstance().screen.height;
            widthD = Minecraft.getInstance().screen.width;
        }

        System.out.print("");

        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/stat_panel.png"), left + 13, top - 7, 0, 0, 138, 166, 138, 166);
        gui.enableScissor(0, heightD/2-69, widthD, heightD/2+76);
        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/scroll.png"), left + 18, (int) (top + 7 + 146*scrollOffset/n), 0, 0, 4,(146*VISIBLE_ROWS/n), 4, 10);
        gui.disableScissor();

        int startIndex = (int) -(scrollOffset * ICON_SIZE);
        int i = 0;
        boolean tooltipIsShown = false;
            for (StatType stat : StatType.values()) {
                System.out.print("");
                double defaultValue = stat.getBaseValue();
                double value = finalStats.get(stat);

                if (!(hideUnchanged && defaultValue == value)) {

                    int x = left + 24;
                int y = top + 8 + i * ICON_SIZE + startIndex;

                if (y > 270) break;

                i++;
                gui.enableScissor(0, heightD / 2 - 68, widthD, heightD / 2 + 75);

                if (isHovered(x, y, mouseX, mouseY) && !tooltipIsShown) {
                    gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/panel_selected_stat_background.png"), x, y, 0, 0, 120, 20, 120, 20);
                } else {
                    gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/panel_stat_background.png"), x, y, 0, 0, 120, 20, 120, 20);
                }

                ResourceLocation icon = stat.getIcon();
                gui.blit(icon, x + 2, y + 2, 0, 0, 16, 16, 16, 16);

                int shortLen = Minecraft.getInstance().font.width(stat.getShortName());
                String shortName = stat.getShortName();
                int integer = (int) value;
                double percentage = 100 * value;
                double twentieth = value / 20;
                String displayedValue = stat.getValue();
                displayedValue = displayedValue.replace("$value", value + "").replace("$int", integer + "").replace("$percentage", percentage + "").replace("$1/20", twentieth + "");
                int valueLen = Minecraft.getInstance().font.width(displayedValue);

                if (shortLen <= 78 - valueLen / 2) {
                    gui.drawString(Minecraft.getInstance().font, shortName, x + 22, y + 6, 0xFFFFFF);
                } else {
                    gui.enableScissor(x + 22, y + 5, x + 84, y + 15);
                    gui.drawString(Minecraft.getInstance().font, shortName, (int) (x + 22 - (shortLen + Minecraft.getInstance().font.width("   ")) * ticks / STRING_CYCLE_DURATION), y + 6, 0xFFFFFF);
                    gui.drawString(Minecraft.getInstance().font, shortName, (int) (x + 22 + Minecraft.getInstance().font.width("   ") + shortLen - (shortLen + Minecraft.getInstance().font.width("   ")) * ticks / STRING_CYCLE_DURATION), y + 6, 0xFFFFFF);
                    gui.disableScissor();
                }

                int color = 0xFFFFFF;
                if (value > defaultValue) {
                    color = 0x062ecf;
                } else if (value < defaultValue) {
                    color = 0xcf0606;
                }

                if (valueLen <= 31) {
                    gui.drawCenteredString(Minecraft.getInstance().font, displayedValue, x + 102, y + 6, color);
                } else {
                    gui.enableScissor(x + 86, y + 5, x + 117, y + 15);
                    gui.drawString(Minecraft.getInstance().font, displayedValue, (int) (x + 86 - (valueLen + Minecraft.getInstance().font.width("   ")) * ticks / STRING_CYCLE_DURATION), y + 6, color);
                    gui.drawString(Minecraft.getInstance().font, displayedValue, (int) (x + 86 + valueLen + Minecraft.getInstance().font.width("   ") - (valueLen + Minecraft.getInstance().font.width("   ")) * ticks / STRING_CYCLE_DURATION), y + 6, color);
                    gui.disableScissor();
                }

                gui.disableScissor();


                if (isHovered(x, y, mouseX, mouseY) && !tooltipIsShown) {
                    gui.renderTooltip(Minecraft.getInstance().font, buildTooltip(stat), Optional.empty(), mouseX, mouseY);
                    gui.renderTooltip(Minecraft.getInstance().font, List.of(Component.literal("")), Optional.empty(), -10, -10);
                    tooltipIsShown = true;
                }
            }
        }
    }

    private boolean isHovered(int x, int y, int mouseX, int mouseY) {
        return (mouseX >= x && mouseX <= x + 120 && mouseY >= y && mouseY <= y + 20 && mouseX>= left + 24 && mouseX <= left + 144 && mouseY>= top + 8 && mouseY <= top + 8 + 144);
    }

    private List<Component> buildTooltip(StatType stat) {

        String name = stat.getName();
        String description = stat.getDescription();
        double value = finalStats.get(stat);
        int integer = (int) value;
        double percentage = 100 * value;
        double twentieth = value/20;
        name = name.replace("$value", value + "").replace("$int", integer + "").replace("$percentage", percentage + "").replace("$1/20", twentieth + "");
        description = description.replace("$value", value + "").replace("$int", integer + "").replace("$percentage", percentage + "").replace("$1/20", twentieth + "");
        double base = baseStats.get(stat);
        double add = addStats.get(stat);
        double mult = multStats.get(stat);
        int color = 0x06c8cf;
        double min = stat.getMinValue();
        double max = stat.getMaxValue();

        String minS = min +"";
        if (min == -Double.MAX_VALUE) {
            minS = "Negative Infinity";
        }

        String maxS = max +"";
        if (max == Double.MAX_VALUE) {
            maxS = "Positive Infinity";
        }

        double defaultValue = stat.getBaseValue();
        String defaultValueS = "Default value: " + stat.getBaseValue();

        if (value > defaultValue) {
            color = 0x062ecf;
        } else if (value < defaultValue) {
            color = 0xcf0606;
        }

        return List.of(Component.literal(name).withColor(color), Component.literal(description), Component.literal(defaultValueS +"; Minimum value: " + minS + "; Maximum value: " + maxS + ";"), Component.literal("(" + base + " + " + add + ") * " + mult + " = " + ((base + add) * mult) + ".").withColor(0x5c5c5c));
    }

    public void toggleHideStats() {
        hideUnchanged = !hideUnchanged;
    }
}
