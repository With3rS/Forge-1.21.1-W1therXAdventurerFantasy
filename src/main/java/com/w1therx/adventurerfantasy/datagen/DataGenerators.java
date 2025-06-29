package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = AdventurerFantasy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)

public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lockupProvider = event.getLookupProvider();

        //add ModBlockLootTableProvider
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));
        //add ModBlockStateProvider

    }
}