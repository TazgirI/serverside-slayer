package net.tazgirl.slayerquests;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = SlayerQuests.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue BELL_CURVE_MAX_PASSES = BUILDER.comment("How many times the bell curve attempts to be within the min/max for a given quest before just returning the average").defineInRange("bellCurveMaxPasses", 10, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue COPY_OVER_JSON = BUILDER.comment("Do copy the SlayerQuests.json to the config dir if none exists already").define("copyOverJSON",true);
    private static final ModConfigSpec.BooleanValue VALIDATE_LOOT_TABLES = BUILDER.comment("Do validate loot table exists for each tier and throw error if not (DANGER: Do not touch unless using an addon that explicitly handles/requires this, will almost definitely cause a crash if you are not)").define("validateLootTables",true);

    

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int bellCurveMaxPasses;
    public static boolean copyOverJSON;
    public static boolean validateLootTables;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        bellCurveMaxPasses = BELL_CURVE_MAX_PASSES.get();
        copyOverJSON = COPY_OVER_JSON.get();
        validateLootTables = VALIDATE_LOOT_TABLES.get();
    }
}
