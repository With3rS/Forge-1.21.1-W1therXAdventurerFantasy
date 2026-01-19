package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.DmgInstanceType;
import com.w1therx.adventurerfantasy.common.enums.ElementType;
import com.w1therx.adventurerfantasy.common.enums.IndependentStatType;
import com.w1therx.adventurerfantasy.effect.general.ICustomStatusEffect;
import com.w1therx.adventurerfantasy.effect.general.StatusEffectEntry;
import com.w1therx.adventurerfantasy.effect.general.StatusEffectInstanceEntry;
import com.w1therx.adventurerfantasy.util.ModGeneralUtils;
import com.w1therx.adventurerfantasy.util.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


import java.util.*;

public class IndependentStats implements IIndependentStats {

    private final Map<IndependentStatType, Double> IndependentStatMap = new EnumMap<>(IndependentStatType.class);
    private ElementType elementType = ElementType.PHYSICAL;
    private DmgInstanceType dmgInstanceType = DmgInstanceType.DIRECT;
    private ItemStack Weapon = ItemStack.EMPTY;

    public IndependentStats() {
        IndependentStatType[] statList = IndependentStatType.values();

        for (IndependentStatType stat : Arrays.stream(statList).toList()) {
            setIndependentStat(stat, 0);
        }
    }

    @Override
    public Map<IndependentStatType, Double> getIndependentStatMap() {
        return IndependentStatMap;
    }

    @Override
    public void setIndependentStat(IndependentStatType stat, double value) {
        IndependentStatMap.put(stat, value);
    }

    @Override
    public double getIndependentStat(IndependentStatType stat) {
        return IndependentStatMap.getOrDefault(stat, 0D);
    }


    @Override
    public ElementType getElementType() { return elementType; }

    @Override
    public void setElementType(ElementType type) { elementType = type; }

    @Override
    public DmgInstanceType getDmgInstanceType() { return dmgInstanceType; }

    @Override
    public void setDmgInstanceType(DmgInstanceType type) { dmgInstanceType = type; }

    @Override
    public ItemStack getWeapon() { return Weapon; }

    @Override
    public void setWeapon(ItemStack itemStack) { Weapon = itemStack; }

    private final Map<UUID, AggroData> AggroMap = new HashMap<>();

    @Override
    public Map<UUID, AggroData> getAggroMap() {
        return AggroMap;
    }

    @Override
    public void setAggroMap(Map<UUID, AggroData> map) {
        AggroMap.clear();
        AggroMap.putAll(map);
    }

    @Override
    public void setAggro(UUID target, AggroData aggroData) {
        AggroMap.put(target, aggroData);
    }

    @Override
    public AggroData getAggro(UUID target) {
        return AggroMap.getOrDefault(target, new AggroData(0.0D, 0.0D));
    }

    @Override
    public void removeAggro(UUID target) {
        AggroMap.remove(target);
    }

    @Override
    public void clearAggro() {
        AggroMap.clear();
    }

    private final List<StatusEffectEntry> EffectList = new ArrayList<>();

    @Override
    public List<StatusEffectEntry> getEffectList() {
        return EffectList;
    }

    @Override
    public void setEffectList(List<StatusEffectEntry> list) {
        EffectList.clear();
        if (list != null) {
            EffectList.addAll(list);
        }
    }

    @Override
    public void addEffectEntry(StatusEffectEntry entry) {
        EffectList.add(entry);
    }

    @Override
    public void clearEffectList() {
        EffectList.clear();
    }

    private final Map<MobEffect, StatusEffectInstanceEntry> ActiveEffectMap = new HashMap<>();

    @Override
    public Map<MobEffect, StatusEffectInstanceEntry> getActiveEffectList() {
        return ActiveEffectMap;
    }

    @Override
    public void replaceActiveEffectList(Map<MobEffect, StatusEffectInstanceEntry> map, LivingEntity entity) {
        removeAllActiveEffects(entity);
        for (Map.Entry<MobEffect, StatusEffectInstanceEntry> entry : map.entrySet()) {
            addActiveEffectEntry(entry.getKey(), entry.getValue(), entity);
        }
    }

    @Override
    public void addActiveEffectEntry(MobEffect effect, StatusEffectInstanceEntry instanceData, LivingEntity entity) {
        if (ActiveEffectMap.containsKey(effect)) {
            double prevAmp = ActiveEffectMap.get(effect).amplifier();
            int prevStacks = ActiveEffectMap.get(effect).stacks();
            int prevDuration = ActiveEffectMap.get(effect).duration();
            int prevMaxStacks = ActiveEffectMap.get(effect).duration();

            UUID applier = instanceData.applier();
            CompoundTag data = ActiveEffectMap.get(effect).data();

            double newAmp = instanceData.amplifier();
            int newStacks = instanceData.stacks();
            int newDuration = instanceData.duration();
            int newMaxStacks = instanceData.maxStacks();

            removeActiveEffect(effect, entity);

            BuiltInRegistries.MOB_EFFECT.getResourceKey(effect).ifPresent(key -> {
                Holder<MobEffect> holder = key.getOrThrow(entity);
                if (holder.is(ModTags.STACKABLE_EFFECT)) {
                    if (prevAmp <= newAmp) {
                        if (prevMaxStacks <= newMaxStacks) {
                            if (prevStacks < newMaxStacks && newMaxStacks <= prevStacks + newStacks && effect instanceof ICustomStatusEffect) {
                                ((ICustomStatusEffect) effect).onThisEffectStacksReachMax(entity);
                                if (!ActiveEffectMap.isEmpty()) {
                                    for (MobEffect copiedEffect : (new HashMap<>(ActiveEffectMap).keySet())) {
                                        if (ActiveEffectMap.containsKey(copiedEffect)) {
                                            StatusEffectInstanceEntry effectInstance = ActiveEffectMap.get(copiedEffect);
                                            if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                                ((ICustomStatusEffect) copiedEffect).onOtherEffectStacksReachMax(entity, effect);
                                            }
                                        }
                                    }
                                }
                            }
                            ActiveEffectMap.put(effect, new StatusEffectInstanceEntry(Math.max(prevDuration, newDuration), newAmp, Math.min(prevMaxStacks, prevStacks + newStacks), newMaxStacks, applier, data, false));
                        } else {
                            if (prevStacks < prevMaxStacks && prevMaxStacks <= prevStacks + newStacks && effect instanceof ICustomStatusEffect) {
                                ((ICustomStatusEffect) effect).onThisEffectStacksReachMax(entity);
                                if (!ActiveEffectMap.isEmpty()) {
                                    for (MobEffect copiedEffect : (new HashMap<>(ActiveEffectMap).keySet())) {
                                        if (ActiveEffectMap.containsKey(copiedEffect)) {
                                            StatusEffectInstanceEntry effectInstance = ActiveEffectMap.get(copiedEffect);
                                            if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                                ((ICustomStatusEffect) copiedEffect).onOtherEffectStacksReachMax(entity, effect);
                                            }
                                        }
                                    }
                                }
                            }
                            ActiveEffectMap.put(effect, new StatusEffectInstanceEntry(Math.max(prevDuration, newDuration), newAmp, Math.min(prevMaxStacks, prevStacks + newStacks), prevMaxStacks, applier, data, false));
                        }
                    } else {
                        if (prevMaxStacks <= newMaxStacks) {
                            if (prevStacks < newMaxStacks && newMaxStacks <= prevStacks + newStacks && effect instanceof ICustomStatusEffect) {
                                ((ICustomStatusEffect) effect).onThisEffectStacksReachMax(entity);
                                if (!ActiveEffectMap.isEmpty()) {
                                    for (MobEffect copiedEffect : (new HashMap<>(ActiveEffectMap).keySet())) {
                                        if (ActiveEffectMap.containsKey(copiedEffect)) {
                                            StatusEffectInstanceEntry effectInstance = ActiveEffectMap.get(copiedEffect);
                                            if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                                ((ICustomStatusEffect) copiedEffect).onOtherEffectStacksReachMax(entity, effect);
                                            }
                                        }
                                    }
                                }
                            }
                            ActiveEffectMap.put(effect, new StatusEffectInstanceEntry(Math.max(prevDuration, newDuration), prevAmp, Math.min(prevMaxStacks, prevStacks + newStacks), newMaxStacks, applier, data, false));
                        } else {
                            if (prevStacks < prevMaxStacks && prevMaxStacks <= prevStacks + newStacks && effect instanceof ICustomStatusEffect) {
                                ((ICustomStatusEffect) effect).onThisEffectStacksReachMax(entity);
                                if (!ActiveEffectMap.isEmpty()) {
                                    for (MobEffect copiedEffect : (new HashMap<>(ActiveEffectMap).keySet())) {
                                        if (ActiveEffectMap.containsKey(copiedEffect)) {
                                            StatusEffectInstanceEntry effectInstance = ActiveEffectMap.get(copiedEffect);
                                            if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                                ((ICustomStatusEffect) copiedEffect).onOtherEffectStacksReachMax(entity, effect);
                                            }
                                        }
                                    }
                                }
                            }
                            ActiveEffectMap.put(effect, new StatusEffectInstanceEntry(Math.max(prevDuration, newDuration), prevAmp, Math.min(prevMaxStacks, prevStacks + newStacks), prevMaxStacks, applier, data, false));

                        }
                    }
                } else {
                    ActiveEffectMap.put(effect, new StatusEffectInstanceEntry(Math.max(prevDuration, newDuration), Math.max(prevAmp, newAmp), Math.min(Math.max(prevMaxStacks, newMaxStacks), Math.max(prevStacks, newStacks)), Math.max(prevMaxStacks, newMaxStacks), applier, data, false));
                }
            });
        } else {
            ActiveEffectMap.put(effect, instanceData);

        }


        IndependentStatMap.replace(IndependentStatType.STAT_RECALCULATION_TIME, 1D);
    }

    @Override
    public void removeActiveEffect(MobEffect effect, LivingEntity entity) {
        if (ActiveEffectMap.containsKey(effect)) {
            if (effect instanceof ICustomStatusEffect) {
                ((ICustomStatusEffect) effect).onDispel(entity);
            }
            ActiveEffectMap.remove(effect);

            if (!ActiveEffectMap.isEmpty()) {
                for (MobEffect copiedEffect : (new HashMap<>(ActiveEffectMap).keySet())) {
                    if (ActiveEffectMap.containsKey(copiedEffect)) {
                        StatusEffectInstanceEntry effectInstance = ActiveEffectMap.get(copiedEffect);
                        if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                            ((ICustomStatusEffect) copiedEffect).onOtherDispel(entity, effect);
                        }
                    }
                }}
            IndependentStatMap.replace(IndependentStatType.STAT_RECALCULATION_TIME, 1D);
            BuiltInRegistries.MOB_EFFECT.getHolder(BuiltInRegistries.MOB_EFFECT.getId(effect)).ifPresent(entity::removeEffect);
        }
    }

    @Override
    public void removeAllActiveEffects(LivingEntity entity) {
        for (MobEffect effect : (new HashMap<>(ActiveEffectMap)).keySet()) {
            if (ActiveEffectMap.containsKey(effect)) {
                removeActiveEffect(effect, entity);
            }
        }
    }

    @Override
    public StatusEffectInstanceEntry getActiveEffectData(MobEffect effect) {
        return ActiveEffectMap.getOrDefault(effect, new StatusEffectInstanceEntry(0, 0, 0, 0, ModGeneralUtils.EMPTY_UUID, new CompoundTag(), false));
    }

    @Override
    public void setClientActiveEffectMap(Map<MobEffect, StatusEffectInstanceEntry> activeEffects, Player player) {
        if (player.level().isClientSide) {
            Map<MobEffect, StatusEffectInstanceEntry> copiedMap = new HashMap<>(ActiveEffectMap);
            for (Map.Entry<MobEffect, StatusEffectInstanceEntry> entry : copiedMap.entrySet()) {
                ActiveEffectMap.remove(entry.getKey());
            }
            ActiveEffectMap.putAll(activeEffects);
        }
    }

    @Override
    public void removeActiveEffectWithoutConsequences(MobEffect effect, LivingEntity entity) {
        if (ActiveEffectMap.containsKey(effect)) {
            ActiveEffectMap.remove(effect);
            IndependentStatMap.replace(IndependentStatType.STAT_RECALCULATION_TIME, 1D);
            BuiltInRegistries.MOB_EFFECT.getHolder(BuiltInRegistries.MOB_EFFECT.getId(effect)).ifPresent(entity::removeEffect);
        }

    }

    @Override
    public void removeAllActiveEffectsWithoutConsequences(LivingEntity entity) {
        for (MobEffect effect : (new HashMap<>(ActiveEffectMap)).keySet()) {
            if (ActiveEffectMap.containsKey(effect)) {
                removeActiveEffectWithoutConsequences(effect, entity);
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag IndependentStatMapTag = new CompoundTag();
        for (IndependentStatType stat : IndependentStatType.values()) {
            IndependentStatMapTag.putDouble(stat.name(), getIndependentStat(stat));
        }
        tag.put("IndependentStats", IndependentStatMapTag);
        tag.putInt("ElementType", elementType.ordinal());
        tag.putInt("DmgInstanceType", dmgInstanceType.ordinal());

        ListTag AggroList = new ListTag();
        for (Map.Entry<UUID, AggroData> entry : AggroMap.entrySet()) {
            CompoundTag AggroEntry = new CompoundTag();
            AggroEntry.putUUID("Target", entry.getKey());
            AggroEntry.putDouble("AggroValue", entry.getValue().getAggroValue());
            AggroEntry.putDouble("AttackedCount", entry.getValue().getAttackedCount());
            AggroList.add(AggroEntry);
        }
        tag.put("AggroMap", AggroList);

        ListTag EffectList = new ListTag();
        for (StatusEffectEntry entry : getEffectList()) {
            CompoundTag EffectEntry = new CompoundTag();
            EffectEntry.putString("Effect", entry.effect().toString());
            EffectEntry.putInt("Duration", entry.duration());
            EffectEntry.putDouble("Amplifier", entry.amplifier());
            EffectEntry.putInt("maxStacks", entry.maxStacks());
            EffectEntry.putInt("stacks", entry.stacks());

            EffectEntry.putDouble("Chance", entry.baseChance());
            EffectList.add(EffectEntry);
        }
        tag.put("EffectList", EffectList);

        ListTag ActiveEffectList = new ListTag();
        for (Map.Entry<MobEffect, StatusEffectInstanceEntry> entry : ActiveEffectMap.entrySet()) {
            CompoundTag ActiveEffectEntry = new CompoundTag();
            ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(entry.getKey());
            if (effectId != null) {
                ActiveEffectEntry.putString("ActiveEffectNamespace", effectId.getNamespace());
                ActiveEffectEntry.putString("ActiveEffectPath", effectId.getPath());
            }
            ActiveEffectEntry.putDouble("ActiveAmplifier", entry.getValue().amplifier());
            ActiveEffectEntry.putInt("ActiveDuration", entry.getValue().duration());
            ActiveEffectEntry.putInt("ActiveStacks", entry.getValue().stacks());
            ActiveEffectEntry.putInt("ActiveMaxStacks", entry.getValue().maxStacks());
            if (entry.getValue().applier() != null) {
                ActiveEffectEntry.putUUID("ActiveApplier", entry.getValue().applier());
            } else {
                ActiveEffectEntry.putUUID("ActiveApplier", ModGeneralUtils.EMPTY_UUID);
            }
            ActiveEffectEntry.put("Data", entry.getValue().data());
            ActiveEffectEntry.putBoolean("ActiveIsInitialised", entry.getValue().isInitialised());
            ActiveEffectList.add(ActiveEffectEntry);
        }
        tag.put("ActiveEffectMap", ActiveEffectList);

        ResourceLocation weaponResLoc = BuiltInRegistries.ITEM.getKey(getWeapon().getItem());

        tag.putString("WeaponNamespace", weaponResLoc.getNamespace());
        tag.putString("WeaponPath", weaponResLoc.getPath());

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt, LivingEntity entity) {
        if (nbt == null) return;
        if (nbt instanceof CompoundTag tag) {
            CompoundTag IndependentStatMap = tag.getCompound("IndependentStats");
            for (IndependentStatType stat : IndependentStatType.values()) {
                setIndependentStat(stat, IndependentStatMap.getDouble(stat.name()));
            }
            setElementType(ElementType.values() [tag.getInt("ElementType")]);
            setDmgInstanceType(DmgInstanceType.values() [tag.getInt("DmgInstanceType")]);
            setIndependentStat(IndependentStatType.STAT_RECALCULATION_TIME, 1);

            setWeapon(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(tag.getString("WeaponNamespace"), tag.getString("WeaponPath")))));

            if (nbt.contains("AggroMap", Tag.TAG_LIST)) {
                ListTag AggroList = (nbt).getList("AggroMap", Tag.TAG_COMPOUND);
                Map<UUID, AggroData> map = new HashMap<>();
                for (Tag element : AggroList) {
                    CompoundTag entry = (CompoundTag) element;
                    UUID target = entry.getUUID("Target");
                    double aggroValue = entry.getDouble("AggroValue");
                    double attackedCount = entry.getDouble("AttackedCount");
                    map.put(target, new AggroData(aggroValue, attackedCount));
                }
                setAggroMap(map);
            }

            if (nbt.contains("EffectList", Tag.TAG_LIST)) {
                ListTag listTag = nbt.getList("EffectList", Tag.TAG_COMPOUND);
                List<StatusEffectEntry> effects = new ArrayList<>();

                for (Tag EffectEntry : listTag) {
                    if (EffectEntry instanceof CompoundTag compound) {
                        ResourceLocation id = ResourceLocation.tryParse(compound.getString("Effect"));
                        RegistryObject<MobEffect> effect = null;
                        if (id != null) {
                           effect = RegistryObject.create(id, ForgeRegistries.MOB_EFFECTS);
                        }
                        int duration = compound.getInt("Duration");
                        int stacks = compound.getInt("stacks");
                        int maxStacks = compound.getInt("maxStacks");
                        double amplifier = compound.getDouble("Amplifier");
                        double chance = compound.getDouble("Chance");
                        if (effect != null) {
                            effects.add(new StatusEffectEntry(effect.get(), duration, amplifier, stacks, maxStacks, chance));
                        }
                    }
                }

                setEffectList(effects);

                if (nbt.contains("ActiveEffectMap")) {
                    ListTag activeEffectList = nbt.getList("ActiveEffectMap", Tag.TAG_COMPOUND);

                    for (int i = 0; i < activeEffectList.size(); i++) {
                        CompoundTag effectTag = activeEffectList.getCompound(i);
                        String effectNamespace = effectTag.getString("ActiveEffectNamespace");
                        String effectPath = effectTag.getString("ActiveEffectPath");
                        ResourceLocation effectId = ResourceLocation.fromNamespaceAndPath(effectNamespace, effectPath);
                        MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(effectId);
                        if (effect != null) {
                            MobEffect registryEffect = RegistryObject.create(effectId, ForgeRegistries.MOB_EFFECTS).get();
                            double amp = effectTag.getDouble("ActiveAmplifier");
                            int duration = effectTag.getInt("ActiveDuration");
                            int stacks = effectTag.getInt("ActiveStacks");
                            int maxStacks = effectTag.getInt("ActiveMaxStacks");
                            UUID applier = effectTag.getUUID("ActiveApplier");
                            CompoundTag data = effectTag.getCompound("Data");
                            boolean isInitialised = effectTag.getBoolean("ActiveIsInitialised");

                            addActiveEffectEntry(registryEffect, new StatusEffectInstanceEntry(duration, amp, stacks, maxStacks, applier, data, isInitialised), entity);
                        }
                    }
                }
            }
        }
    }
}
