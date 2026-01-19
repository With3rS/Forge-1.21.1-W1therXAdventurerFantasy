package com.w1therx.adventurerfantasy.effect.general;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
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
    public static final RegistryObject<MobEffect> RESONANT_EFFECT = MOB_EFFECTS.register("resonant", () -> new ResonantEffect(MobEffectCategory.NEUTRAL, 0x231d45));
    public static final RegistryObject<MobEffect> BLAZING_EFFECT = MOB_EFFECTS.register("blazing", () -> new BlazingEffect(MobEffectCategory.NEUTRAL, 0xe36410));
    public static final RegistryObject<MobEffect> FROSTED_EFFECT = MOB_EFFECTS.register("frosted", () -> new FrostedEffect(MobEffectCategory.NEUTRAL, 0x10e3e3));
    public static final RegistryObject<MobEffect> ECSTATIC_EFFECT = MOB_EFFECTS.register("ecstatic", () -> new EcstaticEffect(MobEffectCategory.NEUTRAL, 0xffff00));
    public static final RegistryObject<MobEffect> MOLTEN_EFFECT = MOB_EFFECTS.register("molten", () -> new MoltenEffect(MobEffectCategory.NEUTRAL, 0xffa600));
    public static final RegistryObject<MobEffect> ELECTRIFIED_EFFECT = MOB_EFFECTS.register("electrified", () -> new ElectrifiedEffect(MobEffectCategory.NEUTRAL, 0xde5dd5));
    public static final RegistryObject<MobEffect> SEVERED_EFFECT = MOB_EFFECTS.register("severed", () -> new SeveredEffect(MobEffectCategory.NEUTRAL, 0x595959));
    public static final RegistryObject<MobEffect> FLOURISHING_EFFECT = MOB_EFFECTS.register("flourishing", () -> new FlourishingEffect(MobEffectCategory.NEUTRAL, 0x005c02));
    public static final RegistryObject<MobEffect> EMPTIED_EFFECT = MOB_EFFECTS.register("emptied", () -> new EmptiedEffect(MobEffectCategory.NEUTRAL, 0x2e0f4f));
    public static final RegistryObject<MobEffect> WET_EFFECT = MOB_EFFECTS.register("wet", () -> new WetEffect(MobEffectCategory.NEUTRAL, 0x001075));
    public static final RegistryObject<MobEffect> WHIRLING_EFFECT = MOB_EFFECTS.register("whirling", () -> new WhirlingEffect(MobEffectCategory.NEUTRAL, 0xcbdec8));
    public static final RegistryObject<MobEffect> CREATIVE_SHOCK_EFFECT = MOB_EFFECTS.register("creative_shock", () -> new CreativeShockEffect(MobEffectCategory.HARMFUL, 0x301966));
    public static final RegistryObject<MobEffect> CAUTION_EFFECT = MOB_EFFECTS.register("caution", () -> new CautionEffect(MobEffectCategory.BENEFICIAL, 0xc1c2c4));
    public static final RegistryObject<MobEffect> BLESSING_OF_UNDYING_EFFECT = MOB_EFFECTS.register("blessing_of_undying", () -> new BlessingOfUndyingEffect(MobEffectCategory.BENEFICIAL, 0xc1c2c4));
    public static final RegistryObject<MobEffect> TOTEM_WARD_EFFECT = MOB_EFFECTS.register("totem_ward", () -> new TotemWardEffect(MobEffectCategory.BENEFICIAL, 0xc1c2c4));
    public static final RegistryObject<MobEffect> BLIGHT_INFUSION_EFFECT = MOB_EFFECTS.register("blight_infusion", () -> new BlightInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> DECAY_INFUSION_EFFECT = MOB_EFFECTS.register("decay_infusion", () -> new DecayInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> EARTH_INFUSION_EFFECT = MOB_EFFECTS.register("earth_infusion", () -> new EarthInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> ECHO_INFUSION_EFFECT = MOB_EFFECTS.register("echo_infusion", () -> new EchoInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> FIRE_INFUSION_EFFECT = MOB_EFFECTS.register("fire_infusion", () -> new FireInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> ICE_INFUSION_EFFECT = MOB_EFFECTS.register("ice_infusion", () -> new IceInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> IMAGINARY_INFUSION_EFFECT = MOB_EFFECTS.register("imaginary_infusion", () -> new ImaginaryInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> LAVA_INFUSION_EFFECT = MOB_EFFECTS.register("lava_infusion", () -> new LavaInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> LIGHTNING_INFUSION_EFFECT = MOB_EFFECTS.register("lightning_infusion", () -> new LightningInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> PHYSICAL_INFUSION_EFFECT = MOB_EFFECTS.register("physical_infusion", () -> new PhysicalInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> NATURE_INFUSION_EFFECT = MOB_EFFECTS.register("nature_infusion", () -> new NatureInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> VOID_INFUSION_EFFECT = MOB_EFFECTS.register("void_infusion", () -> new VoidInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> WATER_INFUSION_EFFECT = MOB_EFFECTS.register("water_infusion", () -> new WaterInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> WIND_INFUSION_EFFECT = MOB_EFFECTS.register("wind_infusion", () -> new WindInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> TRUE_INFUSION_EFFECT = MOB_EFFECTS.register("true_infusion", () -> new TrueInfusionEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF));


    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
