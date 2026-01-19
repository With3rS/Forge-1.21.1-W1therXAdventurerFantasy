package com.w1therx.adventurerfantasy.datagen;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.block.ModBlocksWithFireResistantItem;
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


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.BLIGHT_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.BLIGHT_LOGOS.get())
                .unlockedBy(getHasName(ModItems.BLIGHT_LOGOS.get()), has(ModItems.BLIGHT_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.DECAY_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.DECAY_LOGOS.get())
                .unlockedBy(getHasName(ModItems.DECAY_LOGOS.get()), has(ModItems.DECAY_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.EARTH_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.EARTH_LOGOS.get())
                .unlockedBy(getHasName(ModItems.EARTH_LOGOS.get()), has(ModItems.EARTH_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.ECHO_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.ECHO_LOGOS.get())
                .unlockedBy(getHasName(ModItems.ECHO_LOGOS.get()), has(ModItems.ECHO_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.FIRE_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.FIRE_LOGOS.get())
                .unlockedBy(getHasName(ModItems.FIRE_LOGOS.get()), has(ModItems.FIRE_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.ICE_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.ICE_LOGOS.get())
                .unlockedBy(getHasName(ModItems.ICE_LOGOS.get()), has(ModItems.ICE_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.IMAGINATION_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.IMAGINATION_LOGOS.get())
                .unlockedBy(getHasName(ModItems.IMAGINATION_LOGOS.get()), has(ModItems.IMAGINATION_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.LAVA_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.LAVA_LOGOS.get())
                .unlockedBy(getHasName(ModItems.LAVA_LOGOS.get()), has(ModItems.LAVA_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.LIGHTNING_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.LIGHTNING_LOGOS.get())
                .unlockedBy(getHasName(ModItems.LIGHTNING_LOGOS.get()), has(ModItems.LIGHTNING_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.MOTION_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.MOTION_LOGOS.get())
                .unlockedBy(getHasName(ModItems.MOTION_LOGOS.get()), has(ModItems.MOTION_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.NATURE_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.NATURE_LOGOS.get())
                .unlockedBy(getHasName(ModItems.NATURE_LOGOS.get()), has(ModItems.NATURE_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.VOID_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.VOID_LOGOS.get())
                .unlockedBy(getHasName(ModItems.VOID_LOGOS.get()), has(ModItems.VOID_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.WATER_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.WATER_LOGOS.get())
                .unlockedBy(getHasName(ModItems.WATER_LOGOS.get()), has(ModItems.WATER_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.WIND_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.WIND_LOGOS.get())
                .unlockedBy(getHasName(ModItems.WIND_LOGOS.get()), has(ModItems.WIND_LOGOS.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.DIVINITIES_DIVINITY.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.LOGOI_LOGOS.get())
                .unlockedBy(getHasName(ModItems.LOGOI_LOGOS.get()), has(ModItems.LOGOI_LOGOS.get())).save(pRecipeOutput);


        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BLIGHT_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.BLIGHT_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.BLIGHT_DIVINITY.get()), has(ModBlocksWithFireResistantItem.BLIGHT_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":blight_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DECAY_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.DECAY_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.DECAY_DIVINITY.get()), has(ModBlocksWithFireResistantItem.DECAY_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":decay_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.EARTH_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.EARTH_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.EARTH_DIVINITY.get()), has(ModBlocksWithFireResistantItem.EARTH_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":earth_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ECHO_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.ECHO_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.ECHO_DIVINITY.get()), has(ModBlocksWithFireResistantItem.ECHO_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":echo_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.FIRE_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.FIRE_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.FIRE_DIVINITY.get()), has(ModBlocksWithFireResistantItem.FIRE_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":fire_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ICE_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.ICE_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.ICE_DIVINITY.get()), has(ModBlocksWithFireResistantItem.ICE_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":ice_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.IMAGINATION_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.IMAGINATION_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.IMAGINATION_DIVINITY.get()), has(ModBlocksWithFireResistantItem.IMAGINATION_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":imagination_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.LAVA_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.LAVA_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.LAVA_DIVINITY.get()), has(ModBlocksWithFireResistantItem.LAVA_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":lava_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.LIGHTNING_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.LIGHTNING_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.LIGHTNING_DIVINITY.get()), has(ModBlocksWithFireResistantItem.LIGHTNING_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":lightning_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MOTION_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.MOTION_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.MOTION_DIVINITY.get()), has(ModBlocksWithFireResistantItem.MOTION_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":motion_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.NATURE_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.NATURE_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.NATURE_DIVINITY.get()), has(ModBlocksWithFireResistantItem.NATURE_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":nature_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.VOID_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.VOID_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.VOID_DIVINITY.get()), has(ModBlocksWithFireResistantItem.VOID_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":void_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.WATER_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.WATER_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.WATER_DIVINITY.get()), has(ModBlocksWithFireResistantItem.WATER_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":water_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.WIND_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.WIND_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.WIND_DIVINITY.get()), has(ModBlocksWithFireResistantItem.WIND_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":wind_logos_from_divinity");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.LOGOI_LOGOS.get(), 9)
                .requires(ModBlocksWithFireResistantItem.DIVINITIES_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.DIVINITIES_DIVINITY.get()), has(ModBlocksWithFireResistantItem.DIVINITIES_DIVINITY.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":logoi_logos_from_divinity");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.BLIGHT_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.BLIGHT_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.BLIGHT_DIVINITY.get()), has(ModBlocksWithFireResistantItem.BLIGHT_DIVINITY.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.DECAY_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.DECAY_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.DECAY_DIVINITY.get()), has(ModBlocksWithFireResistantItem.DECAY_DIVINITY.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.EARTH_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.EARTH_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.EARTH_DIVINITY.get()), has(ModBlocksWithFireResistantItem.EARTH_DIVINITY.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.ECHO_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.ECHO_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.ECHO_DIVINITY.get()), has(ModBlocksWithFireResistantItem.ECHO_DIVINITY.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.FIRE_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.FIRE_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.FIRE_DIVINITY.get()), has(ModBlocksWithFireResistantItem.FIRE_DIVINITY.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.ICE_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.ICE_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.ICE_DIVINITY.get()), has(ModBlocksWithFireResistantItem.ICE_DIVINITY.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.IMAGINATION_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.IMAGINATION_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.IMAGINATION_DIVINITY.get()), has(ModBlocksWithFireResistantItem.IMAGINATION_DIVINITY.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.LAVA_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.LAVA_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.LAVA_DIVINITY.get()), has(ModBlocksWithFireResistantItem.LAVA_DIVINITY.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.LIGHTNING_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.LIGHTNING_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.LIGHTNING_DIVINITY.get()), has(ModBlocksWithFireResistantItem.LIGHTNING_DIVINITY.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.MOTION_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.MOTION_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.MOTION_DIVINITY.get()), has(ModBlocksWithFireResistantItem.MOTION_DIVINITY.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.NATURE_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.NATURE_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.NATURE_DIVINITY.get()), has(ModBlocksWithFireResistantItem.NATURE_DIVINITY.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.VOID_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.VOID_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.VOID_DIVINITY.get()), has(ModBlocksWithFireResistantItem.VOID_DIVINITY.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.WATER_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.WATER_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.WATER_DIVINITY.get()), has(ModBlocksWithFireResistantItem.WATER_DIVINITY.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.WIND_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.WIND_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.WIND_DIVINITY.get()), has(ModBlocksWithFireResistantItem.WIND_DIVINITY.get())).save(pRecipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocksWithFireResistantItem.PRINCIPLES_PRINCIPLE.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModBlocksWithFireResistantItem.DIVINITIES_DIVINITY.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.DIVINITIES_DIVINITY.get()), has(ModBlocksWithFireResistantItem.DIVINITIES_DIVINITY.get())).save(pRecipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.BLIGHT_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.BLIGHT_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.BLIGHT_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.BLIGHT_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":blight_divinity_from_principle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.DECAY_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.DECAY_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.DECAY_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.DECAY_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":decay_divinity_from_principle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.EARTH_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.EARTH_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.EARTH_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.EARTH_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":earth_divinity_from_principle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.ECHO_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.ECHO_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.ECHO_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.ECHO_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":echo_divinity_from_principle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.FIRE_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.FIRE_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.FIRE_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.FIRE_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":fire_divinity_from_principle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.ICE_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.ICE_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.ICE_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.ICE_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":ice_divinity_from_principle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.IMAGINATION_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.IMAGINATION_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.IMAGINATION_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.IMAGINATION_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":imagination_divinity_from_principle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.LAVA_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.LAVA_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.LAVA_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.LAVA_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":lava_divinity_from_principle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.LIGHTNING_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.LIGHTNING_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.LIGHTNING_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.LIGHTNING_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":lightning_divinity_from_principle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.MOTION_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.MOTION_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.MOTION_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.MOTION_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":motion_divinity_from_principle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.NATURE_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.NATURE_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.NATURE_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.NATURE_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":nature_divinity_from_principle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.VOID_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.VOID_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.VOID_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.VOID_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":void_divinity_from_principle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.WATER_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.WATER_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.WATER_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.WATER_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":water_divinity_from_principle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.WIND_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.WIND_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.WIND_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.WIND_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":wind_divinity_from_principle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocksWithFireResistantItem.DIVINITIES_DIVINITY.get(), 9)
                .requires(ModBlocksWithFireResistantItem.PRINCIPLES_PRINCIPLE.get())
                .unlockedBy(getHasName(ModBlocksWithFireResistantItem.PRINCIPLES_PRINCIPLE.get()), has(ModBlocksWithFireResistantItem.PRINCIPLES_PRINCIPLE.get())).save(pRecipeOutput, AdventurerFantasy.MOD_ID + ":divinities_divinity_from_principle");

    }
}
