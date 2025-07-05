package net.tazgirl.slayerquests;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = SlayerQuests.MODID, bus = EventBusSubscriber.Bus.GAME)
public class NitwitQuestGiver
{
    //TODO: Turn all GetData and SetData calls into library funcs



    @SubscribeEvent
    public static void NitwitInteracted(PlayerInteractEvent.EntityInteractSpecific event)
    {
        if(event.getTarget() instanceof Villager villager && villager.getVillagerData().getProfession() == VillagerProfession.NITWIT)
        {
            System.out.println(villager.getVillagerData().getProfession());

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

                    DataAttachment.questHolderRecord villagerStoredQuest = villager.getData(DataAttachment.QUEST_HOLDER.get());
                    if(SlayerQuestsLibraryFuncs.GetStoredQuestHolderAsRecord(villager).questName() == "" || !CalculatePossibleTiers(player).contains(SlayerQuestsLibraryFuncs.GetTierObjectFromName(villagerStoredQuest.tierName())))
                    {
                        setVillagersQuest(villager, player);
                    }

                    player.setData(DataAttachment.QUEST_HOLDER.get(), villagerStoredQuest);


                    event.setCanceled(true);
                    break;
            }
        }
    }

    private static List<SlayerQuestsLibraryFuncs.Tier> CalculatePossibleTiers(Player player)
    {
        int playerSlayerLevel = SlayerQuestsLibraryFuncs.GetPlayerLevel(player);
        List<Integer> thresholds = Config.tierLevelThresholds;
        List<SlayerQuestsLibraryFuncs.Tier> tiers = SlayerQuestsLibraryFuncs.GetTierObjectsList();
        List<SlayerQuestsLibraryFuncs.Tier> tiersToReturn = new ArrayList<>();

        for(int i = 0; i < tiers.size(); i++)
        {
            if (thresholds.size() - i >= 1)
            {
                if(playerSlayerLevel >= thresholds.get(i))
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
                if(playerSlayerLevel >= thresholds.getLast())
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

    private static void setVillagersQuest(Villager villager, Player player)
    {
        List<SlayerQuestsLibraryFuncs.Tier> possibleTiers = CalculatePossibleTiers(player);
        SlayerQuestsLibraryFuncs.Tier tierToGive = possibleTiers.get(new Random().nextInt(possibleTiers.size()));

        SlayerQuestsLibraryFuncs.Quest questToGive = tierToGive.GetRandomQuestObject();

        SlayerQuestsLibraryFuncs.DoSetStoredQuest(villager, tierToGive.name, questToGive.name, null);
    }

    private static boolean GrantStoredQuestCommandFunction(Player player)
    {
        DataAttachment.questHolderRecord qtgRecord = player.getData(DataAttachment.QUEST_HOLDER);
        if (qtgRecord.timeWhenStored() != null && (player.level().getGameTime() - qtgRecord.timeWhenStored()) <= Config.questTimeoutTicks)
        {
            SlayerQuestsLibraryFuncs.DoSetPlayerQuest(player,qtgRecord.tierName(),qtgRecord.questName());
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("grantPreparedQuest")
                .requires(source -> source.hasPermission(0))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    boolean success = GrantStoredQuestCommandFunction(player);

                    if (success) {
                        context.getSource().sendSuccess(() -> Component.literal("Quest granted, happy hunting"), false);
                        return 1;
                    } else {
                        context.getSource().sendFailure(Component.literal("No quest stored or request timed out"));
                        return 0;
                    }
                })
        );
    }

    private static void sendQuestPrompt(Player player, String mob, int cap, String tier)
    {
        Component message = Component.literal("I can offer you a tier " + tier + " quest to kill " + cap + " " + mob + "s");
    }

}
