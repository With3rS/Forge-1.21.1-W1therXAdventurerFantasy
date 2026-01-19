package com.w1therx.adventurerfantasy.event.custom;

import com.w1therx.adventurerfantasy.screen.inventory.effects.EffectPanelScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)

public class ClientInventoryOverride {
    @SubscribeEvent
    public static void onOpenScreen(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof InventoryScreen inv && !(inv instanceof EffectPanelScreen)) {
            event.setNewScreen(new EffectPanelScreen(Minecraft.getInstance().player));
        }
    }
}
