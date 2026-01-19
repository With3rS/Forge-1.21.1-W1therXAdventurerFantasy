package com.w1therx.adventurerfantasy.item;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.block.ModBlocksWithFireResistantItem;
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
    public static final RegistryObject<CreativeModeTab> ELEMENTAL_BLOCKS_TAB = CREATIVE_MODE_TABS.register("elemental_blocks_tab", () -> CreativeModeTab.builder().icon(()-> new ItemStack(ModBlocksWithFireResistantItem.DIVINITIES_DIVINITY.get()))
            .title(Component.translatable("creativetab.adventurerfantasy.elemental_blocks")).withTabsBefore(ELEMENTAL_INGREDIENTS_TAB.getId())
            .displayItems((itemDisplayParameters, output) -> {
                output.accept(ModBlocksWithFireResistantItem.BLIGHT_DIVINITY.get());
                output.accept(ModBlocksWithFireResistantItem.DECAY_DIVINITY.get());
                output.accept(ModBlocksWithFireResistantItem.EARTH_DIVINITY.get());
                output.accept(ModBlocksWithFireResistantItem.ECHO_DIVINITY.get());
                output.accept(ModBlocksWithFireResistantItem.FIRE_DIVINITY.get());
                output.accept(ModBlocksWithFireResistantItem.ICE_DIVINITY.get());
                output.accept(ModBlocksWithFireResistantItem.IMAGINATION_DIVINITY.get());
                output.accept(ModBlocksWithFireResistantItem.LAVA_DIVINITY.get());
                output.accept(ModBlocksWithFireResistantItem.LIGHTNING_DIVINITY.get());
                output.accept(ModBlocksWithFireResistantItem.MOTION_DIVINITY.get());
                output.accept(ModBlocksWithFireResistantItem.NATURE_DIVINITY.get());
                output.accept(ModBlocksWithFireResistantItem.VOID_DIVINITY.get());
                output.accept(ModBlocksWithFireResistantItem.WATER_DIVINITY.get());
                output.accept(ModBlocksWithFireResistantItem.WIND_DIVINITY.get());
                output.accept(ModBlocksWithFireResistantItem.DIVINITIES_DIVINITY.get());

                output.accept(ModBlocksWithFireResistantItem.BLIGHT_PRINCIPLE.get());
                output.accept(ModBlocksWithFireResistantItem.DECAY_PRINCIPLE.get());
                output.accept(ModBlocksWithFireResistantItem.EARTH_PRINCIPLE.get());
                output.accept(ModBlocksWithFireResistantItem.ECHO_PRINCIPLE.get());
                output.accept(ModBlocksWithFireResistantItem.FIRE_PRINCIPLE.get());
                output.accept(ModBlocksWithFireResistantItem.ICE_PRINCIPLE.get());
                output.accept(ModBlocksWithFireResistantItem.IMAGINATION_PRINCIPLE.get());
                output.accept(ModBlocksWithFireResistantItem.LAVA_PRINCIPLE.get());
                output.accept(ModBlocksWithFireResistantItem.LIGHTNING_PRINCIPLE.get());
                output.accept(ModBlocksWithFireResistantItem.MOTION_PRINCIPLE.get());
                output.accept(ModBlocksWithFireResistantItem.NATURE_PRINCIPLE.get());
                output.accept(ModBlocksWithFireResistantItem.VOID_PRINCIPLE.get());
                output.accept(ModBlocksWithFireResistantItem.WATER_PRINCIPLE.get());
                output.accept(ModBlocksWithFireResistantItem.WIND_PRINCIPLE.get());
                output.accept(ModBlocksWithFireResistantItem.PRINCIPLES_PRINCIPLE.get());

            }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
