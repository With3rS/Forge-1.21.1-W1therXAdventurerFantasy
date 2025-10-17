package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.block.DivinitiesBlocks;
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
            .add(DivinitiesBlocks.BLIGHT_DIVINITY.get())
            .add(DivinitiesBlocks.DECAY_DIVINITY.get())
                .add(DivinitiesBlocks.EARTH_DIVINITY.get())
                .add(DivinitiesBlocks.ECHO_DIVINITY.get())
                .add(DivinitiesBlocks.FIRE_DIVINITY.get())
                .add(DivinitiesBlocks.ICE_DIVINITY.get())
                .add(DivinitiesBlocks.IMAGINATION_DIVINITY.get())
                .add(DivinitiesBlocks.LAVA_DIVINITY.get())
                .add(DivinitiesBlocks.LIGHTNING_DIVINITY.get())
                .add(DivinitiesBlocks.MOTION_DIVINITY.get())
                .add(DivinitiesBlocks.NATURE_DIVINITY.get())
                .add(DivinitiesBlocks.VOID_DIVINITY.get())
                .add(DivinitiesBlocks.WATER_DIVINITY.get())
                .add(DivinitiesBlocks.WIND_DIVINITY.get())
                .add(DivinitiesBlocks.DIVINITIES_DIVINITY.get());

    }
}
