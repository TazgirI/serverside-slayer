package net.tazgirl.slayerquests;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static net.minecraft.commands.Commands.argument;

@EventBusSubscriber(modid = SlayerQuests.MODID, bus = EventBusSubscriber.Bus.GAME)
public class NitwitQuestGiver
{


    @SubscribeEvent
    public static void NitwitInteracted(PlayerInteractEvent.EntityInteractSpecific event)
    {
        LivingEntity questGiverMob;
        Entity eventTarget = event.getTarget();

        if(event.getLevel().isClientSide || !(eventTarget instanceof LivingEntity))
        {
            return;
        }

        if(eventTarget instanceof Villager villager && villager.getVillagerData().getProfession() == VillagerProfession.NITWIT)
        {
            questGiverMob = villager;
        }
        else if(eventTarget.getData(DataAttachment.EXTENDS_NITWIT_BEHAVIOUR.get()).extendsNitwitBehaviour())
        {
            questGiverMob = (LivingEntity) eventTarget;
        }
        else
        {
            return;
        }



        ServerPlayer player = (ServerPlayer) event.getEntity();
        DataAttachment.currentQuestRecord currentQuest = SlayerQuestsLibraryFuncs.GetPlayersQuestAsRecord(player);

        switch(SlayerQuestsLibraryFuncs.GetQuestState(currentQuest))
        {
            case "unfulfilled":

                player.sendSystemMessage(Component.literal("You still need to kill " + (currentQuest.questCap() - currentQuest.questCurrent() + " " + SlayerQuestsLibraryFuncs.GetMobPlaintext(currentQuest.mob()) + "s")));

                return;
            case "fulfilled":

                player.sendSystemMessage(Component.literal("Thank you for the help"));
                SlayerQuestsLibraryFuncs.DoAttemptRewardQuest(player, true);
                break;
            case "unassigned":

                SlayerQuestsLibraryFuncs.DoRefreshStoredQuestHolderTime(questGiverMob);
                DataAttachment.questHolderRecord villagerStoredQuest = SlayerQuestsLibraryFuncs.GetStoredQuestHolderAsRecord(questGiverMob);
                if(Objects.equals(villagerStoredQuest.questName(), "") || !CalculatePossibleTiers(player).contains(SlayerQuestsLibraryFuncs.GetTierObjectFromName(villagerStoredQuest.tierName())))
                {
                    setVillagersQuest(questGiverMob, player);
                    SlayerQuestsLibraryFuncs.DoRefreshStoredQuestHolderTime(questGiverMob);
                    villagerStoredQuest = SlayerQuestsLibraryFuncs.GetStoredQuestHolderAsRecord(questGiverMob);
                }



                SlayerQuestsLibraryFuncs.DoSetStoredQuestHolder(player, villagerStoredQuest);
                SlayerQuestsLibraryFuncs.DoRefreshStoredQuestHolderTime(player);

                SlayerQuestsLibraryFuncs.Quest storedQuestObject = SlayerQuestsLibraryFuncs.GetStoredQuestHolderAsObject(questGiverMob);


                sendQuestPrompt(player, SlayerQuestsLibraryFuncs.GetMobPlaintext(storedQuestObject.mob), villagerStoredQuest.tierName(), villagerStoredQuest.timeWhenStored());

                break;
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

    private static void setVillagersQuest(LivingEntity villager, Player player)
    {
        List<SlayerQuestsLibraryFuncs.Tier> possibleTiers = CalculatePossibleTiers(player);
        SlayerQuestsLibraryFuncs.Tier tierToGive = possibleTiers.get((int) (Math.pow(Math.random(), 0.5) * possibleTiers.size()));

        SlayerQuestsLibraryFuncs.Quest questToGive = tierToGive.GetRandomQuestObject();

        SlayerQuestsLibraryFuncs.DoSetStoredQuestHolder(villager, tierToGive.name, questToGive.name, null, villager.getId());
    }

    private static boolean GrantStoredQuestCommandFunction(Player player)
    {
        DataAttachment.questHolderRecord qtgRecord = SlayerQuestsLibraryFuncs.GetStoredQuestHolderAsRecord(player);
        if (qtgRecord.timeWhenStored() != null && (player.level().getGameTime() - qtgRecord.timeWhenStored()) <= Config.questTimeoutTicks)
        {
            SlayerQuestsLibraryFuncs.DoSetPlayerQuest(player,qtgRecord.tierName(),qtgRecord.questName());
            return true;
        }
        return false;
    }

    @SubscribeEvent
    private static void onPlayerJoins(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(Config.sendQuestReminder)
        {
            Player player = event.getEntity();
            String playerState = SlayerQuestsLibraryFuncs.GetQuestState(player);
            if(playerState.equals("unfulfilled"))
            {
                DataAttachment.currentQuestRecord currentQuest = SlayerQuestsLibraryFuncs.GetPlayersQuestAsRecord(player);

                player.sendSystemMessage(Component.literal("Reminder, you still need to kill " + (currentQuest.questCap() - currentQuest.questCurrent()) + " " + SlayerQuestsLibraryFuncs.GetMobPlaintext(currentQuest.mob() + "s")).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
            }
            else if(playerState.equals("fulfilled"))
            {
                player.sendSystemMessage(Component.literal("You have a completed quest to hand in").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
            }


        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("grantPreparedQuest")
                .requires(source -> source.hasPermission(0))
                .then(argument("timeWhenStored", LongArgumentType.longArg())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    boolean success = false;

                    boolean correctQuest = LongArgumentType.getLong(context, "timeWhenStored") == SlayerQuestsLibraryFuncs.GetStoredQuestHolderAsRecord(player).timeWhenStored();

                    if (!correctQuest)
                    {
                        context.getSource().sendSuccess(() -> Component.literal("You seem to have considered another quest since talking to me, please come and receive this offer again"), false);
                        return 0;
                    }

                    success = GrantStoredQuestCommandFunction(player);


                    if (success)
                    {
                        DataAttachment.currentQuestRecord currentQuest = SlayerQuestsLibraryFuncs.GetPlayersQuestAsRecord(player);
                        context.getSource().sendSuccess(() -> Component.literal("Quest granted, happy hunting.\nCome back after you've killed " + currentQuest.questCap() + " " + SlayerQuestsLibraryFuncs.GetMobPlaintext(currentQuest.mob()) + "s"), false);
                        SlayerQuestsLibraryFuncs.DoClearStoredQuestHolder((LivingEntity) player.level().getEntity(SlayerQuestsLibraryFuncs.GetStoredQuestHolderAsRecord(player).sourceUuid()));
                        return 1;
                    }
                    else
                    {
                        context.getSource().sendFailure(Component.literal("No quest stored or request timed out"));
                        return 0;
                    }
                }))
        );
        dispatcher.register(Commands.literal("explainLevels")
                .requires(source -> source.hasPermission(0))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    Long timeWhenStored = SlayerQuestsLibraryFuncs.GetStoredQuestHolderAsRecord(player).timeWhenStored();
                    if(Objects.equals(CalculatePossibleTiers(player).getLast().name, SlayerQuestsLibraryFuncs.GetStoredQuestHolderAsObject(player).tier))
                    {
                        MutableComponent accept = Component.literal("[accept quest anyway]").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/grantPreparedQuest " + timeWhenStored)));
                        context.getSource().sendSuccess(() -> Component.literal("Until you get a higher level by completing quests, this is the best I can offer \n").append(accept), false);
                    }
                    else
                    {
                        MutableComponent accept = Component.literal("[accept quest anyway]").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/grantPreparedQuest " + timeWhenStored)));
                        context.getSource().sendSuccess(() -> Component.literal("This is all I can offer at the moment \n").append(accept), false);
                    }


                    return 0;
                })
        );
    }

    private static void sendQuestPrompt(Player player, String mob, String tier, long timeWhenStored)
    {
        Component text = Component.literal("I can offer you a tier " + tier + " quest to kill " + mob + "s");

        MutableComponent accept = Component.literal("[accept]").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/grantPreparedQuest " + timeWhenStored)));
        MutableComponent higherTier = Component.literal("[give me harder quests]").withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/explainLevels")));

        Component message = Component.literal("").append(text).append("\n").append(accept).append("  ").append(higherTier);

        player.sendSystemMessage(message);
    }

}
