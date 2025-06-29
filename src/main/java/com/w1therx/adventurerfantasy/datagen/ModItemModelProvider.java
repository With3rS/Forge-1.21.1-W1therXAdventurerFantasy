package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AdventurerFantasy.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.BLIGHT_LOGOS.get());
        basicItem(ModItems.DECAY_LOGOS.get());
        basicItem(ModItems.EARTH_LOGOS.get());
        basicItem(ModItems.ECHO_LOGOS.get());
        basicItem(ModItems.FIRE_LOGOS.get());
        basicItem(ModItems.ICE_LOGOS.get());
        basicItem(ModItems.IMAGINATION_LOGOS.get());
        basicItem(ModItems.LAVA_LOGOS.get());
        basicItem(ModItems.LIGHTNING_LOGOS.get());
        basicItem(ModItems.MOTION_LOGOS.get());
        basicItem(ModItems.NATURE_LOGOS.get());
        basicItem(ModItems.VOID_LOGOS.get());
        basicItem(ModItems.WATER_LOGOS.get());
        basicItem(ModItems.WIND_LOGOS.get());
        basicItem(ModItems.LOGOI_LOGOS.get());
        basicItem(ModItems.ESSENCES_ESSENCE.get());


    }
}
