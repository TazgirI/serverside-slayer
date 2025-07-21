package net.tazgirl.slayerquests;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

                    Player player = (Player) context.getSource().getEntity();

                    testResults.add(MobIDToName());
                    testResults.add(MobTypeMatchesQuestMob(player));
                    testResults.add(MobTypeDoesntMatchQuestMob(player));
                    testResults.add(RandomLevelAsExp());
                    testResults.add(RandomLevelAsExpTotal());

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

        dispatcher.register(Commands.literal("SpawnMyQuestLoot")
                .requires(source -> source.hasPermission(4))
                .executes(context ->
                {
                    ServerPlayer player = context.getSource().getPlayer();
                    SlayerQuestsLibraryFuncs.DoDropPlayerQuestLoot(player,SlayerQuestsLibraryFuncs.GetQuestLootRoll(SlayerQuestsLibraryFuncs.GetQuestObjectFromPlayer(player), context.getSource().getServer()));

                    return 0;
                })
        );

        dispatcher.register(Commands.literal("ClearMySlayerLevel")
                .requires(source -> source.hasPermission(4))
                .executes(context ->
                {
                    context.getSource().getPlayer().setData(DataAttachment.SLAYER_EXPERIENCE.get(), new DataAttachment.slayerExperienceRecord(1,1));

                    return 0;
                })
        );

        dispatcher.register(Commands.literal("SeeMySlayerLevel")
                .requires(source -> source.hasPermission(4))
                .executes(context ->
                {
                    context.getSource().sendSuccess(() -> Component.literal(context.getSource().getEntity().getData(DataAttachment.SLAYER_EXPERIENCE.get()).toString()), false);

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
        return "minecraft:skeleton converted to " + SlayerQuestsLibraryFuncs.GetMobPlaintext("minecraft:skeleton");
    }

    private static String MobTypeMatchesQuestMob(Player player)
    {
        player.setData(DataAttachment.CURRENT_QUEST.get(), new DataAttachment.currentQuestRecord("minecraft:skeleton", 0, 0, 0, "",""));
        return "Set player quest to minecraft:skeleton and compared it against a skeleton: " + SlayerQuestsLibraryFuncs.CalcTypeMatchesQuest(player, new Skeleton(EntityType.SKELETON, player.level()));
    }

    private static String MobTypeDoesntMatchQuestMob(Player player)
    {
        player.setData(DataAttachment.CURRENT_QUEST.get(), new DataAttachment.currentQuestRecord("minecraft:zombie", 0, 0, 0, "",""));
        return "Set player quest to minecraft:zombie and compared it against a skeleton: " + SlayerQuestsLibraryFuncs.CalcTypeMatchesQuest(player, new Skeleton(EntityType.SKELETON, player.level()));

    }

    private static String RandomLevelAsExp()
    {
        Random random = new Random();
        int randomNumber = random.nextInt(100);
        int result =  SlayerQuestsLibraryFuncs.CalcLevelToExp(randomNumber);
        return randomNumber + " converted to " + result + " exp";
    }

    private static String RandomLevelAsExpTotal()
    {
        Random random = new Random();
        int randomNumber = random.nextInt(100);
        int result =  SlayerQuestsLibraryFuncs.CalcLevelToExpTotal(randomNumber);
        return randomNumber + " converted to " + result + " exp total";
    }


}
