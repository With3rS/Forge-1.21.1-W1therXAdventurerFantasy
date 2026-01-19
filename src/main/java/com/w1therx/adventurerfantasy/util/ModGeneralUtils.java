package com.w1therx.adventurerfantasy.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

import java.util.Optional;
import java.util.UUID;

public class ModGeneralUtils {
    public static final UUID EMPTY_UUID = new UUID(0L, 0L);

    public static ResourceLocation getEffectIcon(MobEffect effect) {
        ResourceLocation registryName = BuiltInRegistries.MOB_EFFECT.getKey(effect);
        if (registryName == null) {
            return ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/general_effect.png");
        }
        Optional<Holder.Reference<MobEffect>> optional = BuiltInRegistries.MOB_EFFECT.getHolder(registryName);
        if (optional.isPresent()) {
            Holder<MobEffect> holder = optional.get();
            if (holder.is(ModTags.GENERAL_BUFF)) {
                return ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/general_buff.png");
            } else if (holder.is(ModTags.GENERAL_DEBUFF)) {
                return ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/general_debuff.png");
            } else if (holder.is(ModTags.GENERAL_NEUTRAL_EFFECT)) {
                return ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/general_neutral_effect.png");
            } else {
                String namespace = registryName.getNamespace();
                String path = registryName.getPath();
                return ResourceLocation.fromNamespaceAndPath(namespace, "textures/mob_effect/" + path + ".png");
            }
        } else {
            return ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/general_effect.png");
        }
    }

        public static boolean isVanillaEffect (MobEffect effect){
            ResourceLocation registryName = BuiltInRegistries.MOB_EFFECT.getKey(effect);
            if (registryName == null) {
                return false;
            }

            String namespace = registryName.getNamespace();

            return namespace.equals("minecraft");
    }

    public static String formatDuration(int duration) {
        if (duration == Integer.MAX_VALUE) {
            return "Infinite";
        } else {
            int secs = duration / 20;
            int mins = secs / 60;
            int hours = mins / 60;
            secs = secs % 60;
            mins = mins % 60;
            if (hours < 0) {
                if (mins < 0) {
                    return "" + secs;
                } else  {
                    if (secs > 9) {
                        return mins + ":" + secs;
                    } else {
                        return mins + ":0" + secs;
                    }
                }
            } else {
                if (mins < 10) {
                    if (secs > 9) {
                        return hours + ":0" + mins + ":" + secs;
                    } else {
                        return hours + ":0" + mins + ":0" + secs;
                    }
                } else {
                    if (secs > 9) {
                        return hours + ":" + mins + ":" + secs;
                    } else {
                        return hours + ":" + mins + ":0" + secs;
                    }
                }
            }
        }
    }
}
