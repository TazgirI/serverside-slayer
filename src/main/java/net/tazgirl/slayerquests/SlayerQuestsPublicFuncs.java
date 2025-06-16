package net.tazgirl.slayerquests;

import net.minecraft.world.entity.player.Player;
import org.joml.Random;

public class SlayerQuestsPublicFuncs
{

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

    public static int GenerateQuestCap(int average, int skew, int min, int max)
    {
        java.util.Random random = new java.util.Random();
        double value;

        for(int i = 0; i < 10; i++)
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
        int average = quest.questMean;
        int skew = quest.questSkew;
        int min = quest.questMin;
        int max = quest.questMax;

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
}
