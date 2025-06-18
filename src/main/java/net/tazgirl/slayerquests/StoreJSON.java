package net.tazgirl.slayerquests;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.io.Resources.getResource;

public class StoreJSON
{

    public static class Quest
    {
        public String name;
        public String mob;
        public int mean;
        public int skew;
        public int expPerMob;
        public int min;
        public int max;

    }

    public static class QuestTier
    {
        static String name;
        List<Quest> quests = new ArrayList<>();
        List<String> validQuestNames;

        public void AddSet(String questName, String questMob, int questMean, int questSkew, int questExp, int questMin, int questMax)
        {
            Quest newSet = new Quest();
            newSet.name = questName;
            newSet.mob = questMob;
            newSet.mean = questMean;
            newSet.skew = questSkew;
            newSet.expPerMob = questExp;
            newSet.min = questMin;
            newSet.max = questMax;


            quests.add(newSet);
        }

        public void setName(String newName)
        {
            name = newName;
        }

        public void setQuestNames(List<String> newQuestNames)
        {
            validQuestNames = newQuestNames;
        }

    }



    //Bad vibes: Please run only AFTER JSONToConfig()
    public static List<QuestTier> ProcessJSON()
    {


        List<QuestTier> tierListToReturn = new ArrayList<>();

        List<String> currentTierQuests;

        JsonObject CurrentTierJson;
        JsonObject currentQuest;

        JsonObject questsFile = FetchSlayerQuestsJSON();

        JsonObject tiersJSON = questsFile.getAsJsonObject("tiers");

        List<String> tiersList = new ArrayList<>(tiersJSON.keySet());



        for(int i = 0; i < tiersList.size(); i++)
        {
            QuestTier currentTier = new QuestTier();

            currentTier.setName(tiersList.get(i));

            CurrentTierJson = tiersJSON.getAsJsonObject(tiersList.get(i));

            currentTierQuests = new ArrayList<>(CurrentTierJson.keySet());

            currentTier.setQuestNames(currentTierQuests);

            for(int j = 0; j < currentTierQuests.size(); j++)
            {
                currentQuest = CurrentTierJson.getAsJsonObject(currentTierQuests.get(j));

                if(BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(currentQuest.get("mobID").getAsString())))
                {
                    currentTier.AddSet(currentTierQuests.get(j),currentQuest.get("mobID").getAsString(),currentQuest.get("questAverage").getAsInt(),currentQuest.get("questSkew").getAsInt(),currentQuest.get("slayerExpPerMob").getAsInt(),currentQuest.get("questMin").getAsInt(),currentQuest.get("questMax").getAsInt());
                }
                else
                {
                    throw new RuntimeException(currentQuest.get("mobID").getAsString() + " is not a registered entity");
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
        ResourceManager resourceManager = server.getResourceManager();

        List<String> missingTables = new ArrayList<>();

        ResourceLocation currentDirectory;

        for(String tierName : tierNames)
        {
            currentDirectory = ResourceLocation.parse("slayerquests:tier_loot/" + tierName);

            if(resourceManager.getResource(currentDirectory).isEmpty())
            {
                missingTables.add(tierName);
            }
        }

        if(!missingTables.isEmpty())
        {
            String returnString = "Could not find a loot_table for the following tiers in SlayerQuest.json:" + missingTables;
            SlayerQuests.LOGGER.error(returnString);
            throw new RuntimeException(returnString);
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
