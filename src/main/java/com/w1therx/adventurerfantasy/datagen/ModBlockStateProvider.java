package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.block.ModBlocksWithFireResistantItem;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, AdventurerFantasy.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
       blockWithItem(ModBlocksWithFireResistantItem.BLIGHT_DIVINITY);
        blockWithItem(ModBlocksWithFireResistantItem.DECAY_DIVINITY);
        blockWithItem(ModBlocksWithFireResistantItem.EARTH_DIVINITY);
        blockWithItem(ModBlocksWithFireResistantItem.ECHO_DIVINITY);
        blockWithItem(ModBlocksWithFireResistantItem.FIRE_DIVINITY);
        blockWithItem(ModBlocksWithFireResistantItem.ICE_DIVINITY);
        blockWithItem(ModBlocksWithFireResistantItem.IMAGINATION_DIVINITY);
        blockWithItem(ModBlocksWithFireResistantItem.LAVA_DIVINITY);
        blockWithItem(ModBlocksWithFireResistantItem.LIGHTNING_DIVINITY);
        blockWithItem(ModBlocksWithFireResistantItem.MOTION_DIVINITY);
        blockWithItem(ModBlocksWithFireResistantItem.NATURE_DIVINITY);
        blockWithItem(ModBlocksWithFireResistantItem.VOID_DIVINITY);
        blockWithItem(ModBlocksWithFireResistantItem.WATER_DIVINITY);
        blockWithItem(ModBlocksWithFireResistantItem.WIND_DIVINITY);
        blockWithItem(ModBlocksWithFireResistantItem.DIVINITIES_DIVINITY);

    };

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
}

}
