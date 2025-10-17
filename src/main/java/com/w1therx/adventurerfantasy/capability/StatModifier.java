package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.StatType;

public record StatModifier (StatType stat, double amount, boolean isMultiplicative) {
}
