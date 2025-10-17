package com.w1therx.adventurerfantasy.common.enums;


public enum IndependentStatType {
    ADDITIONAL_JUMP_AVAILABLE(true, 0),
    BOND_OF_LIFE(false, 0),
    DASH_AVAILABLE(true, 0),
    DASH_TIME(true, 0),
    ENERGY(false, 0),
    HEALTH(false, 1),
    INVULNERABLE_TIME(true, 0),
    MANA(false, 0),
    REACTION_COOLDOWN(true, 40),
    REACTION_TIME(true, 0),
    REGENERATION_CALCULATION(false, 0),
    SHIELD(false,0),
    SHIELD_TIME(true, 0),
    STAT_RECALCULATION_TIME(true, 0),
    ZOOM(false,1);


    private final Boolean shouldBeTruncated;
    private final double baseValue;

    IndependentStatType(Boolean shouldBeTruncated, double baseValue) {
        this.shouldBeTruncated = shouldBeTruncated;
        this.baseValue = baseValue;
    }

    public Boolean getShouldBeTruncated() {
        return shouldBeTruncated;
    }
    public double getBaseValue() {
        return baseValue;
    }
}
