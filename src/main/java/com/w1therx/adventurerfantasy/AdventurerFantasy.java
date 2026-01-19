package com.w1therx.adventurerfantasy;

import com.mojang.logging.LogUtils;
import com.w1therx.adventurerfantasy.block.ModBlocksWithFireResistantItem;
import com.w1therx.adventurerfantasy.block.ModBlocks;
import com.w1therx.adventurerfantasy.capability.*;
import com.w1therx.adventurerfantasy.commands.ModGameRules;
import com.w1therx.adventurerfantasy.effect.general.ModEffects;
import com.w1therx.adventurerfantasy.item.ModCreativeModeTabs;
import com.w1therx.adventurerfantasy.item.ModItems;
import com.w1therx.adventurerfantasy.network.ModNetworking;
import com.w1therx.adventurerfantasy.particle.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AdventurerFantasy.MOD_ID)
public class AdventurerFantasy
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "adventurerfantasy";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();


    public AdventurerFantasy(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);

        ModBlocks.register(modEventBus);
        ModBlocksWithFireResistantItem.register(modEventBus);

        ModSounds.SOUND_EVENTS.register(modEventBus);

        ModEffects.register(modEventBus);

        ModGameRules.register();

        ModParticles.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);



    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        System.out.println("[DEBUG] Registering ModCapabilities Class");
        ModCapabilities.register();

        ModNetworking.register();

     }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
     }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }





    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
        }

        @SubscribeEvent
        public static void registerParticleProvider (RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.CONTAMINATED_PARTICLES.get(), ContaminatedParticles.Provider::new);
            event.registerSpriteSet(ModParticles.CRYSTALLIZED_PARTICLES.get(), CrystallizedParticles.Provider::new);
            event.registerSpriteSet(ModParticles.ECSTATIC_PARTICLES.get(), EcstaticParticles.Provider::new);
            event.registerSpriteSet(ModParticles.ELECTRIFIED_PARTICLES.get(), ElectrifiedParticles.Provider::new);

        }
    }
}