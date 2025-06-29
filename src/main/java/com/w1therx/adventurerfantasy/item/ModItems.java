package com.w1therx.adventurerfantasy.item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

import static com.w1therx.adventurerfantasy.AdventurerFantasy.MOD_ID;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Item> BLIGHT_ESSENCE = ITEMS.register("blight_essence", () -> new Item(new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
            if (Screen.hasShiftDown()) {
                pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.blight_essence_lore"));
            } else {
                pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
            }
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        }
    });
    public static final RegistryObject<Item> DECAY_ESSENCE = ITEMS.register("decay_essence", () -> new Item(new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        if (Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.decay_essence_lore"));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
        }
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
});
    public static final RegistryObject<Item> EARTH_ESSENCE = ITEMS.register("earth_essence", () -> new Item(new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        if (Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.earth_essence_lore"));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
        }
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
});
    public static final RegistryObject<Item> ECHO_ESSENCE = ITEMS.register("echo_essence", () -> new Item(new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        if (Screen.hasShiftDown()) {
        pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.echo_essence_lore"));
    } else {
        pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
    }
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
});
    public static final RegistryObject<Item> FIRE_ESSENCE = ITEMS.register("fire_essence", () -> new Item(new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        if (Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.fire_essence_lore"));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
        }
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
});
    public static final RegistryObject<Item> ICE_ESSENCE = ITEMS.register("ice_essence", () -> new Item(new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        if (Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.ice_essence_lore"));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
        }
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
});
    public static final RegistryObject<Item> IMAGINATION_ESSENCE = ITEMS.register("imagination_essence", () -> new Item(new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        if (Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.imagination_essence_lore"));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
        }
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
});
    public static final RegistryObject<Item> LAVA_ESSENCE = ITEMS.register("lava_essence", () -> new Item(new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        if (Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.lava_essence_lore"));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
        }
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
});
    public static final RegistryObject<Item> LIGHTNING_ESSENCE = ITEMS.register("lightning_essence", () -> new Item(new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        if (Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.lightning_essence_lore"));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
        }
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
});
    public static final RegistryObject<Item> MOTION_ESSENCE = ITEMS.register("motion_essence", () -> new Item(new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        if (Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.motion_essence_lore"));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
        }
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
});
    public static final RegistryObject<Item> NATURE_ESSENCE = ITEMS.register("nature_essence", () -> new Item(new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        if (Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.nature_essence_lore"));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
        }
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
});
    public static final RegistryObject<Item> VOID_ESSENCE = ITEMS.register("void_essence", () -> new Item(new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        if (Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.void_essence_lore"));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
        }
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
});
    public static final RegistryObject<Item> WATER_ESSENCE = ITEMS.register("water_essence", () -> new Item(new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        if (Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.water_essence_lore"));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
        }
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
});
    public static final RegistryObject<Item> WIND_ESSENCE = ITEMS.register("wind_essence", () -> new Item(new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        if (Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.wind_essence_lore"));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.adventurerfantasy.item_info"));
        }
            super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
});

    public static final RegistryObject<Item> BLIGHT_LOGOS = ITEMS.register("blight_logos", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DECAY_LOGOS = ITEMS.register("decay_logos", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> EARTH_LOGOS = ITEMS.register("earth_logos", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ECHO_LOGOS = ITEMS.register("echo_logos", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FIRE_LOGOS = ITEMS.register("fire_logos", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ICE_LOGOS = ITEMS.register("ice_logos", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IMAGINATION_LOGOS = ITEMS.register("imagination_logos", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LAVA_LOGOS = ITEMS.register("lava_logos", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LIGHTNING_LOGOS = ITEMS.register("lightning_logos", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MOTION_LOGOS = ITEMS.register("motion_logos", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> NATURE_LOGOS = ITEMS.register("nature_logos", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> VOID_LOGOS = ITEMS.register("void_logos", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WATER_LOGOS = ITEMS.register("water_logos", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WIND_LOGOS = ITEMS.register("wind_logos", () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}