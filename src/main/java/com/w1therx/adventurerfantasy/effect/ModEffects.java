package com.w1therx.adventurerfantasy.effect;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, AdventurerFantasy.MOD_ID);

    public static final RegistryObject<MobEffect> CONTAMINATED_EFFECT = MOB_EFFECTS.register("contaminated", () -> new ContaminatedEffect(MobEffectCategory.NEUTRAL, 0xa944e));
    public static final RegistryObject<MobEffect> NECROTIC_EFFECT = MOB_EFFECTS.register("necrotic", () -> new NecroticEffect(MobEffectCategory.NEUTRAL, 0x121212));
    public static final RegistryObject<MobEffect> CRYSTALLIZED_EFFECT = MOB_EFFECTS.register("crystallized", () -> new CrystallizedEffect(MobEffectCategory.NEUTRAL, 0x3b2a15));
    public static final RegistryObject<MobEffect> RESONATING_EFFECT = MOB_EFFECTS.register("resonating", () -> new ResonatingEffect(MobEffectCategory.NEUTRAL, 0x231d45));
    public static final RegistryObject<MobEffect> BLAZING_EFFECT = MOB_EFFECTS.register("blazing", () -> new BlazingEffect(MobEffectCategory.NEUTRAL, 0xe36410));
    public static final RegistryObject<MobEffect> FROSTED_EFFECT = MOB_EFFECTS.register("frosted", () -> new FrostedEffect(MobEffectCategory.NEUTRAL, 0x10e3e3));
    public static final RegistryObject<MobEffect> ECSTATIC_EFFECT = MOB_EFFECTS.register("ecstatic", () -> new EcstaticEffect(MobEffectCategory.NEUTRAL, 0xffff00));
    public static final RegistryObject<MobEffect> MOLTEN_EFFECT = MOB_EFFECTS.register("molten", () -> new MoltenEffect(MobEffectCategory.NEUTRAL, 0xffa600));
    public static final RegistryObject<MobEffect> SHOCKED_EFFECT = MOB_EFFECTS.register("shocked", () -> new ShockedEffect(MobEffectCategory.NEUTRAL, 0xde5dd5));
    public static final RegistryObject<MobEffect> SEVERED_EFFECT = MOB_EFFECTS.register("severed", () -> new SeveredEffect(MobEffectCategory.NEUTRAL, 0x595959));
    public static final RegistryObject<MobEffect> FLOURISHING_EFFECT = MOB_EFFECTS.register("flourishing", () -> new FlourishingEffect(MobEffectCategory.NEUTRAL, 0x005c02));
    public static final RegistryObject<MobEffect> EMPTIED_EFFECT = MOB_EFFECTS.register("emptied", () -> new EmptiedEffect(MobEffectCategory.NEUTRAL, 0x2e0f4f));
    public static final RegistryObject<MobEffect> WET_EFFECT = MOB_EFFECTS.register("wet", () -> new WetEffect(MobEffectCategory.NEUTRAL, 0x001075));
    public static final RegistryObject<MobEffect> WINDBLOWN_EFFECT = MOB_EFFECTS.register("windblown", () -> new WindblownEffect(MobEffectCategory.NEUTRAL, 0xcbdec8));
    public static final RegistryObject<MobEffect> VERACITY_EFFECT = MOB_EFFECTS.register("veracity", () -> new VeracityEffect(MobEffectCategory.NEUTRAL, 0xc7a4de));
    public static final RegistryObject<MobEffect> CREATIVE_SHOCK_EFFECT = MOB_EFFECTS.register("creative_shock", () -> new CreativeShockEffect(MobEffectCategory.HARMFUL, 0x301966));
    public static final RegistryObject<MobEffect> CAUTION_EFFECT = MOB_EFFECTS.register("caution", () -> new CautionEffect(MobEffectCategory.BENEFICIAL, 0xc1c2c4));


    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
