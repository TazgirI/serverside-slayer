package net.tazgirl.slayerquests;

import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.NoteBlockEvent;

import javax.xml.crypto.Data;
import java.lang.invoke.SwitchPoint;

public class NitwitQuestGiver
{
    @SubscribeEvent
    public void NitwitInteracted(PlayerInteractEvent.EntityInteractSpecific event)
    {
        if(event.getTarget() instanceof Villager villager && villager.getVillagerData().getProfession() == VillagerProfession.NITWIT && event.getLevel() instanceof ServerLevel)
        {
            Player player = event.getEntity();
            DataAttachment.CurrentQuestRecord currentQuest = player.getData(DataAttachment.CURRENT_QUEST);

            switch(SlayerQuestsLibraryFuncs.GetQuestState(currentQuest))
            {
                case "unfulfilled":
                    return;
                case "fulfilled":

                    event.setCanceled(true);
                    break;
                case "unassigned":

                    event.setCanceled(true);
                    break;


            }


        }
    }

}
