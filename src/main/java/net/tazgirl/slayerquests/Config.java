package net.tazgirl.slayerquests;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = SlayerQuests.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    static final ModConfigSpec SPEC = getBuilder().build();

    public static int bellCurveMaxPasses;
    public static boolean validateLootTables;
    public static boolean sendQuestReminder;


    public static boolean enableNitwitQuests;
    public static boolean enableNitwitBehaviourExtension;
    public static List<Integer> tierLevelThresholds;
    public static int questTimeoutTicks;

    static ModConfigSpec.IntValue BELL_CURVE_MAX_PASSES;
    static ModConfigSpec.BooleanValue VALIDATE_LOOT_TABLES;
    static ModConfigSpec.BooleanValue SEND_QUEST_REMINDER;

    static ModConfigSpec.BooleanValue ENABLE_NITWIT_QUESTS;
    static ModConfigSpec.BooleanValue ENABLE_NITWIT_BEHAVIOUR_EXTENSION;
    static ModConfigSpec.ConfigValue<List<? extends Integer>> TIER_LEVEL_THRESHOLDS;
    static ModConfigSpec.IntValue QUEST_TIMEOUT_TICKS;


    static ModConfigSpec.Builder getBuilder()
    {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");

        BELL_CURVE_MAX_PASSES = builder.comment("How many times the bell curve attempts to be within the min/max for a given quest before just returning the average").defineInRange("bellCurveMaxPasses", 10, 1, Integer.MAX_VALUE);
        VALIDATE_LOOT_TABLES = builder.comment("Do validate loot table exists for each tier and throw error if not (DANGER: Do not touch unless using an addon that explicitly handles/requires this, will almost definitely cause a crash if you are not)").define("validateLootTables", true);
        SEND_QUEST_REMINDER = builder.comment("Remind players about their current quest on login").define("sendQuestReminder",true);


        builder.pop();

        builder.push("nitwit quest giver specific");

        ENABLE_NITWIT_QUESTS = builder.comment("Enable nitwits to assign players slayer quests").define("enableNitwitQuests", true);
        ENABLE_NITWIT_BEHAVIOUR_EXTENSION = builder.comment("Enables the Nitwit quest giving interaction when right clicking LivingEntities with the extendsNitwitBehaviour attachment, if you are adding a mod that uses this library please check if it relies on this attachment").define("enableNitwitQuestsExtension", true);
        TIER_LEVEL_THRESHOLDS = builder.comment("The required slayer level for players to unlock each tier of quests, for performance reasons your tiers must be organised from lowest to highest threshold or the algorithm will not work as intended (if there arent enough items in this list to completely map all tiers, every unassigned tier will be given the rightmost threshold)").define("tierLevelThresholds", List.of(0, 4, 10, 25, 50, 75));
        QUEST_TIMEOUT_TICKS = builder.comment("How many ticks you have to accept a quest when receiving an offer").defineInRange("questTimeoutTicks", 600, 1, Integer.MAX_VALUE);
        builder.pop();

        return builder;
    }




    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        bellCurveMaxPasses = BELL_CURVE_MAX_PASSES.get();
        validateLootTables = VALIDATE_LOOT_TABLES.get();
        sendQuestReminder = SEND_QUEST_REMINDER.get();

        enableNitwitQuests = ENABLE_NITWIT_QUESTS.get();
        enableNitwitBehaviourExtension = ENABLE_NITWIT_BEHAVIOUR_EXTENSION.get();
        tierLevelThresholds = TIER_LEVEL_THRESHOLDS.get().stream().collect(Collectors.toUnmodifiableList());
        questTimeoutTicks = QUEST_TIMEOUT_TICKS.get();

    }
}
