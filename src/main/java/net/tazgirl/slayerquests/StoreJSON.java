package net.tazgirl.slayerquests;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootTable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.io.Resources.getResource;

public class StoreJSON
{
    //TODO: Definite optimization to be found, probably in the form of codecs
    public static List<SlayerQuestsLibraryFuncs.Tier> ProcessJSON(ResourceManager resourceManager, MinecraftServer server)
    {

        List<SlayerQuestsLibraryFuncs.Tier> tierListToReturn = new ArrayList<>();

        List<String> currentTierQuests;

        JsonObject CurrentTierJson;
        JsonObject currentQuest;

        JsonObject questsFile = FetchSlayerQuestsJSON(resourceManager);

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
                    currentTier.DoAddQuest(currentTierQuests.get(j),currentQuest.get("mobID").getAsString(),currentQuest.get("questAverage").getAsInt(),currentQuest.get("questSkew").getAsInt(),currentQuest.get("slayerExpPerMob").getAsFloat(),currentQuest.get("questMin").getAsInt(),currentQuest.get("questMax").getAsInt(),currentQuest.get("lootRolls").getAsInt(),currentQuest.get("lootOverrideDirectory").getAsString(), server);
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
    public static List<String> ValidTierNames(ResourceManager resourceManager)
    {
        JsonObject questsFile = FetchSlayerQuestsJSON(resourceManager);
        JsonObject tiersJSON = questsFile.getAsJsonObject("tiers");

        return new ArrayList<>(tiersJSON.keySet());
    }

    public static void ValidateTierLoot(List<String> tierNames, MinecraftServer server)
    {
        List<String> missingTables = new ArrayList<>();

        ResourceKey<LootTable> currentLootTableKey;

        for(String tierName : tierNames)
        {
            if(!VerifyLootTable("slayerquests:tier_loot/" + tierName,server))
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

    //Returns true if a loot table is found at location
    public static boolean VerifyLootTable(String location, MinecraftServer server)
    {
        ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(location));

        LootTable currentLootTable = server.reloadableRegistries().getLootTable(lootTableKey);

        return currentLootTable != LootTable.EMPTY;
    }


    //TODO: Replace with JsonOps?
    public static JsonObject FetchSlayerQuestsJSON(ResourceManager resourceManager)
    {
        ResourceLocation filePath = ResourceLocation.parse("slayerquests:slayerquests.json");

        Optional<Resource> fileResource = resourceManager.getResource(filePath);

        if(fileResource.isEmpty())
        {
            throw new RuntimeException("SlayerQuests.json could not be found in server resources");
        }

        try (Reader reader = new InputStreamReader(fileResource.get().open()))
        {
            Gson gson = new Gson();
            return gson.fromJson(reader, JsonObject.class);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read SlayerQuests.json", e);
        }
    }

    static void SetTierThresholds()
    {
        if(SlayerQuests.tiers == null){return;}
        if(SlayerQuests.tiers.isEmpty()){return;}

        List<Integer> tierLevelThresholds = Config.tierLevelThresholds;
        SlayerQuestsLibraryFuncs.Tier currentTier;

        for(int i = 0; i < SlayerQuests.tiers.size(); i++)
        {
            currentTier = SlayerQuests.tiers.get(i);

            if(!(tierLevelThresholds.size() < i + 1))
            {
                currentTier.requiredSlayerLevel = tierLevelThresholds.get(i);
            }
            else
            {
                currentTier.requiredSlayerLevel = tierLevelThresholds.getLast();
            }
        }
    }



}
