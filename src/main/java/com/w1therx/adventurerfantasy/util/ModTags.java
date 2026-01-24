package com.w1therx.adventurerfantasy.util;

import com.w1therx.adventurerfantasy.AdventurerFantasy;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Items {
        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(AdventurerFantasy.MOD_ID, name));
        }
    }

    public static class Blocks {
        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(AdventurerFantasy.MOD_ID, name));
        }
    }

    public static class Entities {
        private static TagKey<EntityType<?>> createTag(String name) {
            return EntityTypeTags.create(ResourceLocation.fromNamespaceAndPath(AdventurerFantasy.MOD_ID, name));
        }
    }

    public static class MobEffects {
        private static TagKey<MobEffect> createTag(String name) {
            return TagKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(AdventurerFantasy.MOD_ID, name));
        }
    }

    public static final TagKey<EntityType<?>> BOSS_ENTITY = Entities.createTag("boss_entity");

    public static final TagKey<Block> DIVINITIES = Blocks.createTag("divinities");

    public static final TagKey<Block> PRINCIPLES = Blocks.createTag("principles");

    public static final TagKey<Item> ESSENCES = Items.createTag("essences");

    public static final TagKey<Item> LOGOI = Items.createTag("logoi");

    public static final TagKey<MobEffect> BUFFS = MobEffects.createTag("buffs");

    public static final TagKey<MobEffect> DEBUFFS = MobEffects.createTag("debuffs");

    public static final TagKey<MobEffect> NEUTRAL_EFFECTS = MobEffects.createTag("neutral_effects");

    public static final TagKey<MobEffect> SPECIAL_BUFFS = MobEffects.createTag("special_buffs");

    public static final TagKey<MobEffect> SPECIAL_DEBUFFS = MobEffects.createTag("special_debuffs");

    public static final TagKey<MobEffect> SPECIAL_NEUTRAL_EFFECT = MobEffects.createTag("special_neutral_effect");

    public static final TagKey<MobEffect> UNDISPELLABLE_EFFECTS = MobEffects.createTag("undispellable_effects");

    public static final TagKey<MobEffect> EFFECTS_NOT_SHOWN_IN_GUI = MobEffects.createTag("effects_not_shown_in_gui");

    public static final TagKey<MobEffect> DOT_EFFECTS = MobEffects.createTag("dot_effects");

    public static final TagKey<MobEffect> CC_DEBUFFS = MobEffects.createTag("cc_debuffs");

    public static final TagKey<MobEffect> STACKABLE_EFFECT = MobEffects.createTag("stackable_effect");

    public static final TagKey<MobEffect> ELEMENTAL_INFUSION_EFFECT  = MobEffects.createTag("elemental_infusion_effect");

    public static final TagKey<MobEffect> GENERAL_BUFF = MobEffects.createTag("general_buff");

    public static final TagKey<MobEffect> GENERAL_DEBUFF = MobEffects.createTag("general_debuff");

    public static final TagKey<MobEffect> GENERAL_NEUTRAL_EFFECT = MobEffects.createTag("general_neutral_effect");

    public static final TagKey<MobEffect> ELEMENTAL_EFFECTS = MobEffects.createTag("elemental_effects");



}
