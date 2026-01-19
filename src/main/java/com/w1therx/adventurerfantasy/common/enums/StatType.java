package com.w1therx.adventurerfantasy.common.enums;


public enum StatType {
    ADDITIONAL_JUMP(true, 0, 0, Double.MAX_VALUE, false),
    AGGRO_MULT(false, 0, 0, Double.MAX_VALUE, true),
    ALL_DMG_AMP(false, 1, 0, Double.MAX_VALUE, true),
    ALL_DMG_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    ALL_TYPE_RES_IGNORE(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    ARMOR(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, false),
    ARMOR_PEN(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    ARROW_VELOCITY(false, 0, 0, Double.MAX_VALUE, true),
    ATK(false, 0, 0, Double.MAX_VALUE, false),
    ATK_COOLDOWN(false, 0, 0, Double.MAX_VALUE, false),
    ATK_SIZE_AMP(false, 1, 0, Double.MAX_VALUE, true),
    ATK_SPEED(false, 1, 0, Double.MAX_VALUE, true),
    BLIGHT_DMG_AMP(false, 1, 0, Double.MAX_VALUE, true),
    BLIGHT_DMG_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    CC_DEBUFF_EFFICIENCY(false, 1, 0, Double.MAX_VALUE, true),
    CC_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    CHARGE_MULT(false, 0, 0, Double.MAX_VALUE, true),
    CRIT_DMG(false, 1.5, 1, Double.MAX_VALUE, true),
    CRIT_RATE(false, 0, 0, 1, true),
    DASH_COOLDOWN(true, 150, 0, Double.MAX_VALUE, false),
    DASH_COUNT(true, 1, 0, Double.MAX_VALUE, false),
    DASH_LENGTH(false, 4, 0, Double.MAX_VALUE,false),
    DECAY_DMG_AMP(false, 1, 0,Double.MAX_VALUE, true),
    DECAY_DMG_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    DELAYED_DMG_AMP(false, 1, 0, Double.MAX_VALUE,true),
    DELAYED_DMG_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    DIRECT_DMG_AMP(false, 1, 0, Double.MAX_VALUE, true),
    DIRECT_DMG_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    DODGE_CHANCE(false, 0, 0, 1, true),
    DOT_AMP(false, 1, 0, Double.MAX_VALUE, true),
    DOT_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    DROWNING_DMG_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    EARTH_DMG_AMP(false, 1, 0, Double.MAX_VALUE, true),
    EARTH_DMG_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    ECHO_DMG_AMP(false, 1, 0, Double.MAX_VALUE, true),
    ECHO_DMG_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    EFFECT_DURATION(false, 1, 0, Double.MAX_VALUE, true),
    EFFECT_EFFICIENCY(false, 1, 0, Double.MAX_VALUE, true),
    EFFECT_HIT_RATE(false, 0, 0, Double.MAX_VALUE, true),
    EFFECT_RES(false, 0, 0, Double.MAX_VALUE, true),
    ELEMENTAL_AFFINITY(false, 0, 0, 2, true),
    ELEMENTAL_MASTERY(false, 0, 0, Double.MAX_VALUE,  false),
    ENERGY_REGENERATION_RATE(false, 0, 0, Double.MAX_VALUE, true),
    FALL_DMG_RES(false,0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    FIRE_DMG_AMP(false, 1, 0, Double.MAX_VALUE, true),
    FIRE_DMG_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    FOLLOW_UP_DMG_AMP(false, 1, 0, Double.MAX_VALUE, true),
    FOLLOW_UP_DMG_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    HEALTH_REGENERATION_AMP(false, 0, 0, Double.MAX_VALUE, true),
    HEALTH_RESTORATION_ON_DEATH_DEFIANCE(false, 0, 0, 1, true),
    ICE_DMG_AMP(false, 1, 0, Double.MAX_VALUE, true),
    ICE_DMG_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    IMAGINARY_DMG_AMP(false, 1, 0, Double.MAX_VALUE, true),
    IMAGINARY_DMG_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    INACCURACY(false, 0, 0, 180, false),
    INCOMING_HEALING_AMP(false, 1, 0, Double.MAX_VALUE, true),
    INCOMING_SHIELD_STRENGTH_AMP(false, 1, 0, Double.MAX_VALUE, true),
    INVULNERABLE_DURATION(true, 10, 1, Double.MAX_VALUE, true),
    KNOCKBACK_STRENGTH_AMP(false, 1, 0, Double.MAX_VALUE, true),
    KNOCKBACK_RES (false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    LAVA_DMG_AMP(false,1, 0, Double.MAX_VALUE, true),
    LAVA_DMG_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    LIFESTEAL(false, 0, 0, Double.MAX_VALUE, true),
    LIGHTNING_DMG_AMP(false,1, 0, Double.MAX_VALUE, true),
    LIGHTNING_DMG_RES(false,0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    MACE_BLAST_RADIUS(false,1, 0, Double.MAX_VALUE, false),
    MAGAZINE_CAPACITY(true,0, 0, 6, false),
    MANA_REGENERATION(false,0, 0, Double.MAX_VALUE, false),
    MAX_BOND_OF_LIFE(false,2, 0, Double.MAX_VALUE, true),
    MAX_COMBO(true,0, 0, Double.MAX_VALUE, false),
    MAX_DRAW_DURATION(true,0, 0, Double.MAX_VALUE, false),
    MAX_ENERGY(false,0, 0, Double.MAX_VALUE, false),
    MAX_HEALTH(false,0.1, 0.1, Double.MAX_VALUE, false),
    MAX_MANA(false, 0, 0, Double.MAX_VALUE, false),
    MAX_ZOOM(false,1, 1, Double.MAX_VALUE, true),
    MINING_SPEED(false, 1, 0, Double.MAX_VALUE, true),
    MOVEMENT_SPEED(false, 2, 0, Double.MAX_VALUE, false),
    NATURE_DMG_AMP(false,1, 0, Double.MAX_VALUE, true),
    NATURE_DMG_RES(false,0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    NON_DAMAGING_DEBUFF_EFFICIENCY(false,1, 0, Double.MAX_VALUE, true),
    OUTGOING_HEALING_AMP(false,1, 0, Double.MAX_VALUE, true),
    OUTGOING_SHIELD_STRENGTH_AMP(false, 1, 0, Double.MAX_VALUE, true),
    PHYSICAL_DMG_AMP(false,1, 0, Double.MAX_VALUE, true),
    PHYSICAL_DMG_RES(false,0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    PRESERVE_AMMO_RATE(false,0, 0, 1, true),
    PROJECTILE_GRAVITY(false,0, 0, Double.MAX_VALUE, true),
    REACH(false,0, 0, Double.MAX_VALUE, false),
    REACTION_DMG_AMP(false, 1, 0, Double.MAX_VALUE, true),
    REACTION_DMG_RES(false,0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    RETALIATION_DMG_AMP(false,1, 0, Double.MAX_VALUE, true),
    RETALIATION_DMG_RES(false,0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    SELF_DMG_AMP(false,1, 0, Double.MAX_VALUE, true),
    SUMMON_DMG_AMP(false,1, 0, Double.MAX_VALUE, true),
    SUMMON_DMG_RES(false,0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    SWEEP_ANGLE(false,0, 0, 360, false),
    ULTIMATE_DMG_AMP (false, 1, 0, Double.MAX_VALUE, true),
    ULTIMATE_DMG_RES(false, 0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    VOID_DMG_AMP(false,1, 0, Double.MAX_VALUE, true),
    VOID_DMG_RES(false,0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    WATER_DMG_AMP(false,1, 0, Double.MAX_VALUE, true),
    WATER_DMG_RES(false,0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    WIND_DMG_AMP(false,1, 0, Double.MAX_VALUE, true),
    WIND_DMG_RES(false,0, -Double.MAX_VALUE, Double.MAX_VALUE, true),
    XP_GAIN(false,1, 0, Double.MAX_VALUE, true);


    private final Boolean shouldBeTruncated;
    private final double baseValue;
    private final double minValue;
    private final double maxValue;
    private final boolean isPercentageInDecimals;

    StatType(Boolean shouldBeTruncated, double baseValue, double minValue, double maxValue, boolean isPercentageInDecimals) {
        this.shouldBeTruncated = shouldBeTruncated;
        this.baseValue = baseValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.isPercentageInDecimals = isPercentageInDecimals;
    }

    public Boolean getShouldBeTruncated() {
        return shouldBeTruncated;
    }
    public double getBaseValue() {
        return baseValue;
    }
    public double getMinValue() { return minValue; }
    public double getMaxValue() { return maxValue; }
    public boolean getIsPercentageInDecimals() { return isPercentageInDecimals; }
}
