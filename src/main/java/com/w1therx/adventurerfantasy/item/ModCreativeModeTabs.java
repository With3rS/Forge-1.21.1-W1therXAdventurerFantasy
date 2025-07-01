package com.w1therx.adventurerfantasy.item;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AdventurerFantasy.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ELEMENTAL_INGREDIENTS_TAB = CREATIVE_MODE_TABS.register("elemental_ingredients_tab", () -> CreativeModeTab.builder().icon(()-> new ItemStack(ModItems.LOGOI_LOGOS.get()))
            .title(Component.translatable("creativetab.adventurerfantasy.elemental_ingredients"))
            .displayItems((itemDisplayParameters, output) -> {

                output.accept(ModItems.BLIGHT_ESSENCE.get());
                output.accept(ModItems.DECAY_ESSENCE.get());
                output.accept(ModItems.EARTH_ESSENCE.get());
                output.accept(ModItems.ECHO_ESSENCE.get());
                output.accept(ModItems.FIRE_ESSENCE.get());
                output.accept(ModItems.ICE_ESSENCE.get());
                output.accept(ModItems.IMAGINATION_ESSENCE.get());
                output.accept(ModItems.LAVA_ESSENCE.get());
                output.accept(ModItems.LIGHTNING_ESSENCE.get());
                output.accept(ModItems.MOTION_ESSENCE.get());
                output.accept(ModItems.NATURE_ESSENCE.get());
                output.accept(ModItems.VOID_ESSENCE.get());
                output.accept(ModItems.WATER_ESSENCE.get());
                output.accept(ModItems.WIND_ESSENCE.get());
                output.accept(ModItems.ESSENCES_ESSENCE.get());

                output.accept(ModItems.BLIGHT_LOGOS.get());
                output.accept(ModItems.DECAY_LOGOS.get());
                output.accept(ModItems.EARTH_LOGOS.get());
                output.accept(ModItems.ECHO_LOGOS.get());
                output.accept(ModItems.FIRE_LOGOS.get());
                output.accept(ModItems.ICE_LOGOS.get());
                output.accept(ModItems.IMAGINATION_LOGOS.get());
                output.accept(ModItems.LAVA_LOGOS.get());
                output.accept(ModItems.LIGHTNING_LOGOS.get());
                output.accept(ModItems.MOTION_LOGOS.get());
                output.accept(ModItems.NATURE_LOGOS.get());
                output.accept(ModItems.VOID_LOGOS.get());
                output.accept(ModItems.WATER_LOGOS.get());
                output.accept(ModItems.LOGOI_LOGOS.get());

            }).build());
    public static final RegistryObject<CreativeModeTab> ELEMENTAL_BLOCKS_TAB = CREATIVE_MODE_TABS.register("elemental_blocks_tab", () -> CreativeModeTab.builder().icon(()-> new ItemStack(ModBlocks.DIVINITIES_DIVINITY.get()))
            .title(Component.translatable("creativetab.adventurerfantasy.elemental_blocks")).withTabsBefore(ELEMENTAL_INGREDIENTS_TAB.getId())
            .displayItems((itemDisplayParameters, output) -> {
                output.accept(ModBlocks.BLIGHT_DIVINITY.get());
                output.accept(ModBlocks.DECAY_DIVINITY.get());
                output.accept(ModBlocks.EARTH_DIVINITY.get());
                output.accept(ModBlocks.ECHO_DIVINITY.get());
                output.accept(ModBlocks.FIRE_DIVINITY.get());
                output.accept(ModBlocks.ICE_DIVINITY.get());
                output.accept(ModBlocks.IMAGINATION_DIVINITY.get());
                output.accept(ModBlocks.LAVA_DIVINITY.get());
                output.accept(ModBlocks.LIGHTNING_DIVINITY.get());
                output.accept(ModBlocks.MOTION_DIVINITY.get());
                output.accept(ModBlocks.NATURE_DIVINITY.get());
                output.accept(ModBlocks.VOID_DIVINITY.get());
                output.accept(ModBlocks.WATER_DIVINITY.get());
                output.accept(ModBlocks.WIND_DIVINITY.get());
                output.accept(ModBlocks.DIVINITIES_DIVINITY.get());

            }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
