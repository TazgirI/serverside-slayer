package net.tazgirl.slayerquests;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
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
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
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
import java.util.ArrayList;
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
    static List<Integer> levelBoundries;

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
        tiers = StoreJSON.ProcessJSON(event.getServer().getResourceManager(),event.getServer());
        validTiers = StoreJSON.ValidTierNames(event.getServer().getResourceManager());
        levelBoundries = CalcLevelBoundriesList();

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

    private static List<Integer> CalcLevelBoundriesList()
    {
        List<Integer> listToReturn = new ArrayList<>();

        for(int i = 1; i < 101; i++)
        {
            listToReturn.add(SlayerQuestsLibraryFuncs.CalcLevelToExpTotal(i));
        }

        return listToReturn;
    }
}


