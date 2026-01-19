package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.item.ModItems;
import com.w1therx.adventurerfantasy.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> providerCompletableFuture, CompletableFuture<TagLookup<Block>> aSuper, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, providerCompletableFuture, aSuper, AdventurerFantasy.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {

        tag(ModTags.ESSENCES)
                .add(ModItems.BLIGHT_ESSENCE.get())
                .add(ModItems.DECAY_ESSENCE.get())
                .add(ModItems.EARTH_ESSENCE.get())
                .add(ModItems.ECHO_ESSENCE.get())
                .add(ModItems.FIRE_ESSENCE.get())
                .add(ModItems.ICE_ESSENCE.get())
                .add(ModItems.IMAGINATION_ESSENCE.get())
                .add(ModItems.LAVA_ESSENCE.get())
                .add(ModItems.LIGHTNING_ESSENCE.get())
                .add(ModItems.MOTION_ESSENCE.get())
                .add(ModItems.NATURE_ESSENCE.get())
                .add(ModItems.VOID_ESSENCE.get())
                .add(ModItems.WATER_ESSENCE.get())
                .add(ModItems.WIND_ESSENCE.get())
                .add(ModItems.ESSENCES_ESSENCE.get());
        tag(ModTags.LOGOI)
                .add(ModItems.BLIGHT_LOGOS.get())
                .add(ModItems.DECAY_LOGOS.get())
                .add(ModItems.EARTH_LOGOS.get())
                .add(ModItems.ECHO_LOGOS.get())
                .add(ModItems.FIRE_LOGOS.get())
                .add(ModItems.ICE_LOGOS.get())
                .add(ModItems.IMAGINATION_LOGOS.get())
                .add(ModItems.LAVA_LOGOS.get())
                .add(ModItems.LIGHTNING_LOGOS.get())
                .add(ModItems.MOTION_LOGOS.get())
                .add(ModItems.NATURE_LOGOS.get())
                .add(ModItems.VOID_LOGOS.get())
                .add(ModItems.WATER_LOGOS.get())
                .add(ModItems.WIND_LOGOS.get())
                .add(ModItems.LOGOI_LOGOS.get());
    }
}
