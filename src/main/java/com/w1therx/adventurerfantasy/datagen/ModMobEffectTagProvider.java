package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.AdventurerFantasy;

import com.w1therx.adventurerfantasy.effect.general.ModEffects;
import com.w1therx.adventurerfantasy.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModMobEffectTagProvider extends TagsProvider<MobEffect> {
    public ModMobEffectTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.MOB_EFFECT, lookupProvider, AdventurerFantasy.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        MobEffects.CONDUIT_POWER.unwrapKey().ifPresent(key-> {tag(ModTags.BUFFS).add(key); tag(ModTags.SPECIAL_BUFFS).add(key); tag(ModTags.UNDISPELLABLE_EFFECTS).add(key);});
        MobEffects.DIG_SPEED.unwrapKey().ifPresent(key-> tag(ModTags.BUFFS).add(key));
        MobEffects.DOLPHINS_GRACE.unwrapKey().ifPresent(key-> {tag(ModTags.BUFFS).add(key); tag(ModTags.UNDISPELLABLE_EFFECTS).add(key); tag(ModTags.SPECIAL_BUFFS).add(key);});
        MobEffects.FIRE_RESISTANCE.unwrapKey().ifPresent(key-> tag(ModTags.BUFFS).add(key));
        MobEffects.HERO_OF_THE_VILLAGE.unwrapKey().ifPresent(key-> {tag(ModTags.BUFFS).add(key); tag(ModTags.UNDISPELLABLE_EFFECTS).add(key); tag(ModTags.SPECIAL_BUFFS).add(key);});
        MobEffects.INVISIBILITY.unwrapKey().ifPresent(key-> tag(ModTags.BUFFS).add(key));
        MobEffects.JUMP.unwrapKey().ifPresent(key-> tag(ModTags.BUFFS).add(key));
        MobEffects.LUCK.unwrapKey().ifPresent(key-> tag(ModTags.BUFFS).add(key));
        MobEffects.MOVEMENT_SPEED.unwrapKey().ifPresent(key-> tag(ModTags.BUFFS).add(key));
        MobEffects.NIGHT_VISION.unwrapKey().ifPresent(key-> tag(ModTags.BUFFS).add(key));
        MobEffects.WATER_BREATHING.unwrapKey().ifPresent(key-> tag(ModTags.BUFFS).add(key));
        MobEffects.BAD_OMEN.unwrapKey().ifPresent(key-> tag(ModTags.NEUTRAL_EFFECTS).add(key));
        MobEffects.GLOWING.unwrapKey().ifPresent(key-> tag(ModTags.NEUTRAL_EFFECTS).add(key));
        MobEffects.RAID_OMEN.unwrapKey().ifPresent(key->  {tag(ModTags.NEUTRAL_EFFECTS).add(key); tag(ModTags.SPECIAL_NEUTRAL_EFFECT).add(key);});
        MobEffects.TRIAL_OMEN.unwrapKey().ifPresent(key-> {tag(ModTags.NEUTRAL_EFFECTS).add(key); tag(ModTags.SPECIAL_NEUTRAL_EFFECT).add(key);});
        MobEffects.HUNGER.unwrapKey().ifPresent(key-> tag(ModTags.DEBUFFS).add(key));
        MobEffects.INFESTED.unwrapKey().ifPresent(key-> tag(ModTags.DEBUFFS).add(key));
        MobEffects.MOVEMENT_SLOWDOWN.unwrapKey().ifPresent(key-> tag(ModTags.DEBUFFS).add(key));
        MobEffects.OOZING.unwrapKey().ifPresent(key-> tag(ModTags.DEBUFFS).add(key));
        MobEffects.UNLUCK.unwrapKey().ifPresent(key-> tag(ModTags.DEBUFFS).add(key));
        MobEffects.WEAVING.unwrapKey().ifPresent(key-> tag(ModTags.DEBUFFS).add(key));
        MobEffects.WIND_CHARGED.unwrapKey().ifPresent(key-> tag(ModTags.DEBUFFS).add(key));


        tag(ModTags.BUFFS)
                .add(ModEffects.TOTEM_WARD_EFFECT.getKey())
                .add(ModEffects.CAUTION_EFFECT.getKey())
                .add(ModEffects.BLESSING_OF_UNDYING_EFFECT.getKey());
        tag(ModTags.SPECIAL_BUFFS)
                .add(ModEffects.TOTEM_WARD_EFFECT.getKey())
                .add(ModEffects.BLESSING_OF_UNDYING_EFFECT.getKey());
        tag(ModTags.DEBUFFS)
                .add(ModEffects.CREATIVE_SHOCK_EFFECT.getKey());
        tag(ModTags.SPECIAL_DEBUFFS)
                .add(ModEffects.CREATIVE_SHOCK_EFFECT.getKey());
        tag(ModTags.NEUTRAL_EFFECTS)
                .add(ModEffects.CONTAMINATED_EFFECT.getKey())
                .add(ModEffects.NECROTIC_EFFECT.getKey())
                .add(ModEffects.CRYSTALLIZED_EFFECT.getKey())
                .add(ModEffects.RESONANT_EFFECT.getKey())
                .add(ModEffects.BLAZING_EFFECT.getKey())
                .add(ModEffects.FROSTED_EFFECT.getKey())
                .add(ModEffects.ECSTATIC_EFFECT.getKey())
                .add(ModEffects.MOLTEN_EFFECT.getKey())
                .add(ModEffects.ELECTRIFIED_EFFECT.getKey())
                .add(ModEffects.SEVERED_EFFECT.getKey())
                .add(ModEffects.FLOURISHING_EFFECT.getKey())
                .add(ModEffects.EMPTIED_EFFECT.getKey())
                .add(ModEffects.WET_EFFECT.getKey())
                .add(ModEffects.WHIRLING_EFFECT.getKey())
                .add(ModEffects.BLIGHT_INFUSION_EFFECT.getKey())
                .add(ModEffects.DECAY_INFUSION_EFFECT.getKey())
                .add(ModEffects.EARTH_INFUSION_EFFECT.getKey())
                .add(ModEffects.ECHO_INFUSION_EFFECT.getKey())
                .add(ModEffects.FIRE_INFUSION_EFFECT.getKey())
                .add(ModEffects.ICE_INFUSION_EFFECT.getKey())
                .add(ModEffects.IMAGINARY_INFUSION_EFFECT.getKey())
                .add(ModEffects.LAVA_INFUSION_EFFECT.getKey())
                .add(ModEffects.LIGHTNING_INFUSION_EFFECT.getKey())
                .add(ModEffects.PHYSICAL_INFUSION_EFFECT.getKey())
                .add(ModEffects.NATURE_INFUSION_EFFECT.getKey())
                .add(ModEffects.VOID_INFUSION_EFFECT.getKey())
                .add(ModEffects.WATER_INFUSION_EFFECT.getKey())
                .add(ModEffects.WIND_INFUSION_EFFECT.getKey())
                .add(ModEffects.TRUE_INFUSION_EFFECT.getKey());
        tag(ModTags.UNDISPELLABLE_EFFECTS)
                .add(ModEffects.CREATIVE_SHOCK_EFFECT.getKey())
                .add(ModEffects.BLESSING_OF_UNDYING_EFFECT.getKey())
                .add(ModEffects.TOTEM_WARD_EFFECT.getKey());
        tag(ModTags.ELEMENTAL_INFUSION_EFFECT)
                .add(ModEffects.BLIGHT_INFUSION_EFFECT.getKey())
                .add(ModEffects.DECAY_INFUSION_EFFECT.getKey())
                .add(ModEffects.EARTH_INFUSION_EFFECT.getKey())
                .add(ModEffects.ECHO_INFUSION_EFFECT.getKey())
                .add(ModEffects.FIRE_INFUSION_EFFECT.getKey())
                .add(ModEffects.ICE_INFUSION_EFFECT.getKey())
                .add(ModEffects.IMAGINARY_INFUSION_EFFECT.getKey())
                .add(ModEffects.LAVA_INFUSION_EFFECT.getKey())
                .add(ModEffects.LIGHTNING_INFUSION_EFFECT.getKey())
                .add(ModEffects.PHYSICAL_INFUSION_EFFECT.getKey())
                .add(ModEffects.NATURE_INFUSION_EFFECT.getKey())
                .add(ModEffects.VOID_INFUSION_EFFECT.getKey())
                .add(ModEffects.WATER_INFUSION_EFFECT.getKey())
                .add(ModEffects.WIND_INFUSION_EFFECT.getKey())
                .add(ModEffects.TRUE_INFUSION_EFFECT.getKey());

    }
}
