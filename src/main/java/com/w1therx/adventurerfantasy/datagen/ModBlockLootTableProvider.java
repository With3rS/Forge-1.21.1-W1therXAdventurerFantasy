package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.block.DivinitiesBlocks;
import com.w1therx.adventurerfantasy.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;
import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    protected ModBlockLootTableProvider(HolderLookup.Provider pRegistries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), pRegistries);
    }

    @Override
    protected void generate() {
        dropSelf(DivinitiesBlocks.BLIGHT_DIVINITY.get());
        dropSelf(DivinitiesBlocks.DECAY_DIVINITY.get());
        dropSelf(DivinitiesBlocks.EARTH_DIVINITY.get());
        dropSelf(DivinitiesBlocks.ECHO_DIVINITY.get());
        dropSelf(DivinitiesBlocks.FIRE_DIVINITY.get());
        dropSelf(DivinitiesBlocks.ICE_DIVINITY.get());
        dropSelf(DivinitiesBlocks.IMAGINATION_DIVINITY.get());
        dropSelf(DivinitiesBlocks.LAVA_DIVINITY.get());
        dropSelf(DivinitiesBlocks.LIGHTNING_DIVINITY.get());
        dropSelf(DivinitiesBlocks.MOTION_DIVINITY.get());
        dropSelf(DivinitiesBlocks.NATURE_DIVINITY.get());
        dropSelf(DivinitiesBlocks.VOID_DIVINITY.get());
        dropSelf(DivinitiesBlocks.WATER_DIVINITY.get());
        dropSelf(DivinitiesBlocks.WIND_DIVINITY.get());
        dropSelf(DivinitiesBlocks.DIVINITIES_DIVINITY.get());

    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return DivinitiesBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
