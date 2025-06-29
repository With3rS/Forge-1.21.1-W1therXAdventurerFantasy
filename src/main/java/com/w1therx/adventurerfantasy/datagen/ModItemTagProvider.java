package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
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

    }
}
