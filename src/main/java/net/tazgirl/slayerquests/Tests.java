package net.tazgirl.slayerquests;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SlayerQuests.MODID, bus = EventBusSubscriber.Bus.GAME)
public class Tests
{
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("SlayerQuestTests")
                .requires(source -> source.hasPermission(4))
                .executes(context ->
                {
                    List<String> testResults = new ArrayList<>();

                    testResults.add(MobIDToName());

                    String componentMessage = "";
                    for(int i = 0; i < testResults.size(); i++)
                    {
                        componentMessage += testResults.get(i) + "\n";
                    }

                    String finalComponentMessage = componentMessage;
                    context.getSource().sendSuccess(() -> Component.literal(finalComponentMessage), false);

                    return 0;
                })
        );

        dispatcher.register(Commands.literal("SeeMyQuest")
                .requires(source -> source.hasPermission(4))
                .executes(context ->
                {
                    DataAttachment.currentQuestRecord myQuest = context.getSource().getEntity().getData(DataAttachment.CURRENT_QUEST.get());
                    context.getSource().sendSuccess(() -> Component.literal(myQuest.toString()), false);

                    return 0;
                })
        );

        dispatcher.register(Commands.literal("ClearMyQuest")
                .requires(source -> source.hasPermission(4))
                .executes(context ->
                {
                    context.getSource().getEntity().removeData(DataAttachment.CURRENT_QUEST);
                    context.getSource().sendSuccess(() -> Component.literal("Cleared"), false);

                    return 0;
                })
        );

        dispatcher.register(Commands.literal("SeeMyQuestHolder")
                .requires(source -> source.hasPermission(4))
                .executes(context ->
                {
                    context.getSource().sendSuccess(() -> Component.literal(context.getSource().getEntity().getData(DataAttachment.QUEST_HOLDER.get()).toString()), false);

                    return 0;
                })
        );


//        dispatcher.register(Commands.literal("")
//                .requires(source -> source.hasPermission(4))
//                .executes(context ->
//                {
//                    context.getSource().sendSuccess(() -> Component.literal(), false);
//
//                    return 0;
//                })
//        );



    }

    private static String MobIDToName()
    {
        return "minecraft:skeleton converted to " + NitwitQuestGiver.MobPlaintext("minecraft:skeleton");
    }




}
