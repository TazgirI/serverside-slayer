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

    private static final ModConfigSpec.IntValue BELL_CURVE_MAX_PASSES = BUILDER
            .comment("How many times the bell curve attempts to be within the min/max for a given quest before just returning the average")
            .defineInRange("bellCurveMaxPasses", 10, 1, Integer.MAX_VALUE);



    static final ModConfigSpec SPEC = BUILDER.build();

    public static int bellCurveMaxPasses;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        bellCurveMaxPasses = BELL_CURVE_MAX_PASSES.get();

    }
}
