package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.block.ModBlocksWithFireResistantItem;
import com.w1therx.adventurerfantasy.util.ModTags;
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
                .add(ModBlocksWithFireResistantItem.BLIGHT_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.DECAY_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.EARTH_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.ECHO_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.FIRE_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.ICE_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.IMAGINATION_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.LAVA_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.LIGHTNING_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.MOTION_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.NATURE_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.VOID_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.WATER_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.WIND_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.DIVINITIES_DIVINITY.get());

        tag(ModTags.DIVINITIES)
                .add(ModBlocksWithFireResistantItem.BLIGHT_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.DECAY_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.EARTH_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.ECHO_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.FIRE_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.ICE_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.IMAGINATION_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.LAVA_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.LIGHTNING_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.MOTION_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.NATURE_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.VOID_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.WATER_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.WIND_DIVINITY.get())
                .add(ModBlocksWithFireResistantItem.DIVINITIES_DIVINITY.get());
        tag(ModTags.PRINCIPLES)
                .add(ModBlocksWithFireResistantItem.BLIGHT_PRINCIPLE.get())
                .add(ModBlocksWithFireResistantItem.DECAY_PRINCIPLE.get())
                .add(ModBlocksWithFireResistantItem.EARTH_PRINCIPLE.get())
                .add(ModBlocksWithFireResistantItem.ECHO_PRINCIPLE.get())
                .add(ModBlocksWithFireResistantItem.FIRE_PRINCIPLE.get())
                .add(ModBlocksWithFireResistantItem.ICE_PRINCIPLE.get())
                .add(ModBlocksWithFireResistantItem.IMAGINATION_PRINCIPLE.get())
                .add(ModBlocksWithFireResistantItem.LAVA_PRINCIPLE.get())
                .add(ModBlocksWithFireResistantItem.LIGHTNING_PRINCIPLE.get())
                .add(ModBlocksWithFireResistantItem.MOTION_PRINCIPLE.get())
                .add(ModBlocksWithFireResistantItem.NATURE_PRINCIPLE.get())
                .add(ModBlocksWithFireResistantItem.VOID_PRINCIPLE.get())
                .add(ModBlocksWithFireResistantItem.WATER_PRINCIPLE.get())
                .add(ModBlocksWithFireResistantItem.WIND_PRINCIPLE.get())
                .add(ModBlocksWithFireResistantItem.PRINCIPLES_PRINCIPLE.get());
    }
}
