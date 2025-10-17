package com.w1therx.adventurerfantasy.capability;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IStatModifierProvider {
    List<StatModifier> getModifiers(ItemStack stack);
}