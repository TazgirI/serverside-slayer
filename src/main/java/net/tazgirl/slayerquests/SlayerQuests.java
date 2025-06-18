package net.tazgirl.slayerquests;

import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SlayerQuests.MODID)
public class SlayerQuests
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "slayerquests";
    // Directly reference a slf4j logger
    static final Logger LOGGER = LogUtils.getLogger();

    static List<StoreJSON.QuestTier> tiers;
    static List<String> validTiers;

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public SlayerQuests(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::Setup);

        DataAttachment.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void Setup(FMLCommonSetupEvent event)
    {
        if(Config.copyOverJSON)
        {
            JSONToConfig();
        }
        tiers = StoreJSON.ProcessJSON();
        validTiers = StoreJSON.ValidTierNames();

    }

    private void WorldLoad(ServerStartedEvent event)
    {

    }



    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        if(Config.validateLootTables)
        {
            StoreJSON.ValidateTierLoot(validTiers, event.getServer());
        }
    }

    private void JSONToConfig()
    {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path targetFile = configDir.resolve("SlayerQuests.json");

        if (Files.notExists(targetFile))
        {
            try (InputStream in = SlayerQuests.class.getResourceAsStream("/data/slayerquests/SlayerQuests.json"))
            {
                if(in != null)
                {
                    Files.copy(in, targetFile);
                }
                else
                {
                    LOGGER.error("No SlayerQuests.json could be found");
                    throw new RuntimeException("Missing SlayerQuests.json in '/data/slayerquests/SlayerQuests.json' or config directory");
                }

            }
            catch (IOException error) {
                LOGGER.error("SlayerQuests.json generation failed", error);
                throw new RuntimeException("Could not generate SlayerQuests.json", error);
            }
        }

    }

}
