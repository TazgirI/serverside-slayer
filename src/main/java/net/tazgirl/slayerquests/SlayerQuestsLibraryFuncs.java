package net.tazgirl.slayerquests;

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

        public int GenerateQuestCap()
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

        protected void setName(String newName)
        {
            name = newName;
        }

        public void setQuestNames(List<String> newQuestNames)
        {
            validQuestNames = newQuestNames;
        }

        public List<String> QuestNamesInTier()
        {
            return validQuestNames;
        }

        public List<Quest> QuestObjectsInTier()
        {
            return quests;
        }

        public boolean RemoveQuest(String questName)
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

        public boolean RemoveQuest(Quest questToRemove)
        {
            if(quests.contains(questToRemove) && quests.size() != 1)
            {
                quests.remove(questToRemove);
                validQuestNames.remove(questToRemove.name);

                return true;
            }

            return false;
        }

        public String RandomQuestName()
        {
            return quests.get(new Random().nextInt(0,quests.size())).name;
        }

        public Quest RandomQuestObject()
        {
            return quests.get(new Random().nextInt(0,quests.size()));
        }

    }


    //====================
    //   Calculate Data
    //====================


    public static int ExpToLevel(int exp)
    {
        return (int) Math.min(Math.round((-2.5f + Math.sqrt(10 * exp - 43.75)) / 5.0),100);
    }

    public static int LevelToExp(int level)
    {
        return (int) Math.floor((2.5 * Math.pow(level,2)) + (2.5 * level) + 5);
    }

    public static int ExpToNextLevel(int currentLevel, int exp)
    {
        return LevelToExp(currentLevel + 1) - (exp - LevelToExp(currentLevel));
    }

    // Use if you don't have a specific Quest object, if you do then use Quest.GenerateQuestCap()//
    public static int GenerateQuestCap(int average, int skew, int min, int max)
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
        DataAttachment.CurrentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST);

        if(playerQuest.mob() != null)
        {
            if (playerQuest.questCurrent() >= playerQuest.questCap())
            {
                DataAttachment.SlayerExperienceRecord playerExp = player.getData(DataAttachment.SLAYER_EXPERIENCE);
                int newExp = playerExp.exp() + (playerQuest.slayerExpPerMob() * playerQuest.questCap());
                player.setData(DataAttachment.SLAYER_EXPERIENCE.get(), new DataAttachment.SlayerExperienceRecord(ExpToLevel(newExp),newExp));
                return "fulfilled";
            }

            return "incomplete";
        }
        else
        {

            return "assigned";
        }
    }


    //========================
    //   Quest Manipulation
    //========================

    public String GetQuestState(Player player)
    {
        DataAttachment.CurrentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST);
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

    public String GetQuestState(DataAttachment.CurrentQuestRecord playerQuest)
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

    public boolean RewardQuest(Player player, Boolean doRemoveQuest)
    {
        DataAttachment.CurrentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST);

        if (IsQuestComplete(playerQuest))
        {
            DataAttachment.SlayerExperienceRecord playerExp = player.getData(DataAttachment.SLAYER_EXPERIENCE);
            int newExp = playerExp.exp() + (playerQuest.slayerExpPerMob() * playerQuest.questCap());
            player.setData(DataAttachment.SLAYER_EXPERIENCE.get(), new DataAttachment.SlayerExperienceRecord(ExpToLevel(newExp),newExp));

            if(doRemoveQuest)
            {
                player.setData(DataAttachment.CURRENT_QUEST, new DataAttachment.CurrentQuestRecord(null,0,0,0,""));
            }

            return true;
        }

        return false;
    }

    public void ForceRewardQuest(Player player, Boolean doRemoveQuest)
    {
        DataAttachment.CurrentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST);

        DataAttachment.SlayerExperienceRecord playerExp = player.getData(DataAttachment.SLAYER_EXPERIENCE);
        int newExp = playerExp.exp() + (playerQuest.slayerExpPerMob() * playerQuest.questCap());
        player.setData(DataAttachment.SLAYER_EXPERIENCE.get(), new DataAttachment.SlayerExperienceRecord(ExpToLevel(newExp),newExp));

        if(doRemoveQuest)
        {
            player.setData(DataAttachment.CURRENT_QUEST, new DataAttachment.CurrentQuestRecord(null,0,0,0,""));
        }
    }

    public void RemoveQuest(Player player, Boolean doCheckIfQuestCompleted)
    {
        if(doCheckIfQuestCompleted)
        {
            DataAttachment.CurrentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST);

            if(IsQuestComplete(playerQuest))
            {
                player.setData(DataAttachment.CURRENT_QUEST, new DataAttachment.CurrentQuestRecord(null,0,0,0,""));
            }
        }
        else
        {
            player.setData(DataAttachment.CURRENT_QUEST, new DataAttachment.CurrentQuestRecord(null,0,0,0,""));
        }

    }

    public Boolean IsQuestComplete(DataAttachment.CurrentQuestRecord playerQuest)
    {
        if(playerQuest.mob() != null && playerQuest.questCurrent() >= playerQuest.questCap())
        {
            return true;
        }

        return false;
    }

    //==================
    //   Fetch Random
    //==================


    public String RandomTierName()
    {
        return SlayerQuests.validTiers.get(new Random().nextInt(0,SlayerQuests.validTiers.size()));
    }

    public Tier RandomTierObject()
    {
        return SlayerQuests.tiers.get(new Random().nextInt(0,SlayerQuests.tiers.size()));
    }

    // Use if you don't have a specific Tier object, if you do then use Tier.RandomQuestName()
    public String RandomQuestNameFromTier(String tier)
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
    public Quest RandomQuestObjectFromTier(String tier)
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


    public List<String> TierNamesList()
    {
        return SlayerQuests.validTiers;
    }

    public List<Tier> TierObjectsList()
    {
        return SlayerQuests.tiers;
    }

    // Use if you don't have a specific Tier object, if you do then use Tier.QuestNamesInTier()
    public List<String> QuestNamesInTier(String tierName)
    {
        int tierIndex = SlayerQuests.validTiers.indexOf(tierName);

        if(tierIndex != -1)
        {
            return SlayerQuests.tiers.get(tierIndex).validQuestNames;
        }

        return new ArrayList<>();
    }

    // Use if you don't have a specific Tier object, if you do then use Tier.QuestObjectsInTier()
    public List<Quest> QuestObjectsInTier(String tier)
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
    public void JSONToConfig(String fileToCopyDir)
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
