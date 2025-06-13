package net.tazgirl.slayerquests;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StoreJSON
{

    public static class MobSet
    {
        String mob;
        int questMean;
        int questSkew;
        int questExp;
        int questMin;
        int questMax;


        public void setMob(String newMob)
        {
            mob = newMob;
        }

        public void setQuestMean(int newQuestMean)
        {
            questMean = newQuestMean;
        }

        public void setQuestSkew(int newQuestSkew)
        {
            questSkew = newQuestSkew;
        }

        public void setQuestExp(int newQuestExp)
        {
            questExp = newQuestExp;
        }

        public void setQuestMin(int newQuestMin)
        {
            questMin = newQuestMin;
        }

        public void setQuestMax(int newQuestMax)
        {
            questMax = newQuestMax;
        }



    }

    public static class QuestTier
    {
        List<MobSet> quests = new ArrayList<>();

        public void AddSet(String mob, int questMean, int questSkew, int questExp, int questMin, int questMax)
        {
            MobSet newSet = new MobSet();
            newSet.setMob(mob);
            newSet.setQuestMean(questMean);
            newSet.setQuestSkew(questSkew);
            newSet.setQuestExp(questExp);
            newSet.setQuestMin(questMin);
            newSet.setQuestMax(questMax);

            quests.add(newSet);
        }

    }

    //Don't trust: Please run only AFTER JSONToConfig()
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

            CurrentTierJson = tiersJSON.getAsJsonObject(tiersList.get(i));

            currentTierQuests = new ArrayList<>(CurrentTierJson.keySet());

            for(int j = 0; j < currentTierQuests.size(); j++)
            {
                currentQuest = CurrentTierJson.getAsJsonObject(currentTierQuests.get(j));

                if(BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(currentQuest.get("mobID").getAsString())))
                {
                    currentTier.AddSet(currentQuest.get("mobID").getAsString(),currentQuest.get("questAverage").getAsInt(),currentQuest.get("questSkew").getAsInt(),currentQuest.get("questExp").getAsInt(),currentQuest.get("questMin").getAsInt(),currentQuest.get("questMax").getAsInt());
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
