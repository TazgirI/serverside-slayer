package net.tazgirl.slayerquests;

import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

public class SlayerQuestsLibraryFuncs
{

    //================
    //   Fetch Data
    //================


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

    //Don't use, seperate functions
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


    //======================
    //  Quest Manipulation
    //======================


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

    public static int GenerateQuestCap(StoreJSON.Quest quest)
    {
        int average = quest.mean;
        int skew = quest.skew;
        int min = quest.min;
        int max = quest.max;

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

    public String FetchQuestNameFromTier(String tier)
    {
        List<StoreJSON.QuestTier> tiersList = SlayerQuests.tiers;
        List<String> validTiers = SlayerQuests.validTiers;

        if(validTiers.contains(tier))
        {
            StoreJSON.QuestTier tierObject = tiersList.get(validTiers.indexOf(tier));
            List<StoreJSON.Quest> questList = tierObject.quests;

            Random random = new Random();

            return questList.get(random.nextInt(0,questList.size())).name;
        }

        return null;

    }

    public StoreJSON.Quest FetchQuestObjectFromTier(String tier)
    {
        List<StoreJSON.QuestTier> tiersList = SlayerQuests.tiers;
        List<String> validTiers = SlayerQuests.validTiers;

        if(validTiers.contains(tier))
        {
            StoreJSON.QuestTier tierObject = tiersList.get(validTiers.indexOf(tier));
            List<StoreJSON.Quest> questList = tierObject.quests;

            return questList.get(new Random().nextInt(0,questList.size()));
        }

        return null;

    }

    public String RandomTierName()
    {
        return SlayerQuests.validTiers.get(new Random().nextInt(0,SlayerQuests.validTiers.size()));
    }

    public StoreJSON.QuestTier RandomTierObject()
    {
        return SlayerQuests.tiers.get(new Random().nextInt(0,SlayerQuests.tiers.size()));
    }

    public List<String> TierNamesList()
    {
        return SlayerQuests.validTiers;
    }

    public List<StoreJSON.QuestTier> TierObjectsList()
    {
        return SlayerQuests.tiers;
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
