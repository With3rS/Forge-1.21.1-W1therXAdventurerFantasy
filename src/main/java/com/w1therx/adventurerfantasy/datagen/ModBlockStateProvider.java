package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.block.DivinitiesBlocks;
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
       blockWithItem(DivinitiesBlocks.BLIGHT_DIVINITY);
        blockWithItem(DivinitiesBlocks.DECAY_DIVINITY);
        blockWithItem(DivinitiesBlocks.EARTH_DIVINITY);
        blockWithItem(DivinitiesBlocks.ECHO_DIVINITY);
        blockWithItem(DivinitiesBlocks.FIRE_DIVINITY);
        blockWithItem(DivinitiesBlocks.ICE_DIVINITY);
        blockWithItem(DivinitiesBlocks.IMAGINATION_DIVINITY);
        blockWithItem(DivinitiesBlocks.LAVA_DIVINITY);
        blockWithItem(DivinitiesBlocks.LIGHTNING_DIVINITY);
        blockWithItem(DivinitiesBlocks.MOTION_DIVINITY);
        blockWithItem(DivinitiesBlocks.NATURE_DIVINITY);
        blockWithItem(DivinitiesBlocks.VOID_DIVINITY);
        blockWithItem(DivinitiesBlocks.WATER_DIVINITY);
        blockWithItem(DivinitiesBlocks.WIND_DIVINITY);
        blockWithItem(DivinitiesBlocks.DIVINITIES_DIVINITY);

    };

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
}

}
