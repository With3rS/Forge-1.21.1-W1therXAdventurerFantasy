package com.w1therx.adventurerfantasy.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;


@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)

public class ModKeyBindings {
    public static final KeyMapping DASH_KEY = new KeyMapping("key.adventurerfantasy.dash", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.movement");

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(DASH_KEY);

    }
}
