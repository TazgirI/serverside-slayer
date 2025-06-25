package net.tazgirl.slayerquests;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                        List<SlayerQuestsLibraryFuncs.Tier> possibleTiers = CalculcatePossibleTiers(player);
                        SlayerQuestsLibraryFuncs.Tier tierToGive = possibleTiers.get(new Random().nextInt(0,possibleTiers.size()));

                        SlayerQuestsLibraryFuncs.Quest questToGive = tierToGive.GetRandomQuestObject();

                        SlayerQuestsLibraryFuncs.DoSetStoredQuest(villager, tierToGive.name, questToGive.name);
                    }

                    event.setCanceled(true);
                    break;
            }
        }
    }

    private List<SlayerQuestsLibraryFuncs.Tier> CalculcatePossibleTiers(Player player)
    {
        int playerSlayerlevel = SlayerQuestsLibraryFuncs.GetPlayerLevel(player);
        List<Integer> thresholds = Config.tierLevelThresholds;
        List<SlayerQuestsLibraryFuncs.Tier> tiers = SlayerQuestsLibraryFuncs.GetTierObjectsList();
        List<SlayerQuestsLibraryFuncs.Tier> tiersToReturn = new ArrayList<>();

        for(int i = 0; i < tiers.size(); i++)
        {
            if (thresholds.size() - i >= 1)
            {
                if(playerSlayerlevel >= thresholds.get(i))
                {
                    tiersToReturn.add(tiers.get(i));
                }
                else
                {
                    break;
                }
            }
            else
            {
                if(playerSlayerlevel >= thresholds.getLast())
                {
                    tiersToReturn.add(tiers.get(i));
                }
                else
                {
                    break;
                }
            }
        }

        return tiersToReturn;
    }

}
