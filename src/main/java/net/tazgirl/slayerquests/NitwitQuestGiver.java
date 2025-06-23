package net.tazgirl.slayerquests;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class NitwitQuestGiver
{
    @SubscribeEvent
    public void NitwitInteracted(PlayerInteractEvent.EntityInteractSpecific event)
    {
        if(event.getTarget() instanceof Villager villager && villager.getVillagerData().getProfession() == VillagerProfession.NITWIT && event.getLevel() instanceof ServerLevel)
        {
            Player player = event.getEntity();
            DataAttachment.currentQuestRecord currentQuest = player.getData(DataAttachment.CURRENT_QUEST);

            switch(SlayerQuestsLibraryFuncs.GetQuestState(currentQuest))
            {
                case "unfulfilled":
                    return;
                case "fulfilled":

                    event.setCanceled(true);
                    break;
                case "unassigned":
                    if(SlayerQuestsLibraryFuncs.GetStoredQuest(villager) == null)
                    {
                        SlayerQuestsLibraryFuncs.DoSetStoredQuest(villager, SlayerQuestsLibraryFuncs.GetRandomQuestNameFromTier());
                    }

                    event.setCanceled(true);
                    break;


            }


        }
    }

    private int CalulcatePossibleTiers(Player player)
    {
        List<SlayerQuestsLibraryFuncs.Tier> tiers = SlayerQuestsLibraryFuncs.GetTierObjectsList();

        for(int i = 0; i > tiers.size(); i++)
        {

        }

    }

}
