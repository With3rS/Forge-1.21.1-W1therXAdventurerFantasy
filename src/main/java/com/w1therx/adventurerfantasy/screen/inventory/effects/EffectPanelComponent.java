package com.w1therx.adventurerfantasy.screen.inventory.effects;


import com.w1therx.adventurerfantasy.effect.general.ICustomStatusEffect;
import com.w1therx.adventurerfantasy.effect.general.StatusEffectInstanceEntry;
import com.w1therx.adventurerfantasy.util.ModGeneralUtils;
import com.w1therx.adventurerfantasy.util.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EffectPanelComponent {
    private static final int ICON_SIZE = 24;
    private static final int COLUMNS = 5;
    private static final int VISIBLE_ROWS = 6;

    private boolean visible = false;
    private double scrollOffset = 0;
    private Map<MobEffect, StatusEffectInstanceEntry> effects;

    private int left;
    private int top;
    private int width;
    private int height;
    private int savedMouseY;
    private double scrollSpeed;

    public void init(int leftPos, int topPos) {
        this.left = leftPos - 162;
        this.top = topPos + 7;
        this.width = COLUMNS * ICON_SIZE + 12;
        this.height = VISIBLE_ROWS * ICON_SIZE + 12;
    }

    public void updateEffects(Map<MobEffect, StatusEffectInstanceEntry> effectMap) {
        effects = effectMap;
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
        int rows = (int) Math.ceil((double) effects.size()/COLUMNS);
        return Math.max(0, rows - VISIBLE_ROWS);
    }

    private void clampScroll() {
        scrollOffset = Math.clamp(scrollOffset, 0, maxScroll());
    }

    public boolean mouseScrolled(double delta) {
        if (!visible) return false;
        scrollSpeed -= Math.signum(delta)*0.7;
        return true;
    }

    public void onTick() {
        scrollOffset = scrollOffset + scrollSpeed/20;
        clampScroll();
        scrollSpeed = scrollSpeed * 0.9;
    }

    public void click(int mouseX, int mouseY, int button) {
        if (mouseX >= left + 18 && mouseX <= left + 22 && mouseY>=(int) (top + 7 + scrollOffset/Math.ceil((double) effects.size() /COLUMNS)*146) && mouseY<= (int) (top + 7 + scrollOffset/Math.ceil((double) effects.size() /COLUMNS)*146) + (int) (VISIBLE_ROWS/(Math.ceil((double) effects.size() /COLUMNS))*146)) {

            savedMouseY = mouseY;
        }
    }

    public boolean isInsideScroll(int mouseX, int mouseY) {
        return mouseX >= left + 18 && mouseX <= left + 22 && mouseY>=(int) (top + 7 + scrollOffset/Math.ceil((double) effects.size() /COLUMNS)*146) && mouseY<= (int) (top + 7 + scrollOffset/Math.ceil((double) effects.size() /COLUMNS)*146) + (int) (VISIBLE_ROWS/(Math.ceil((double) effects.size() /COLUMNS))*146);

        }

    public void release(int mouseX, int mouseY, int button) {
            savedMouseY = 0;
    }

    public void render(GuiGraphics gui, int mouseX, int mouseY) {
        if (!visible) return;

        if (savedMouseY > 0) {
            scrollOffset = scrollOffset + Math.ceil((double) effects.size() / COLUMNS) * (mouseY - savedMouseY)/146;
            savedMouseY = mouseY;
            clampScroll();
            }
        int heightD = 270;
        int widthD = 480;
        if (Minecraft.getInstance().screen!=null) {
            heightD = Minecraft.getInstance().screen.height;
            widthD = Minecraft.getInstance().screen.width;
        }

        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/effect_panel.png"), left + 13, top - 7, 0, 0, 138, 166, 138, 166);
        gui.enableScissor(0, heightD/2-69, widthD, heightD/2+76);
        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/scroll.png"), left + 18, (int) (top + 7 + scrollOffset/Math.ceil((double) effects.size() /COLUMNS)*146), 0, 0, 4, (int) (VISIBLE_ROWS/(Math.ceil((double) effects.size() /COLUMNS))*146), 4, 10);
        gui.disableScissor();

        int startIndex = (int) -(scrollOffset * ICON_SIZE);
        int i = 0;
        boolean tooltipIsShown = false;
            for (MobEffect effect : (new HashMap<>(effects)).keySet()) {
                Optional<ResourceKey<MobEffect>> optional = BuiltInRegistries.MOB_EFFECT.getResourceKey(effect);
                if (optional.isPresent()) {
                    Holder<MobEffect> holder = optional.get().getOrThrow(Minecraft.getInstance().player);
                    if (!holder.is(ModTags.EFFECTS_NOT_SHOWN_IN_GUI)) {
                        System.out.print("");

                        int col = Math.abs(i % COLUMNS);
                        int row = i / COLUMNS;

                        int x = left + col * ICON_SIZE + 24;
                        int y = top + row * ICON_SIZE + 8 + startIndex;

                        if (y > 270) break;

                        i++;
                        gui.enableScissor(0, heightD/2 - 68, widthD, heightD/2 + 75);

                        if (isHovered(x, y, mouseX, mouseY) && !tooltipIsShown) {
                            if (holder.is(ModTags.SPECIAL_BUFFS)) {
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/panel_selected_special_buff_background.png"), x, y, 0, 0, 24, 24, 24, 24);
                            } else if (holder.is(ModTags.SPECIAL_NEUTRAL_EFFECT)) {
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/panel_selected_special_neutral_effect_background.png"), x, y, 0, 0, 24, 24, 24, 24);
                            } else if (holder.is(ModTags.SPECIAL_DEBUFFS)) {
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/panel_selected_special_debuff_background.png"), x, y, 0, 0, 24, 24, 24, 24);
                            } else {
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/panel_selected_effect_icon_background.png"), x, y, 0, 0, 24, 24, 24, 24);
                            }
                        } else {
                            if (holder.is(ModTags.SPECIAL_BUFFS)) {
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/panel_special_buff_background.png"), x, y, 0, 0, 24, 24, 24, 24);
                            } else if (holder.is(ModTags.SPECIAL_NEUTRAL_EFFECT)) {
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/panel_special_neutral_effect_background.png"), x, y, 0, 0, 24, 24, 24, 24);
                            } else if (holder.is(ModTags.SPECIAL_DEBUFFS)) {
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/panel_special_debuff_background.png"), x, y, 0, 0, 24, 24, 24, 24);
                            } else {
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/panel_effect_icon_background.png"), x, y, 0, 0, 24, 24, 24, 24);
                            }
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

                        gui.disableScissor();


                        if (isHovered(x, y, mouseX, mouseY) && y >= heightD/2-68 && y<=heightD/2+75 && !tooltipIsShown) {
                            gui.renderTooltip(Minecraft.getInstance().font, buildTooltip(effect, Minecraft.getInstance().player), Optional.empty(), mouseX, mouseY);
                            gui.renderTooltip(Minecraft.getInstance().font, List.of(Component.literal("")), Optional.empty(), -10, -10);
                            tooltipIsShown = true;
                        }
                    }
                }
        }
    }

    private boolean isHovered(int x, int y, int mouseX, int mouseY) {
        return (mouseX >= x && mouseX <= x + ICON_SIZE && mouseY >= y && mouseY <= y + ICON_SIZE);
    }

    private List<Component> buildTooltip(MobEffect effect, Player player) {
        StatusEffectInstanceEntry entry = effects.get(effect);
            Component description = Component.literal("");
        if (effect instanceof ICustomStatusEffect custom) {
            description = Component.literal(custom.effectDescription(player));
        } else if (effect == MobEffects.FIRE_RESISTANCE.get()) {
            description = Component.literal("Protects from environmental fire damage.");
        } else if (effect == MobEffects.OOZING.get()) {
            description = Component.literal("When killed releases slimes.");
        } else if (effect == MobEffects.CONDUIT_POWER.get()) {
            description = Component.literal("Prevents drowning and enhances underwater sight, movement and block breaking speed.");
        }else if (effect == MobEffects.WIND_CHARGED.get()) {
            description = Component.literal("When killed knocks nearby entities away.");
        } else if (effect == MobEffects.WEAVING.get()) {
            description = Component.literal("When killed releases cobwebs.");
        } else if (effect == MobEffects.UNLUCK.get()) {
            description = Component.literal("Decreases luck.");
        } else if (effect == MobEffects.MOVEMENT_SLOWDOWN.get()) {
            description = Component.literal("Decreases movement speed.");
        } else if (effect == MobEffects.INFESTED.get()) {
            description = Component.literal("When hit there is a chance to spawn silverfish.");
        }else if (effect == MobEffects.HUNGER.get()) {
            description = Component.literal("Increases food depletion's rate.");
        } else if (effect == MobEffects.TRIAL_OMEN.get()) {
            description = Component.literal("More formidable trials await you...");
        } else if (effect == MobEffects.RAID_OMEN.get()) {
            description = Component.literal("More formidable foes lurk behind you...");
        } else if (effect == MobEffects.BAD_OMEN.get()) {
            description = Component.literal("You feel an evil presence watching you...");
        } else if (effect == MobEffects.GLOWING.get()) {
            description = Component.literal("Spotted!");
        } else if (effect == MobEffects.WATER_BREATHING.get()) {
            description = Component.literal("Prevents drowning.");
        } else if (effect == MobEffects.NIGHT_VISION.get()) {
            description = Component.literal("Allows to see in the dark (at least sometimes).");
        }else if (effect == MobEffects.MOVEMENT_SPEED.get()) {
            description = Component.literal("Increases movement speed.");
        }else if (effect == MobEffects.JUMP.get()) {
            description = Component.literal("Increases jump height");
        } else if (effect == MobEffects.INVISIBILITY.get()) {
            description = Component.literal("Prevents being spotted.");
        } else if (effect == MobEffects.HERO_OF_THE_VILLAGE.get()) {
            description = Component.literal("Villagers celebrate your victory!");
        } else if (effect == MobEffects.DOLPHINS_GRACE.get()) {
            description = Component.literal("Dolphins help you swim faster.");
        } else if (effect == MobEffects.DIG_SPEED.get()) {
            description = Component.literal("Increases mining speed.");
        } else {
            description = Component.literal("Amplifier: " + effects.get(effect).amplifier());
        }

        Component tags = Component.literal("Tags: ");

        Optional<ResourceKey<MobEffect>> optional = BuiltInRegistries.MOB_EFFECT.getResourceKey(effect);
        int type = 0;

        if (optional.isPresent()) {
            ResourceKey<MobEffect> key = optional.get();
            Optional<Holder.Reference<MobEffect>> optionalRef = BuiltInRegistries.MOB_EFFECT.getHolder(key);
            if (optionalRef.isPresent()) {
                Holder<MobEffect> holder = optionalRef.get();
                if (holder.is(ModTags.ELEMENTAL_INFUSION_EFFECT)) {
                    tags = Component.literal(tags.getString() + "elemental infusion, ");
                }
                if (holder.is(ModTags.DEBUFFS)) {
                    tags = Component.literal(tags.getString() + "debuff, ");
                    type = -1;
                }
                if (holder.is(ModTags.NEUTRAL_EFFECTS)) {
                    tags = Component.literal(tags.getString() + "neutral effect, ");
                    type = 0;
                }
                if (holder.is(ModTags.BUFFS)) {
                    tags = Component.literal(tags.getString() + "buff, ");
                    type = 1;
                }
                if (holder.is(ModTags.UNDISPELLABLE_EFFECTS)) {
                    tags = Component.literal(tags.getString() + "undispellable, ");
                }
                if (holder.is(ModTags.CC_DEBUFFS)) {
                    tags = Component.literal(tags.getString() + "CC debuff, ");
                }
                if (holder.is(ModTags.SPECIAL_DEBUFFS)) {
                    tags = Component.literal(tags.getString() + "special debuff, ");
                    type = -2;
                }
                if (holder.is(ModTags.SPECIAL_BUFFS)) {
                    tags = Component.literal(tags.getString() + "special buff, ");
                    type = 2;
                }
                if (holder.is(ModTags.SPECIAL_NEUTRAL_EFFECT)) {
                    tags = Component.literal(tags.getString() + "special neutral effect, ");
                    type = 3;
                }
                if (holder.is(ModTags.DOT_EFFECTS)) {
                    tags = Component.literal(tags.getString() + "DoT effect, ");
                }
                if (holder.is(ModTags.STACKABLE_EFFECT)) {
                    tags = Component.literal(tags.getString() + "stackable effect, ");
                }
                if (holder.is(ModTags.ELEMENTAL_EFFECTS)) {
                    tags = Component.literal(tags.getString() + "elemental effect, ");
                }

                int length = tags.getString().length();
                tags = Component.literal(tags.getString(length - 2));
            }
        }

        Component name = Component.translatable(effect.getDescriptionId());
       if (type == 1) {
           name = name.copy().withColor(0x0045b5);
       } else if (type == 2) {
           name = name.copy().withColor(0x00afb5);
       } else if (type == 3) {
           name = name.copy().withColor(0x00b515);
        } else if (type == -1) {
           name = name.copy().withColor(0xb50000);
       } else if (type == -2) {
           name = name.copy().withColor(0xffd500);
       } else {
           name = name.copy().withColor(0xd600cb);
       }

        return List.of(name.copy().append(" (" + ModGeneralUtils.formatDuration(entry.duration()) + ")"), description, tags.copy().withColor(0x5c5c5c));
    }
}
