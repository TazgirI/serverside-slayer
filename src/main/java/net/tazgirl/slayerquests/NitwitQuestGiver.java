package net.tazgirl.slayerquests;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
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
        if(event.getTarget() instanceof Villager villager && villager.getVillagerData().getProfession() == VillagerProfession.NITWIT && !event.getLevel().isClientSide)
        {
            Player player = event.getEntity();
            DataAttachment.currentQuestRecord currentQuest = player.getData(DataAttachment.CURRENT_QUEST.get());

            switch(SlayerQuestsLibraryFuncs.GetQuestState(currentQuest))
            {
                case "unfulfilled":

                    player.sendSystemMessage(Component.literal("You still need to kill " + (currentQuest.questCap() - currentQuest.questCurrent() + " " + MobPlaintext(currentQuest.mob()) + "s")));

                    return;
                case "fulfilled":

                    player.sendSystemMessage(Component.literal("Thank you for the help"));
                    SlayerQuestsLibraryFuncs.DoRewardQuest(player, true);
                    break;
                case "unassigned":


                    DataAttachment.questHolderRecord villagerStoredQuest = SlayerQuestsLibraryFuncs.GetStoredQuestHolderAsRecord(villager);
                    if(villagerStoredQuest.questName() == "" || !CalculatePossibleTiers(player).contains(SlayerQuestsLibraryFuncs.GetTierObjectFromName(villagerStoredQuest.tierName())))
                    {
                        setVillagersQuest(villager, player);
                        villagerStoredQuest = SlayerQuestsLibraryFuncs.GetStoredQuestHolderAsRecord(villager);
                    }

                    SlayerQuestsLibraryFuncs.DoSetStoredQuestHolder(player, villagerStoredQuest);
                    SlayerQuestsLibraryFuncs.DoRefreshStoredQuestHolderTime(player);

                    SlayerQuestsLibraryFuncs.Quest storedQuestObject = SlayerQuestsLibraryFuncs.GetStoredQuestHolderAsObject(villager);


                    sendQuestPrompt(player, MobPlaintext(storedQuestObject.mob), villagerStoredQuest.tierName());

                    break;
            }
        }
    }

    static String MobPlaintext(String mobFull)
    {
        ResourceLocation entityLocation = ResourceLocation.parse(mobFull);
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityLocation);

        if (entityType != null)
        {
            return entityType.getDescription().getString();
        }

        return "";
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

        SlayerQuestsLibraryFuncs.DoSetStoredQuestHolder(villager, tierToGive.name, questToGive.name, null);
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

    private static void sendQuestPrompt(Player player, String mob, String tier)
    {
        Component text = Component.literal("I can offer you a tier " + tier + " quest to kill " + mob + "s");

        MutableComponent accept = Component.literal("[accept]").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/grantPreparedQuest")));

        Component message = Component.literal("").append(text).append("\n").append(accept);

        player.sendSystemMessage(message);
    }

}
