package com.w1therx.adventurerfantasy.common.enums;

import com.w1therx.adventurerfantasy.effect.general.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.RegistryObject;

public enum ElementType {
    BLIGHT(ModEffects.CONTAMINATED_EFFECT, 0xa944e),
    DECAY(ModEffects.NECROTIC_EFFECT, 0x121212),
    EARTH(ModEffects.CRYSTALLIZED_EFFECT, 0x3b2a15),
    ECHO(ModEffects.RESONANT_EFFECT, 0x231d45),
    FIRE(ModEffects.BLAZING_EFFECT, 0xEF1C00),
    ICE(ModEffects.FROSTED_EFFECT, 0x10e3e3),
    IMAGINARY(ModEffects.ECSTATIC_EFFECT, 0xffff00),
    LAVA(ModEffects.MOLTEN_EFFECT, 0xffa600),
    LIGHTNING(ModEffects.ELECTRIFIED_EFFECT, 0xde5dd5),
    PHYSICAL(ModEffects.SEVERED_EFFECT, 0x595959),
    NATURE(ModEffects.FLOURISHING_EFFECT, 0x005c02),
    VOID(ModEffects.EMPTIED_EFFECT, 0x2e0f4f),
    WATER(ModEffects.WET_EFFECT, 0x001075),
    WIND(ModEffects.WHIRLING_EFFECT, 0xD4FFDC),
    TRUE(null, 0xc7a4de);

    private final RegistryObject<MobEffect> effect;
    private final int color;

    ElementType(RegistryObject<MobEffect> effect, int color) {
        this.effect = effect;
        this.color = color;
    }

    public RegistryObject<MobEffect> getEffect() {
        return effect;
    }

    public int getColor() {
        return color;
    }
}
