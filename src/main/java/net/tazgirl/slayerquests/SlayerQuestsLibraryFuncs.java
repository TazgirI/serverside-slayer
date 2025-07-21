package net.tazgirl.slayerquests;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class SlayerQuestsLibraryFuncs
{


    //==================
    //   Data Classes
    //==================

    public static class Quest
    {
        public String name;
        public String tier;
        public String mob;
        public int mean;
        public int skew;
        public float expPerMob;
        public int min;
        public int max;

        public int lootRolls;
        public ResourceLocation lootLocation;

        public int CalcQuestCap()
        {
            int maxPasses = Config.bellCurveMaxPasses;

            java.util.Random random = new java.util.Random();
            double value;

            for(int i = 0; i < maxPasses; i++)
            {
                value = mean + random.nextGaussian() * skew;
                if(value >= min || value <= max)
                {
                    return Math.round((float) value);
                }
            }

            return mean;
        }

        public void SetLootDirectory(String lootOverride, MinecraftServer server)
        {
            ResourceLocation tempLootLocation;

            if (Objects.equals(lootOverride, ""))
            {
                tempLootLocation = ResourceLocation.parse("slayerquests:tier_loot/" + tier);
            }
            else
            {
                if(!StoreJSON.VerifyLootTable(lootOverride,server))
                {
                    SlayerQuests.LOGGER.error("lootOverrideDirectory of \"" + lootOverride + "\", cannot be found or does not exist, reverting to default");
                    tempLootLocation = ResourceLocation.parse("slayerquests:loot_tables/tier_loot/" + tier);
                }
                tempLootLocation = ResourceLocation.tryParse(lootOverride);
            }

            lootLocation = tempLootLocation;
        }

        public void GrantToPlayer(Player player)
        {
            DoSetPlayerQuest(player, this);
        }
    }

    public static class Tier
    {
        public String name;
        List<Quest> quests = new ArrayList<>();
        List<String> validQuestNames;

        public void DoAddSet(String questName, String questMob, int questMean, int questSkew, float questExp, int questMin, int questMax, int questLootRolls, String questLootOverrideDirectory, MinecraftServer server)
        {
            Quest newSet = new Quest();
            newSet.name = questName;
            newSet.mob = questMob;
            newSet.mean = questMean;
            newSet.skew = questSkew;
            newSet.expPerMob = questExp;
            newSet.min = questMin;
            newSet.max = questMax;
            newSet.tier = name;
            newSet.lootRolls = questLootRolls;

            newSet.SetLootDirectory(questLootOverrideDirectory, server);

            quests.add(newSet);

        }

        protected void DoSetName(String newName)
        {
            name = newName;
        }

        public void DoSetQuestNames(List<String> newQuestNames)
        {
            validQuestNames = newQuestNames;
        }

        public List<String> GetQuestNamesInTier()
        {
            return validQuestNames;
        }

        public List<Quest> GetQuestObjectsInTier()
        {
            return quests;
        }

        public boolean DoRemoveQuest(String questName)
        {
            int questIndex = validQuestNames.indexOf(questName);

            if(questIndex != -1 && quests.size() != 1)
            {
                quests.remove(questIndex);
                validQuestNames.remove(questIndex);

                return true;
            }

            return false;
        }

        public boolean DoRemoveQuest(Quest questToRemove)
        {
            if(quests.contains(questToRemove) && quests.size() != 1)
            {
                quests.remove(questToRemove);
                validQuestNames.remove(questToRemove.name);

                return true;
            }

            return false;
        }

        public String GetRandomQuestName()
        {
            return quests.get(new Random().nextInt(0,quests.size())).name;
        }

        public Quest GetRandomQuestObject()
        {
            return quests.get(new Random().nextInt(0,quests.size()));
        }

        public Quest GetQuestObjectFromName(String questName)
        {
            if (validQuestNames.contains(questName))
            {
                return quests.get(validQuestNames.indexOf(questName));
            }

            return null;
        }

    }


    //========================
    //   Quest Manipulation
    //========================

    public static void DoSetPlayerQuest(Player player, Quest quest)
    {
        player.setData(DataAttachment.CURRENT_QUEST.get(), new DataAttachment.currentQuestRecord(quest.mob, 0, quest.CalcQuestCap(), quest.expPerMob, quest.name, quest.tier));
    }


    public static void DoSetPlayerQuest(Player player, String tierName, String questName)
    {
        if(GetDoesQuestExist(tierName, questName))
        {
            GetQuestObjectFromName(tierName, questName).GrantToPlayer(player);
        }
    }

    public static String GetQuestState(Player player)
    {
        DataAttachment.currentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST);
        if(!Objects.equals(playerQuest.mob(), ""))
        {
            if(playerQuest.questCurrent()>= playerQuest.questCap())
            {
                return "fulfilled";
            }
            return "unfulfilled";
        }
        else
        {
            return "unassigned";
        }
    }

    public static String GetQuestState(DataAttachment.currentQuestRecord playerQuest)
    {
        if(!Objects.equals(playerQuest.mob(), ""))
        {
            if(playerQuest.questCurrent()>= playerQuest.questCap())
            {
                return "fulfilled";
            }
            return "unfulfilled";
        }
        else
        {
            return "unassigned";
        }
    }

    public static boolean DoAttemptRewardQuest(ServerPlayer player, Boolean doRemoveQuest)
    {
        DataAttachment.currentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST);

        if (GetIsQuestComplete(playerQuest))
        {
            DataAttachment.slayerExperienceRecord playerExp = player.getData(DataAttachment.SLAYER_EXPERIENCE);
            float newExp = playerExp.exp() + (playerQuest.slayerExpPerMob() * playerQuest.questCap());
            player.setData(DataAttachment.SLAYER_EXPERIENCE.get(), new DataAttachment.slayerExperienceRecord(newExp,CalcExpToLevel(newExp)));

            DoDropPlayerQuestLoot(player,GetQuestLootRoll(GetQuestObjectFromPlayer(player),player.getServer()));

            if(doRemoveQuest)
            {
               DoRemoveQuest(player, false);
            }

            return true;
        }

        return false;
    }

    public static void DoForceRewardQuest(Player player, Boolean doRemoveQuest)
    {
        DataAttachment.currentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST);

        DataAttachment.slayerExperienceRecord playerExp = player.getData(DataAttachment.SLAYER_EXPERIENCE);
        float newExp = playerExp.exp() + (playerQuest.slayerExpPerMob() * playerQuest.questCap());
        player.setData(DataAttachment.SLAYER_EXPERIENCE.get(), new DataAttachment.slayerExperienceRecord(newExp,CalcExpToLevel(newExp)));

        if(doRemoveQuest)
        {
            ClearQuest(player);
        }
    }

    public static void DoRemoveQuest(Player player, Boolean doCheckIfQuestCompleted)
    {
        if(doCheckIfQuestCompleted)
        {
            if(GetIsQuestComplete(player.getData(DataAttachment.CURRENT_QUEST)))
            {
                ClearQuest(player);
            }
        }
        else
        {
            ClearQuest(player);
        }

    }

    public static Boolean GetIsQuestComplete(DataAttachment.currentQuestRecord playerQuest)
    {
        return !Objects.equals(playerQuest.mob(), "") && playerQuest.questCurrent() >= playerQuest.questCap();
    }

    public static DataAttachment.currentQuestRecord GetPlayersQuestAsRecord(Player player)
    {
        return player.getData(DataAttachment.CURRENT_QUEST.get());
    }

    public static List<ItemStack> GetQuestLootRoll(Quest quest, MinecraftServer server)
    {
        LootTable questLootTable = GetQuestLootTable(quest, server);

        List<ItemStack> itemstacksToReturn = new ArrayList<>();

        LootParams.Builder lootParamsBuilder = new LootParams.Builder(server.overworld());
        LootParams lootParams = lootParamsBuilder.create(LootContextParamSet.builder().build());

        for (int i = 0; i < quest.lootRolls; i++)
        {
            List<ItemStack> tempStack = questLootTable.getRandomItems(lootParams);

            itemstacksToReturn.addAll(tempStack);
        }

        return itemstacksToReturn;
    }

    public static LootTable GetQuestLootTable(Quest quest, MinecraftServer server)
    {
        return server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,quest.lootLocation));
    }

    public static void DoDropPlayerQuestLoot(ServerPlayer player, List<ItemStack> questLoot)
    {
        ServerLevel level = player.serverLevel();

        Vec3 dropPos = player.position().add(0,2,0);

        for (ItemStack itemStack : questLoot)
        {
            ItemEntity droppedItem = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, itemStack.copy());
            droppedItem.setPickUpDelay(40);
            droppedItem.setTarget(player.getUUID());
            level.addFreshEntity(droppedItem);
        }

    }

    private static void ClearQuest(Player player)
    {
        player.setData(DataAttachment.CURRENT_QUEST, new DataAttachment.currentQuestRecord("",0,0,0,"",""));
    }



    //====================
    //   Calculate Data
    //====================

    public static int GetPlayerLevel(Player player)
    {
        return player.getData(DataAttachment.SLAYER_EXPERIENCE.get()).level();
    }

    //TODO: Adjust level formulae and add config
    public static int CalcExpToLevel(float exp)
    {
        for(int i = 0; i < 100; i++)
        {
            if(exp < SlayerQuests.levelBoundries.get(i))
            {
                return i + 1;
            }
        }
        return 100;
    }

    public static int CalcLevelToExpTotal(int level)
    {
        int total = 0;
        for(int i = 0; i < level; i++)
        {
            total += CalcLevelToExp(i + 1);
        }

        return total;
    }

    public static int CalcLevelToExp(int level)
    {
        level += 3;
        return (int) Math.round(((Math.pow(level, 1.5) + 2.4 * level) * level / 50) + (Math.min(level, 7) * 3));
    }



    public static int CalcExpToNextLevel(int currentLevel, int exp)
    {
        return CalcLevelToExp(currentLevel + 1) - (exp - CalcLevelToExp(currentLevel));
    }

    // Use if you don't have a specific Quest object, if you do then use Quest.GenerateQuestCap()//
    public static int CalcQuestCap(int average, int skew, int min, int max)
    {
        Quest tempQuest = new Quest();
        tempQuest.mean = average;
        tempQuest.skew = skew;
        tempQuest.min = min;
        tempQuest.max = max;
        return tempQuest.CalcQuestCap();

    }

    public static boolean CalcTypeMatchesQuest(Player player, LivingEntity entity)
    {
        return Objects.equals(player.getData(DataAttachment.CURRENT_QUEST.get()).mob(), BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
    }

    //=================
    //   Manage Data
    //=================

    public static DataAttachment.questHolderRecord GetStoredQuestHolderAsRecord(LivingEntity entity)
    {
        return entity.getData(DataAttachment.QUEST_HOLDER);
    }

    public static Quest GetStoredQuestHolderAsObject(LivingEntity entity)
    {
        DataAttachment.questHolderRecord qtgRecord = entity.getData(DataAttachment.QUEST_HOLDER);
        Tier tier = SlayerQuests.tiers.get(SlayerQuests.validTiers.indexOf(qtgRecord.tierName()));

        return tier.GetQuestObjectsInTier().get(tier.GetQuestNamesInTier().indexOf(qtgRecord.questName()));
    }

    public static void DoRefreshStoredQuestHolderTime(LivingEntity entity)
    {
        DataAttachment.questHolderRecord qtrRecord = entity.getData(DataAttachment.QUEST_HOLDER);
        DoSetStoredQuestHolder(entity, qtrRecord.tierName(), qtrRecord.questName(), entity.level().getGameTime(), qtrRecord.sourceUuid());
    }

    public static void DoClearStoredQuestHolder(LivingEntity entity)
    {
        entity.setData(DataAttachment.QUEST_HOLDER.get(), new DataAttachment.questHolderRecord("","", null,0));
    }

    public static void DoSetStoredQuestHolder(LivingEntity entity, String tierNameToStore, String questNameToStore, Long timeWhenStored, int sourceUuid)
    {
        entity.setData(DataAttachment.QUEST_HOLDER.get(), new DataAttachment.questHolderRecord(tierNameToStore, questNameToStore, timeWhenStored, sourceUuid));
    }

    public static void DoSetStoredQuestHolder(Entity entity, DataAttachment.questHolderRecord questHolderRecord)
    {
        entity.setData(DataAttachment.QUEST_HOLDER.get(), questHolderRecord);
    }


    public static void DoSetMobBonus(LivingEntity entity, int bonus)
    {
        entity.setData(DataAttachment.MOB_BONUS.get(), new DataAttachment.mobBonusRecord(bonus));
    }

    public static int GetMobBonus(LivingEntity enity)
    {
        return enity.getData(DataAttachment.MOB_BONUS.get()).bonusAmount();
    }

    public static Tier GetTierObjectFromName(String tierName)
    {
        if(GetDoesTierExist(tierName))
        {
            return SlayerQuests.tiers.get(SlayerQuests.validTiers.indexOf(tierName));
        }
        return null;
    }



    public static Quest GetQuestObjectFromName(String tierName, String questName)
    {
        if(GetDoesQuestExist(tierName,questName))
        {
            return GetTierObjectFromName(tierName).GetQuestObjectFromName(questName);
        }
        return null;
    }

    public static Quest GetQuestObjectFromPlayer(Player player)
    {
        DataAttachment.currentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST.get());
        return GetQuestObjectFromName(playerQuest.questTier(), playerQuest.questName());
    }

    public static boolean GetDoesQuestExist(String tierName, String questName)
    {
        if(SlayerQuests.validTiers.contains(tierName))
        {
            return SlayerQuests.tiers.get(SlayerQuests.validTiers.indexOf(tierName)).GetQuestNamesInTier().contains(questName);
        }

        return false;
    }

    public static boolean GetDoesTierExist(String tierName)
    {
        return SlayerQuests.validTiers.contains(tierName);
    }

    public static String GetMobPlaintext(String mobFull)
    {
        ResourceLocation entityLocation = ResourceLocation.parse(mobFull);
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityLocation);


        return entityType.getDescription().getString();

    }

    //==================
    //   Fetch Random
    //==================


    public static String GetRandomTierName()
    {
        return SlayerQuests.validTiers.get(new Random().nextInt(0,SlayerQuests.validTiers.size()));
    }

    public static Tier GetRandomTierObject()
    {
        return SlayerQuests.tiers.get(new Random().nextInt(0,SlayerQuests.tiers.size()));
    }

    // Use if you don't have a specific Tier object, if you do then use Tier.RandomQuestName()
    public static String GetRandomQuestNameFromTier(String tier)
    {
        List<Tier> tiersList = SlayerQuests.tiers;
        List<String> validTiers = SlayerQuests.validTiers;

        if(validTiers.contains(tier))
        {
            Tier tierObject = tiersList.get(validTiers.indexOf(tier));
            List<Quest> questList = tierObject.quests;

            Random random = new Random();

            return questList.get(random.nextInt(0,questList.size())).name;
        }

        return "";

    }

    // Use if you don't have a specific Tier object, if you do then use Tier.RandomQuestObject()
    public static Quest GetRandomQuestObjectFromTier(String tier)
    {
        List<Tier> tiersList = SlayerQuests.tiers;
        List<String> validTiers = SlayerQuests.validTiers;

        if(validTiers.contains(tier))
        {
            Tier tierObject = tiersList.get(validTiers.indexOf(tier));
            List<Quest> questList = tierObject.quests;

            return questList.get(new Random().nextInt(0,questList.size()));
        }

        return null;

    }


    //=================
    //   Fetch Lists
    //=================


    public static List<String> GetTierNamesList()
    {
        return SlayerQuests.validTiers;
    }

    public static List<Tier> GetTierObjectsList()
    {
        return SlayerQuests.tiers;
    }

    // Use if you don't have a specific Tier object, if you do then use Tier.QuestNamesInTier()
    public static List<String> GetQuestNamesInTier(String tierName)
    {
        int tierIndex = SlayerQuests.validTiers.indexOf(tierName);

        if(tierIndex != -1)
        {
            return SlayerQuests.tiers.get(tierIndex).validQuestNames;
        }

        return new ArrayList<>();
    }

    // Use if you don't have a specific Tier object, if you do then use Tier.QuestObjectsInTier()
    public static List<Quest> GetQuestObjectsInTier(String tier)
    {
        int tierIndex = SlayerQuests.validTiers.indexOf(tier);

        if(tierIndex != -1)
        {
            return SlayerQuests.tiers.get(tierIndex).quests;
        }

        return new ArrayList<>();
    }


    //=======================
    //   File Manipulation
    //=======================


    //fileTOCopyDir should be something like "slayerquests:SlayerQuests.json"
    public static boolean DoJSONToConfig(String locationToCopyDir, MinecraftServer server, Boolean checkForExisting) throws IOException
    {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path outputFile = configDir.resolve("SlayerQuests.json");

        if(!Files.notExists(outputFile) && checkForExisting)
        {
            return true;
        }

        ResourceManager resourceManager = server.getResourceManager();

        ResourceLocation locationToCopy = ResourceLocation.parse(locationToCopyDir);

        if(resourceManager.getResource(locationToCopy).isPresent())
        {
            Resource fileToCopy = resourceManager.getResource(locationToCopy).get();
            try(InputStream inputFile = fileToCopy.open())
            {
                Files.copy(inputFile,outputFile, StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
            catch (IOException exception)
            {
                throw new RuntimeException("Failed to copy \"" + locationToCopyDir + "\" to the ConfigDir");
            }
        }

        return false;


    }
}
