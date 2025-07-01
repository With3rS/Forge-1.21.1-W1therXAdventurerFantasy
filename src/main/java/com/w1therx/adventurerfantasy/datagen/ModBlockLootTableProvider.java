package com.w1therx.adventurerfantasy.datagen;

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
        dropSelf(ModBlocks.BLIGHT_DIVINITY.get());
        dropSelf(ModBlocks.DECAY_DIVINITY.get());
        dropSelf(ModBlocks.EARTH_DIVINITY.get());
        dropSelf(ModBlocks.ECHO_DIVINITY.get());
        dropSelf(ModBlocks.FIRE_DIVINITY.get());
        dropSelf(ModBlocks.ICE_DIVINITY.get());
        dropSelf(ModBlocks.IMAGINATION_DIVINITY.get());
        dropSelf(ModBlocks.LAVA_DIVINITY.get());
        dropSelf(ModBlocks.LIGHTNING_DIVINITY.get());
        dropSelf(ModBlocks.MOTION_DIVINITY.get());
        dropSelf(ModBlocks.NATURE_DIVINITY.get());
        dropSelf(ModBlocks.VOID_DIVINITY.get());
        dropSelf(ModBlocks.WATER_DIVINITY.get());
        dropSelf(ModBlocks.WIND_DIVINITY.get());
        dropSelf(ModBlocks.DIVINITIES_DIVINITY.get());

    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
