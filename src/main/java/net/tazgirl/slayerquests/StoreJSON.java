package net.tazgirl.slayerquests;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.io.Resources.getResource;

public class StoreJSON
{
    //Bad vibes: Please run only AFTER JSONToConfig()
    public static List<SlayerQuestsLibraryFuncs.Tier> ProcessJSON(ResourceManager resourceManager)
    {


        List<SlayerQuestsLibraryFuncs.Tier> tierListToReturn = new ArrayList<>();

        List<String> currentTierQuests;

        JsonObject CurrentTierJson;
        JsonObject currentQuest;

        JsonObject questsFile = FetchSlayerQuestsJSON();

        JsonObject tiersJSON = questsFile.getAsJsonObject("tiers");

        List<String> tiersList = new ArrayList<>(tiersJSON.keySet());



        for(int i = 0; i < tiersList.size(); i++)
        {
            SlayerQuestsLibraryFuncs.Tier currentTier = new SlayerQuestsLibraryFuncs.Tier();

            currentTier.DoSetName(tiersList.get(i));

            CurrentTierJson = tiersJSON.getAsJsonObject(tiersList.get(i));

            currentTierQuests = new ArrayList<>(CurrentTierJson.keySet());

            currentTier.DoSetQuestNames(currentTierQuests);

            for(int j = 0; j < currentTierQuests.size(); j++)
            {
                currentQuest = CurrentTierJson.getAsJsonObject(currentTierQuests.get(j));


                if(BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(currentQuest.get("mobID").getAsString())))
                {
                    currentTier.DoAddSet(currentTierQuests.get(j),currentQuest.get("mobID").getAsString(),currentQuest.get("questAverage").getAsInt(),currentQuest.get("questSkew").getAsInt(),currentQuest.get("slayerExpPerMob").getAsInt(),currentQuest.get("questMin").getAsInt(),currentQuest.get("questMax").getAsInt(),currentQuest.get("lootRolls").getAsInt(),currentQuest.get("lootOverrideDirectory").getAsString(), resourceManager);
                }
                else
                {
                    throw new RuntimeException(currentQuest.get("mobID").getAsString() + " is not a registered LivingEntity");
                }
            }
            tierListToReturn.add(currentTier);
        }
        return tierListToReturn;
    }

    //Also has bad vibes
    public static List<String> ValidTierNames()
    {
        JsonObject questsFile = FetchSlayerQuestsJSON();
        JsonObject tiersJSON = questsFile.getAsJsonObject("tiers");

        return new ArrayList<>(tiersJSON.keySet());
    }

    public static void ValidateTierLoot(List<String> tierNames, MinecraftServer server)
    {
        List<String> missingTables = new ArrayList<>();

        ResourceKey<LootTable> currentLootTableKey;

        for(String tierName : tierNames)
        {

            //TRY: remove .json, ask discord?
            currentLootTableKey = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse("slayerquests:tier_loot/" + tierName));

            LootTable currentLootTable = server.reloadableRegistries().getLootTable(currentLootTableKey);

            if(currentLootTable == LootTable.EMPTY)
            {
                missingTables.add(tierName);
            }
        }

        if(!missingTables.isEmpty())
        {
            SlayerQuests.LOGGER.error("Could not find a loot_table for the following tiers in SlayerQuest.json:" + missingTables);
            throw new RuntimeException("Could not find a loot_table for the following tiers in SlayerQuest.json:" + missingTables);
        }

    }

    //Confirm file BEFORE running
    public static JsonObject FetchSlayerQuestsJSON()
    {
        Gson gson = new Gson();

        Path filePath = FMLPaths.CONFIGDIR.get().resolve("SlayerQuests.json");

        try (Reader reader = Files.newBufferedReader(filePath)) {
            return gson.fromJson(reader, JsonObject.class);
        }
        catch (IOException error)
        {
            throw new RuntimeException("SlayerQuests.json not found in config directory. Was this void called incorrectly?", error);
        }
    }



}
