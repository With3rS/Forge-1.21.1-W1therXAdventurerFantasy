package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.block.ModBlocksWithFireResistantItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    protected ModBlockLootTableProvider(HolderLookup.Provider pRegistries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), pRegistries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocksWithFireResistantItem.BLIGHT_DIVINITY.get());
        dropSelf(ModBlocksWithFireResistantItem.DECAY_DIVINITY.get());
        dropSelf(ModBlocksWithFireResistantItem.EARTH_DIVINITY.get());
        dropSelf(ModBlocksWithFireResistantItem.ECHO_DIVINITY.get());
        dropSelf(ModBlocksWithFireResistantItem.FIRE_DIVINITY.get());
        dropSelf(ModBlocksWithFireResistantItem.ICE_DIVINITY.get());
        dropSelf(ModBlocksWithFireResistantItem.IMAGINATION_DIVINITY.get());
        dropSelf(ModBlocksWithFireResistantItem.LAVA_DIVINITY.get());
        dropSelf(ModBlocksWithFireResistantItem.LIGHTNING_DIVINITY.get());
        dropSelf(ModBlocksWithFireResistantItem.MOTION_DIVINITY.get());
        dropSelf(ModBlocksWithFireResistantItem.NATURE_DIVINITY.get());
        dropSelf(ModBlocksWithFireResistantItem.VOID_DIVINITY.get());
        dropSelf(ModBlocksWithFireResistantItem.WATER_DIVINITY.get());
        dropSelf(ModBlocksWithFireResistantItem.WIND_DIVINITY.get());
        dropSelf(ModBlocksWithFireResistantItem.DIVINITIES_DIVINITY.get());

        dropSelf(ModBlocksWithFireResistantItem.BLIGHT_PRINCIPLE.get());
        dropSelf(ModBlocksWithFireResistantItem.DECAY_PRINCIPLE.get());
        dropSelf(ModBlocksWithFireResistantItem.EARTH_PRINCIPLE.get());
        dropSelf(ModBlocksWithFireResistantItem.ECHO_PRINCIPLE.get());
        dropSelf(ModBlocksWithFireResistantItem.FIRE_PRINCIPLE.get());
        dropSelf(ModBlocksWithFireResistantItem.ICE_PRINCIPLE.get());
        dropSelf(ModBlocksWithFireResistantItem.IMAGINATION_PRINCIPLE.get());
        dropSelf(ModBlocksWithFireResistantItem.LAVA_PRINCIPLE.get());
        dropSelf(ModBlocksWithFireResistantItem.LIGHTNING_PRINCIPLE.get());
        dropSelf(ModBlocksWithFireResistantItem.MOTION_PRINCIPLE.get());
        dropSelf(ModBlocksWithFireResistantItem.NATURE_PRINCIPLE.get());
        dropSelf(ModBlocksWithFireResistantItem.VOID_PRINCIPLE.get());
        dropSelf(ModBlocksWithFireResistantItem.WATER_PRINCIPLE.get());
        dropSelf(ModBlocksWithFireResistantItem.WIND_PRINCIPLE.get());
        dropSelf(ModBlocksWithFireResistantItem.PRINCIPLES_PRINCIPLE.get());

    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocksWithFireResistantItem.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
