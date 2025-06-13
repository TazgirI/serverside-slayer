package net.tazgirl.slayerquests;

import net.minecraft.world.entity.player.Player;

public class GrantQuests
{
    public String GrantQuestLevelOne(Player player, int tier)
    {
        DataAttachment.CurrentQuestRecord playerQuest = player.getData(DataAttachment.CURRENT_QUEST);

        if(playerQuest.mob() != null)
        {
            if (playerQuest.questCurrent() >= playerQuest.questCap())
            {
                DataAttachment.SlayerExperienceRecord playerExp = player.getData(DataAttachment.SLAYER_EXPERIENCE);
                int newExp = playerExp.exp() + playerQuest.slayerExp();
                player.setData(DataAttachment.SLAYER_EXPERIENCE.get(), new DataAttachment.SlayerExperienceRecord(SlayerQuestsPublicFuncs.ExpToLevel(newExp),newExp));
                return "fulfilled";
            }

            return "incomplete";
        }
        else
        {

            return "assigned";
        }
    }



}
