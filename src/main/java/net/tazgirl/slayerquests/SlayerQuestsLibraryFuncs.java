package net.tazgirl.slayerquests;

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
    }

    public static class Tier
    {
        static String name;
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

    public static String GetQuestState(Player player)
    {
        DataAttachment.currentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST);
        if(playerQuest.mob() != null)
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
        if(playerQuest.mob() != null)
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
                player.setData(DataAttachment.CURRENT_QUEST, new DataAttachment.currentQuestRecord(null,0,0,0,""));
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
            player.setData(DataAttachment.CURRENT_QUEST, new DataAttachment.currentQuestRecord(null,0,0,0,""));
        }
    }

    public static void DoRemoveQuest(Player player, Boolean doCheckIfQuestCompleted)
    {
        if(doCheckIfQuestCompleted)
        {
            DataAttachment.currentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST);

            if(GetIsQuestComplete(playerQuest))
            {
                player.setData(DataAttachment.CURRENT_QUEST, new DataAttachment.currentQuestRecord(null,0,0,0,""));
            }
        }
        else
        {
            player.setData(DataAttachment.CURRENT_QUEST, new DataAttachment.currentQuestRecord(null,0,0,0,""));
        }

    }

    public static Boolean GetIsQuestComplete(DataAttachment.currentQuestRecord playerQuest)
    {
        return playerQuest.mob() != null && playerQuest.questCurrent() >= playerQuest.questCap();
    }

    //====================
    //   Calculate Data
    //====================


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

        if(playerQuest.mob() != null)
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

        public static String GetStoredQuest(LivingEntity entity)
        {
            return entity.getData(DataAttachment.QUEST_TO_GIVE).questName();
        }

        public static void DoSetStoredQuest(LivingEntity entity, String questNameToStore)
        {
            entity.setData(DataAttachment.QUEST_TO_GIVE.get(), new DataAttachment.questToGiveRecord(questNameToStore));
        }

        public int GetSlayerLevel(Player player)
        {
            return player.getData(DataAttachment.SLAYER_EXPERIENCE).level();
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

        return null;

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
