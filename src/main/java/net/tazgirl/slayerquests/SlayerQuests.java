package net.tazgirl.slayerquests;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SlayerQuests.MODID)
@EventBusSubscriber(modid = SlayerQuests.MODID, bus = EventBusSubscriber.Bus.GAME)
public class SlayerQuests
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "slayerquests";
    // Directly reference a slf4j logger
    static final Logger LOGGER = LogUtils.getLogger();

    static List<SlayerQuestsLibraryFuncs.Tier> tiers;
    static List<String> validTiers;

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public SlayerQuests(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading

        DataAttachment.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.



        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    @SubscribeEvent
    private static void Setup(ServerAboutToStartEvent event) throws IOException
    {

        if(Config.copyOverJSON)
        {
            SlayerQuestsLibraryFuncs.DoJSONToConfig("slayerquests:SlayerQuests.json",event.getServer(),true);
        }

        tiers = StoreJSON.ProcessJSON(event.getServer().getResourceManager());
        validTiers = StoreJSON.ValidTierNames();

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    private static void onServerStarting(ServerStartingEvent event)
    {

        if (Config.validateLootTables)
        {
            StoreJSON.ValidateTierLoot(validTiers, event.getServer());
        }
    }
}


