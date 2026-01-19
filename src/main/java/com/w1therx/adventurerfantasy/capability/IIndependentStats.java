package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.DmgInstanceType;
import com.w1therx.adventurerfantasy.common.enums.ElementType;
import com.w1therx.adventurerfantasy.common.enums.IndependentStatType;
import com.w1therx.adventurerfantasy.effect.general.StatusEffectEntry;
import com.w1therx.adventurerfantasy.effect.general.StatusEffectInstanceEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IIndependentStats {

    Map<IndependentStatType, Double> getIndependentStatMap();

    void setIndependentStat (IndependentStatType stat, double value);

    double getIndependentStat(IndependentStatType stat);

    ElementType getElementType();
    void setElementType(ElementType elementType);

    DmgInstanceType getDmgInstanceType();
    void setDmgInstanceType(DmgInstanceType dmgInstanceType);

    ItemStack getWeapon();
    void setWeapon(ItemStack itemStack);

    Map<UUID, AggroData> getAggroMap();
    void setAggroMap(Map<UUID, AggroData> map);
    void setAggro(UUID target, AggroData aggroData);
    AggroData getAggro(UUID target);
    void removeAggro(UUID target);
    void clearAggro();


    List<StatusEffectEntry> getEffectList();
    void setEffectList(List<StatusEffectEntry> list);
    void addEffectEntry(StatusEffectEntry entry);
    void clearEffectList();

    Map<MobEffect, StatusEffectInstanceEntry> getActiveEffectList();
    void replaceActiveEffectList(Map<MobEffect, StatusEffectInstanceEntry> map, LivingEntity entity);
    void addActiveEffectEntry(MobEffect effect, StatusEffectInstanceEntry entry, LivingEntity entity);
    void removeActiveEffect(MobEffect effect, LivingEntity entity);
    StatusEffectInstanceEntry getActiveEffectData(MobEffect effect);
    void removeAllActiveEffects(LivingEntity entity);
    void setClientActiveEffectMap(Map<MobEffect, StatusEffectInstanceEntry> activeEffects, Player player);
    void removeActiveEffectWithoutConsequences(MobEffect effect,LivingEntity entity);
    void removeAllActiveEffectsWithoutConsequences(LivingEntity entity);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt, LivingEntity entity);
}
