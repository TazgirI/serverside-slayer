package net.tazgirl.slayerquests;

import net.minecraft.nbt.CompoundTag;
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
                // Award loot
                return "fulfilled";
            }

            return "incomplete";
        }
        else
        {

            return "assigned";
        }
    }

    public CompoundTag QuestTag(String mob, int max)
    {
        CompoundTag tag = new CompoundTag();

        tag.putString("QuestTarget", mob);
        tag.putInt("QuestCurrent", 0);
        tag.putInt("QuestCap", max);

        return tag;
    }

}
