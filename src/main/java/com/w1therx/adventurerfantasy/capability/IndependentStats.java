package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.DmgInstanceType;
import com.w1therx.adventurerfantasy.common.enums.ElementType;
import com.w1therx.adventurerfantasy.common.enums.IndependentStatType;
import com.w1therx.adventurerfantasy.effect.combat.StatusEffectEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
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
            EffectEntry.putInt("Amplifier", entry.amplifier());
            EffectEntry.putDouble("Chance", entry.baseChance());
            EffectList.add(EffectEntry);
        }
        tag.put("EffectList", EffectList);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt == null) return;
        if (nbt instanceof CompoundTag tag) {
            CompoundTag IndependentStatMap = tag.getCompound("IndependentStats");
            for (IndependentStatType stat : IndependentStatType.values()) {
                setIndependentStat(stat, IndependentStatMap.getDouble(stat.name()));
            }
            setElementType(ElementType.values() [tag.getInt("ElementType")]);
            setDmgInstanceType(DmgInstanceType.values() [tag.getInt("DmgInstanceType")]);
            setIndependentStat(IndependentStatType.STAT_RECALCULATION_TIME, 1);

            setWeapon(ItemStack.EMPTY);

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
                        int amplifier = compound.getInt("Amplifier");
                        double chance = compound.getDouble("Chance");
                        if (effect != null) {
                            effects.add(new StatusEffectEntry(effect, duration, amplifier, chance));
                        }
                    }
                }

                setEffectList(effects);
            }
        }
    }
}
