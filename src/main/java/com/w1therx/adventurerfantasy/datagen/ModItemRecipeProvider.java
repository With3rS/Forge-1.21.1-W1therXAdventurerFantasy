package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.block.ModBlocks;
import com.w1therx.adventurerfantasy.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;

import java.util.concurrent.CompletableFuture;

public class ModItemRecipeProvider extends RecipeProvider {
    public ModItemRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(RecipeOutput pRecipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BLIGHT_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.BLIGHT_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.BLIGHT_ESSENCE.get()), has(ModItems.BLIGHT_ESSENCE.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DECAY_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.DECAY_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.DECAY_ESSENCE.get()), has(ModItems.DECAY_ESSENCE.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EARTH_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.EARTH_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.EARTH_ESSENCE.get()), has(ModItems.EARTH_ESSENCE.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ECHO_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.ECHO_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.ECHO_ESSENCE.get()), has(ModItems.ECHO_ESSENCE.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FIRE_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.FIRE_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.FIRE_ESSENCE.get()), has(ModItems.FIRE_ESSENCE.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ICE_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.ICE_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.ICE_ESSENCE.get()), has(ModItems.ICE_ESSENCE.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IMAGINATION_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.IMAGINATION_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.IMAGINATION_ESSENCE.get()), has(ModItems.IMAGINATION_ESSENCE.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LAVA_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.LAVA_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.LAVA_ESSENCE.get()), has(ModItems.LAVA_ESSENCE.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LIGHTNING_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.LIGHTNING_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.LIGHTNING_ESSENCE.get()), has(ModItems.LIGHTNING_ESSENCE.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MOTION_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.MOTION_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.MOTION_ESSENCE.get()), has(ModItems.MOTION_ESSENCE.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.NATURE_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.NATURE_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.NATURE_ESSENCE.get()), has(ModItems.NATURE_ESSENCE.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.VOID_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.VOID_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.VOID_ESSENCE.get()), has(ModItems.VOID_ESSENCE.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WATER_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.WATER_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.WATER_ESSENCE.get()), has(ModItems.WATER_ESSENCE.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WIND_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.WIND_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.WIND_ESSENCE.get()), has(ModItems.WIND_ESSENCE.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LOGOI_LOGOS.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.ESSENCES_ESSENCE.get())
                .unlockedBy(getHasName(ModItems.ESSENCES_ESSENCE.get()), has(ModItems.ESSENCES_ESSENCE.get())).save(pRecipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BLIGHT_ESSENCE.get(), 9)
                .requires(ModItems.BLIGHT_LOGOS.get())
                .unlockedBy(getHasName(ModItems.BLIGHT_LOGOS.get()), has(ModItems.BLIGHT_LOGOS.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DECAY_ESSENCE.get(), 9)
                .requires(ModItems.DECAY_LOGOS.get())
                .unlockedBy(getHasName(ModItems.DECAY_LOGOS.get()), has(ModItems.DECAY_LOGOS.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.EARTH_ESSENCE.get(), 9)
                .requires(ModItems.EARTH_LOGOS.get())
                .unlockedBy(getHasName(ModItems.EARTH_LOGOS.get()), has(ModItems.EARTH_LOGOS.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ECHO_ESSENCE.get(), 9)
                .requires(ModItems.ECHO_LOGOS.get())
                .unlockedBy(getHasName(ModItems.ECHO_LOGOS.get()), has(ModItems.ECHO_LOGOS.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.FIRE_ESSENCE.get(), 9)
                .requires(ModItems.FIRE_LOGOS.get())
                .unlockedBy(getHasName(ModItems.FIRE_LOGOS.get()), has(ModItems.FIRE_LOGOS.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ICE_ESSENCE.get(), 9)
                .requires(ModItems.ICE_LOGOS.get())
                .unlockedBy(getHasName(ModItems.ICE_LOGOS.get()), has(ModItems.ICE_LOGOS.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.IMAGINATION_ESSENCE.get(), 9)
                .requires(ModItems.IMAGINATION_LOGOS.get())
                .unlockedBy(getHasName(ModItems.IMAGINATION_LOGOS.get()), has(ModItems.IMAGINATION_LOGOS.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.LAVA_ESSENCE.get(), 9)
                .requires(ModItems.LAVA_LOGOS.get())
                .unlockedBy(getHasName(ModItems.LAVA_LOGOS.get()), has(ModItems.LAVA_LOGOS.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.LIGHTNING_ESSENCE.get(), 9)
                .requires(ModItems.LIGHTNING_LOGOS.get())
                .unlockedBy(getHasName(ModItems.LIGHTNING_LOGOS.get()), has(ModItems.LIGHTNING_LOGOS.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MOTION_ESSENCE.get(), 9)
                .requires(ModItems.MOTION_LOGOS.get())
                .unlockedBy(getHasName(ModItems.MOTION_LOGOS.get()), has(ModItems.MOTION_LOGOS.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.NATURE_ESSENCE.get(), 9)
                .requires(ModItems.NATURE_LOGOS.get())
                .unlockedBy(getHasName(ModItems.NATURE_LOGOS.get()), has(ModItems.NATURE_LOGOS.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.VOID_ESSENCE.get(), 9)
                .requires(ModItems.VOID_LOGOS.get())
                .unlockedBy(getHasName(ModItems.VOID_LOGOS.get()), has(ModItems.VOID_LOGOS.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.WATER_ESSENCE.get(), 9)
                .requires(ModItems.WATER_LOGOS.get())
                .unlockedBy(getHasName(ModItems.WATER_LOGOS.get()), has(ModItems.WATER_LOGOS.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.WIND_ESSENCE.get(), 9)
                .requires(ModItems.WIND_LOGOS.get())
                .unlockedBy(getHasName(ModItems.WIND_LOGOS.get()), has(ModItems.WIND_LOGOS.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ESSENCES_ESSENCE.get(), 9)
                .requires(ModItems.LOGOI_LOGOS.get())
                .unlockedBy(getHasName(ModItems.LOGOI_LOGOS.get()), has(ModItems.LOGOI_LOGOS.get())).save(pRecipeOutput);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BLIGHT_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.BLIGHT_LOGOS.get())
                .unlockedBy(getHasName(ModItems.BLIGHT_LOGOS.get()), has(ModItems.BLIGHT_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.DECAY_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.DECAY_LOGOS.get())
                .unlockedBy(getHasName(ModItems.DECAY_LOGOS.get()), has(ModItems.DECAY_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.EARTH_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.EARTH_LOGOS.get())
                .unlockedBy(getHasName(ModItems.EARTH_LOGOS.get()), has(ModItems.EARTH_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ECHO_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.ECHO_LOGOS.get())
                .unlockedBy(getHasName(ModItems.ECHO_LOGOS.get()), has(ModItems.ECHO_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FIRE_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.FIRE_LOGOS.get())
                .unlockedBy(getHasName(ModItems.FIRE_LOGOS.get()), has(ModItems.FIRE_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ICE_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.ICE_LOGOS.get())
                .unlockedBy(getHasName(ModItems.ICE_LOGOS.get()), has(ModItems.ICE_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.IMAGINATION_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.IMAGINATION_LOGOS.get())
                .unlockedBy(getHasName(ModItems.IMAGINATION_LOGOS.get()), has(ModItems.IMAGINATION_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.LAVA_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.LAVA_LOGOS.get())
                .unlockedBy(getHasName(ModItems.LAVA_LOGOS.get()), has(ModItems.LAVA_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.LIGHTNING_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.LIGHTNING_LOGOS.get())
                .unlockedBy(getHasName(ModItems.LIGHTNING_LOGOS.get()), has(ModItems.LIGHTNING_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.MOTION_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.MOTION_LOGOS.get())
                .unlockedBy(getHasName(ModItems.MOTION_LOGOS.get()), has(ModItems.MOTION_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.NATURE_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.NATURE_LOGOS.get())
                .unlockedBy(getHasName(ModItems.NATURE_LOGOS.get()), has(ModItems.NATURE_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.VOID_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.VOID_LOGOS.get())
                .unlockedBy(getHasName(ModItems.VOID_LOGOS.get()), has(ModItems.VOID_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.WATER_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.WATER_LOGOS.get())
                .unlockedBy(getHasName(ModItems.WATER_LOGOS.get()), has(ModItems.WATER_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.WIND_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.WIND_LOGOS.get())
                .unlockedBy(getHasName(ModItems.WIND_LOGOS.get()), has(ModItems.WIND_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.DIVINITIES_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.LOGOI_LOGOS.get())
                .unlockedBy(getHasName(ModItems.LOGOI_LOGOS.get()), has(ModItems.LOGOI_LOGOS.get())).save(pRecipeOutput);


        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BLIGHT_LOGOS.get(), 9)
                .requires(ModBlocks.BLIGHT_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.BLIGHT_DIVINITY.get()), has(ModBlocks.BLIGHT_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":blight_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DECAY_LOGOS.get(), 9)
                .requires(ModBlocks.DECAY_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.DECAY_DIVINITY.get()), has(ModBlocks.DECAY_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":decay_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.EARTH_LOGOS.get(), 9)
                .requires(ModBlocks.EARTH_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.EARTH_DIVINITY.get()), has(ModBlocks.EARTH_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":earth_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ECHO_LOGOS.get(), 9)
                .requires(ModBlocks.ECHO_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.ECHO_DIVINITY.get()), has(ModBlocks.ECHO_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":echo_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.FIRE_LOGOS.get(), 9)
                .requires(ModBlocks.FIRE_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.FIRE_DIVINITY.get()), has(ModBlocks.FIRE_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":fire_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ICE_LOGOS.get(), 9)
                .requires(ModBlocks.ICE_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.ICE_DIVINITY.get()), has(ModBlocks.ICE_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":ice_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.IMAGINATION_LOGOS.get(), 9)
                .requires(ModBlocks.IMAGINATION_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.IMAGINATION_DIVINITY.get()), has(ModBlocks.IMAGINATION_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":imagination_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.LAVA_LOGOS.get(), 9)
                .requires(ModBlocks.LAVA_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.LAVA_DIVINITY.get()), has(ModBlocks.LAVA_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":lava_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.LIGHTNING_LOGOS.get(), 9)
                .requires(ModBlocks.LIGHTNING_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.LIGHTNING_DIVINITY.get()), has(ModBlocks.LIGHTNING_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":lightning_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MOTION_LOGOS.get(), 9)
                .requires(ModBlocks.MOTION_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.MOTION_DIVINITY.get()), has(ModBlocks.MOTION_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":motion_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.NATURE_LOGOS.get(), 9)
                .requires(ModBlocks.NATURE_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.NATURE_DIVINITY.get()), has(ModBlocks.NATURE_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":nature_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.VOID_LOGOS.get(), 9)
                .requires(ModBlocks.VOID_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.VOID_DIVINITY.get()), has(ModBlocks.VOID_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":void_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.WATER_LOGOS.get(), 9)
                .requires(ModBlocks.WATER_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.WATER_DIVINITY.get()), has(ModBlocks.WATER_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":water_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.WIND_LOGOS.get(), 9)
                .requires(ModBlocks.WIND_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.WIND_DIVINITY.get()), has(ModBlocks.WIND_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":wind_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.LOGOI_LOGOS.get(), 9)
                .requires(ModBlocks.DIVINITIES_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocks.DIVINITIES_DIVINITY.get()), has(ModBlocks.DIVINITIES_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":logoi_logos_from_divinity");



    }
}
