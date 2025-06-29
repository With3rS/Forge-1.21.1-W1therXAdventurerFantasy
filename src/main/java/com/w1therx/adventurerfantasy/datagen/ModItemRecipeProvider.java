package com.w1therx.adventurerfantasy.datagen;

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

    }
}
