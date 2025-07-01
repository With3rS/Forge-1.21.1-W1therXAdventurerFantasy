package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, AdventurerFantasy.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(ModBlocks.BLIGHT_DIVINITY.get())
            .add(ModBlocks.DECAY_DIVINITY.get())
                .add(ModBlocks.EARTH_DIVINITY.get())
                .add(ModBlocks.ECHO_DIVINITY.get())
                .add(ModBlocks.FIRE_DIVINITY.get())
                .add(ModBlocks.ICE_DIVINITY.get())
                .add(ModBlocks.IMAGINATION_DIVINITY.get())
                .add(ModBlocks.LAVA_DIVINITY.get())
                .add(ModBlocks.LIGHTNING_DIVINITY.get())
                .add(ModBlocks.MOTION_DIVINITY.get())
                .add(ModBlocks.NATURE_DIVINITY.get())
                .add(ModBlocks.VOID_DIVINITY.get())
                .add(ModBlocks.WATER_DIVINITY.get())
                .add(ModBlocks.WIND_DIVINITY.get())
                .add(ModBlocks.DIVINITIES_DIVINITY.get());

    }
}
