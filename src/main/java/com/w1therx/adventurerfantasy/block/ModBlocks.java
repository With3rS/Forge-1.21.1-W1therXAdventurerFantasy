package com.w1therx.adventurerfantasy.block;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.item.ModItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;


public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AdventurerFantasy.MOD_ID);

    public static final RegistryObject<Block> BLIGHT_DIVINITY = registerBlock("blight_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 10)
    ));
    public static final RegistryObject<Block> DECAY_DIVINITY = registerBlock("decay_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 10)
    ));
    public static final RegistryObject<Block> EARTH_DIVINITY = registerBlock("earth_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 10)
    ));
    public static final RegistryObject<Block> ECHO_DIVINITY = registerBlock("echo_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 10)
    ));
    public static final RegistryObject<Block> FIRE_DIVINITY = registerBlock("fire_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 10)
    ));
    public static final RegistryObject<Block> ICE_DIVINITY = registerBlock("ice_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 10)
    ));
    public static final RegistryObject<Block> IMAGINATION_DIVINITY = registerBlock("imagination_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 10)
    ));
    public static final RegistryObject<Block> LAVA_DIVINITY = registerBlock("lava_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 10)
    ));
    public static final RegistryObject<Block> LIGHTNING_DIVINITY = registerBlock("lightning_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 10)
    ));
    public static final RegistryObject<Block> MOTION_DIVINITY = registerBlock("motion_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 10)
    ));
    public static final RegistryObject<Block> NATURE_DIVINITY = registerBlock("nature_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 10)
    ));
    public static final RegistryObject<Block> VOID_DIVINITY = registerBlock("void_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 10)
    ));
    public static final RegistryObject<Block> WATER_DIVINITY = registerBlock("water_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 10)
    ));
    public static final RegistryObject<Block> WIND_DIVINITY = registerBlock("wind_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 10)
    ));
    public static final RegistryObject<Block> DIVINITIES_DIVINITY = registerBlock("divinities_divinity", ()-> new Block(BlockBehaviour.Properties.of()
            .strength(3F, 1000F).sound(SoundType.AMETHYST).lightLevel(state -> 15)
    ));



    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().fireResistant()) {
            @Override
            public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
                super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                if (block == BLIGHT_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.blight_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
            }
                if (block == DECAY_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.decay_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                }
                if (block == EARTH_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.earth_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                }
                if (block == ECHO_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.echo_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                }
                if (block == FIRE_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.fire_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                }
                if (block == ICE_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.ice_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                }
                if (block == IMAGINATION_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.imagination_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                }
                if (block == LAVA_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.lava_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                }
                if (block == LIGHTNING_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.lightning_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                }
                if (block == MOTION_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.motion_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                }
                if (block == NATURE_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.nature_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                }
                if (block == VOID_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.void_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                }
                if (block == WATER_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.water_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                }
                if (block == WIND_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.wind_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                }
                if (block == DIVINITIES_DIVINITY) { if (Screen.hasShiftDown()) {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.divinities_divinity_lore"));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
                }
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                }

            }
    });
    };

    public static void register(IEventBus eventbus) {
        BLOCKS.register(eventbus);
    }
}
