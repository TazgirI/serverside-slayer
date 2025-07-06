package net.tazgirl.slayerquests;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SlayerQuestsLibraryFuncs
{


    //==================
    //   Data Classes
    //==================


    public static class Quest
    {
        public String name;
        public String mob;
        public int mean;
        public int skew;
        public int expPerMob;
        public int min;
        public int max;

        public int CalcQuestCap()
        {
            int maxPasses = Config.bellCurveMaxPasses;

            java.util.Random random = new java.util.Random();
            double value;

            for(int i = 0; i < maxPasses; i++)
            {
                value = mean + random.nextGaussian() * skew;
                if(value > min || value < max)
                {
                    return Math.round((float) value);
                }
            }

            return mean;
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

        public void DoAddSet(String questName, String questMob, int questMean, int questSkew, int questExp, int questMin, int questMax)
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

    }


    //========================
    //   Quest Manipulation
    //========================

    public static void DoSetPlayerQuest(Player player, Quest quest)
    {
        player.setData(DataAttachment.CURRENT_QUEST.get(), new DataAttachment.currentQuestRecord(quest.mob, 0, quest.CalcQuestCap(), quest.expPerMob, quest.name));
    }

    //TODO: Will throw error if either name does not exist
    public static void DoSetPlayerQuest(Player player, String tierName, String questName)
    {
        Tier tier = SlayerQuests.tiers.get(SlayerQuests.validTiers.indexOf(tierName));
        Quest quest = tier.GetQuestObjectsInTier().get(tier.GetQuestNamesInTier().indexOf(questName));
        player.setData(DataAttachment.CURRENT_QUEST.get(), new DataAttachment.currentQuestRecord(quest.mob, 0, quest.CalcQuestCap(), quest.expPerMob, quest.name));
    }

    public static String GetQuestState(Player player)
    {
        DataAttachment.currentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST);
        if(playerQuest.mob() != "")
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
        if(playerQuest.mob() != "")
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

    public static boolean DoRewardQuest(Player player, Boolean doRemoveQuest)
    {
        DataAttachment.currentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST);

        if (GetIsQuestComplete(playerQuest))
        {
            DataAttachment.slayerExperienceRecord playerExp = player.getData(DataAttachment.SLAYER_EXPERIENCE);
            int newExp = playerExp.exp() + (playerQuest.slayerExpPerMob() * playerQuest.questCap());
            player.setData(DataAttachment.SLAYER_EXPERIENCE.get(), new DataAttachment.slayerExperienceRecord(CalcExpToLevel(newExp),newExp));

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
        int newExp = playerExp.exp() + (playerQuest.slayerExpPerMob() * playerQuest.questCap());
        player.setData(DataAttachment.SLAYER_EXPERIENCE.get(), new DataAttachment.slayerExperienceRecord(CalcExpToLevel(newExp),newExp));

        if(doRemoveQuest)
        {
            player.setData(DataAttachment.CURRENT_QUEST, new DataAttachment.currentQuestRecord("",0,0,0,""));
        }
    }

    public static void DoRemoveQuest(Player player, Boolean doCheckIfQuestCompleted)
    {
        if(doCheckIfQuestCompleted)
        {
            if(GetIsQuestComplete(player.getData(DataAttachment.CURRENT_QUEST)))
            {
                player.setData(DataAttachment.CURRENT_QUEST, new DataAttachment.currentQuestRecord("",0,0,0,""));
            }
        }
        else
        {
            player.setData(DataAttachment.CURRENT_QUEST, new DataAttachment.currentQuestRecord("",0,0,0,""));
        }

    }

    public static Boolean GetIsQuestComplete(DataAttachment.currentQuestRecord playerQuest)
    {
        return playerQuest.mob() != "" && playerQuest.questCurrent() >= playerQuest.questCap();
    }

    public static DataAttachment.currentQuestRecord GetPlayersQuestAsRecord(Player player)
    {
        return player.getData(DataAttachment.CURRENT_QUEST.get());
    }

    //====================
    //   Calculate Data
    //====================

    public static int GetPlayerLevel(Player player)
    {
        return player.getData(DataAttachment.SLAYER_EXPERIENCE).level();
    }

    public static int CalcExpToLevel(int exp)
    {
        return (int) Math.min(Math.round((-2.5f + Math.sqrt(10 * exp - 43.75)) / 5.0),100);
    }

    public static int CalcLevelToExp(int level)
    {
        return (int) Math.floor((2.5 * Math.pow(level,2)) + (2.5 * level) + 5);
    }

    public static int CalcExpToNextLevel(int currentLevel, int exp)
    {
        return CalcLevelToExp(currentLevel + 1) - (exp - CalcLevelToExp(currentLevel));
    }

    // Use if you don't have a specific Quest object, if you do then use Quest.GenerateQuestCap()//
    public static int CalcQuestCap(int average, int skew, int min, int max)
    {
        int maxPasses = Config.bellCurveMaxPasses;

        java.util.Random random = new java.util.Random();
        double value;

        for(int i = 0; i < maxPasses; i++)
        {
            value = average + random.nextGaussian() * skew;
            if(value > min || value < max)
            {
                return Math.round((float) value);
            }
        }
        return average;

    }



    //Don't use, separate functions
    public String GrantQuest(Player player, int tier)
    {
        DataAttachment.currentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST);

        if(playerQuest.mob() != "")
        {
            if (playerQuest.questCurrent() >= playerQuest.questCap())
            {
                DataAttachment.slayerExperienceRecord playerExp = player.getData(DataAttachment.SLAYER_EXPERIENCE);
                int newExp = playerExp.exp() + (playerQuest.slayerExpPerMob() * playerQuest.questCap());
                player.setData(DataAttachment.SLAYER_EXPERIENCE.get(), new DataAttachment.slayerExperienceRecord(CalcExpToLevel(newExp),newExp));
                return "fulfilled";
            }

            return "incomplete";
        }
        else
        {

            return "assigned";
        }
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
        DoSetStoredQuestHolder(entity, qtrRecord.tierName(), qtrRecord.questName(), entity.level().getGameTime());
    }

    public static void DoClearStoredQuestHolder(LivingEntity entity)
    {
        entity.setData(DataAttachment.QUEST_HOLDER.get(), new DataAttachment.questHolderRecord("","", null));
    }

    public static void DoSetStoredQuestHolder(LivingEntity entity, String tierNameToStore, String questNameToStore, Long timeWhenStored)
    {
        entity.setData(DataAttachment.QUEST_HOLDER.get(), new DataAttachment.questHolderRecord(tierNameToStore, questNameToStore, timeWhenStored));
    }

    public static void DoSetStoredQuestHolder(Entity entity, DataAttachment.questHolderRecord questHolderRecord)
    {
        entity.setData(DataAttachment.QUEST_HOLDER.get(), questHolderRecord);
    }

    public int GetSlayerLevel(Player player)
        {
            return player.getData(DataAttachment.SLAYER_EXPERIENCE).level();
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
        return SlayerQuests.tiers.get(SlayerQuests.validTiers.indexOf(tierName));
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


    //fileTOCopyDir should be something like "/data/slayerquests/SlayerQuests.json"
    public static void DoJSONToConfig(String fileToCopyDir)
    {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path targetFile = configDir.resolve("SlayerQuests.json");

        if (Files.notExists(targetFile))
        {
            try (InputStream in = SlayerQuests.class.getResourceAsStream(fileToCopyDir))
            {
                if(in != null)
                {
                    Files.copy(in, targetFile);
                }
                else
                {
                    SlayerQuests.LOGGER.error("No SlayerQuests.json could be found");
                    throw new RuntimeException("Missing SlayerQuests.json at '" + fileToCopyDir + "' or config directory");
                }

            }
            catch (IOException error) {
                SlayerQuests.LOGGER.error("SlayerQuests.json generation failed", error);
                throw new RuntimeException("Could not generate SlayerQuests.json", error);
            }
        }

    }
}
