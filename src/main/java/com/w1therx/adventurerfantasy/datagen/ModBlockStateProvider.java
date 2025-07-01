package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.block.ModBlocks;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, AdventurerFantasy.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
       blockWithItem(ModBlocks.BLIGHT_DIVINITY);
        blockWithItem(ModBlocks.DECAY_DIVINITY);
        blockWithItem(ModBlocks.EARTH_DIVINITY);
        blockWithItem(ModBlocks.ECHO_DIVINITY);
        blockWithItem(ModBlocks.FIRE_DIVINITY);
        blockWithItem(ModBlocks.ICE_DIVINITY);
        blockWithItem(ModBlocks.IMAGINATION_DIVINITY);
        blockWithItem(ModBlocks.LAVA_DIVINITY);
        blockWithItem(ModBlocks.LIGHTNING_DIVINITY);
        blockWithItem(ModBlocks.MOTION_DIVINITY);
        blockWithItem(ModBlocks.NATURE_DIVINITY);
        blockWithItem(ModBlocks.VOID_DIVINITY);
        blockWithItem(ModBlocks.WATER_DIVINITY);
        blockWithItem(ModBlocks.WIND_DIVINITY);
        blockWithItem(ModBlocks.DIVINITIES_DIVINITY);

    };

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
}

}
